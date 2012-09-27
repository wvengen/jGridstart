package nl.nikhef.browsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.nikhef.browsers.exception.BrowserExecutionException;
import nl.nikhef.browsers.exception.BrowserNotAvailableException;
import nl.nikhef.jgridstart.osutils.FileUtils;

/** Unix/Linux/BSD/... implementation of browser discovery and launch. */
class BrowsersUnix extends BrowsersCommon {
    
    private String defaultBrowser = null;
    
    @Override @SuppressWarnings("unchecked") // for clone() cast
    public void initialize() throws IOException {
	boolean defaultBrowserFound = false;
	
	// find default browser
	String defaultBrowserExe = null;
	String defaultBrowserPath = findDefaultBrowserEnvironment();
	if (defaultBrowserPath==null)
	    defaultBrowserPath = findDefaultBrowserDesktop();
	if (defaultBrowserPath==null)
	    defaultBrowserPath = findDefaultBrowserJava();
	defaultBrowserPath = normaliseBrowserPath(defaultBrowserPath);
	// we only want the basename
	if (defaultBrowserPath!=null)
	    defaultBrowserExe = new File(defaultBrowserPath).getName();
	logger.fine("default browser: "+defaultBrowserExe);
	
	// find known browsers, keep only which are in path
	availableBrowsers = (HashMap<String, Properties>)readKnownBrowsers().clone();
	for (Iterator<Entry<String, Properties>> it = availableBrowsers.entrySet().iterator(); it.hasNext(); ) {
	    Entry<String, Properties> entry = it.next();
	    Properties p = entry.getValue();
	    // we need an exe property
	    if (p.getProperty("exe")!=null) {
		// first make sure default browser is in known browsers as well
		// important when the default browser is not in PATH
		if (p.getProperty("exe").equals(defaultBrowserExe)) {
		    // set to full path so it can be found
		    p.setProperty("exe", defaultBrowserPath);
		    defaultBrowserFound = true;
		    defaultBrowser = entry.getKey();
		    continue;
		}
		// find using which; process spawning in unix is cheap
		String[] cmd = new String[] { "which", p.getProperty("exe") };
		//int ret = Runtime.getRuntime().exec(cmd).waitFor();
		int ret = FileUtils.Exec(cmd);
		if (ret==0) {
		    logger.fine("found browser "+entry.getKey()+" at: "+p.getProperty("exe"));
		    continue;
		}
	    }
	    // error or not found, remove from list
	    it.remove();
	}
	
	// add default browser as entry if not found
	if (!defaultBrowserFound) {
	    if (defaultBrowserPath!=null) {
		// dummy entry
		Properties p = new Properties();
		p.setProperty("desc", defaultBrowserExe);
		p.setProperty("exe", defaultBrowserPath);
		p.setProperty("certinst", "manual");
		defaultBrowser = defaultBrowserExe;
		availableBrowsers.put(defaultBrowser, p);
	    } else {
		// TODO select first browser detected
	    }
	}
	
    }
    
    /** Get default browser from environment $BROWSER, or null if unset. */
    private String findDefaultBrowserEnvironment() {
	logger.finer("Finding default browser using environment");
	String browser = System.getenv("BROWSER");
	return browser;
    }
    
    /** Normalise the browser path.
     * <p>
     * Currently this follows the symlink if its name is {@code x-www-browser},
     * {@code gnome-www-browser}  or {@code sensible-browser}, so
     * that we find the actual browser pointed to by Debian's alternatives.
     */
    private String normaliseBrowserPath(String path) {
	logger.finer("Normalising browser path: "+path);
	// Debian's sensible-browser is a wrapper for one of the others
	if (path.equals("sensible-browser") || path.endsWith("/sensible-browser")) {
	    if (System.getenv("GNOME_DESKTOP_SESSION_ID")!=null)
		path = "gnome-www-browser";
	    else
		path = "x-www-browser";
	}
	// handle Debian alternatives
	final String[] alternatives = new String[] {
	    "x-www-browser", "gnome-www-browser"
	};
	for (int i=0; i<alternatives.length; i++) {
	    if (path.equals(alternatives[i]) || path.endsWith("/"+alternatives[i])) {
		// A Debian alternative is a symlink to /etc/alternatives/something,
		// which in turn is a symlink to the real program.
		try {
		    // full path required for readLink()
		    if (!path.startsWith("/")) {
			StringBuffer abspath = new StringBuffer();
			if (FileUtils.Exec(new String[]{"which",path}, null, abspath)!=0)
			    continue;
			path = abspath.toString().trim();
		    }
		    String link1;
		    link1 = readLink(path);
		    if (link1!=null) {
			String link2 = readLink(link1);
			if (link2!=null) {
			    logger.fine("Normalised browser path: "+link2);
			    return link2;
			}
		    }
		} catch(IOException e) { /* fall through */ }
	    }
	}
	return path;
    }
    /** Unix readlink command, returns null it path was no symlink. 
     * @throws IOException */
    private String readLink(String path) throws IOException {
	StringBuffer output = new StringBuffer();
	if (FileUtils.Exec(new String[] { "readlink", path }, null, output)!=0)
	    return null;
	return output.toString().trim();
    }
    
    /** Get default browser from desktop environment */
    private String findDefaultBrowserDesktop() {
	String browser = null;
	
	if (System.getenv("GNOME_DESKTOP_SESSION_ID")!=null) {
	    // the era of Gnome3 uses XDG
	    if (System.getenv("GNOME_DESKTOP_SESSION_ID").equals("this-is-deprecated"))
		browser = findDefaultBrowserXDG();
	    
	    // get from gconf
	    if (browser==null)
		browser = findDefaultBrowserGConf();
	    
	} else if (System.getenv("KDE_FULL_SESSION")!=null) {
	    // get from kde settings
	    browser = findDefaultBrowserKDE("kde4");
	    if (browser==null)
		browser = findDefaultBrowserKDE("kde");
	    
	} else {
	    // get browser from xdg-mime
	    browser = findDefaultBrowserXDG();
	    // if that fails, otherwise try all desktop methods
	    if (browser==null)
		browser = findDefaultBrowserGConf();
	    if (browser==null)
		browser = findDefaultBrowserKDE("kde4");
	    if (browser==null)
		browser = findDefaultBrowserKDE("kde");
	}
	
	return browser;
    }
    
    /** Get default browser from GConf setting */
    private String findDefaultBrowserGConf() {
	logger.finer("Finding default browser using GConf");
	String browser = null;
	// make sure it is enabled
	if (!Boolean.valueOf(getGConfValue("/desktop/gnome/url-handlers/https/enabled")))
	    return null;
	// get value
	// TODO keep command-line arguments ... !
	String cmd = getGConfValue("/desktop/gnome/url-handlers/https/command");
	if (cmd==null) return null;
	browser = cmd.split("\\s", 2)[0];
	logger.fine("Default GConf browser: "+browser);
	return browser;
    }
    /** Returns the value of a GConf setting, or null if not available. */
    private String getGConfValue(String key) {
	StringBuffer output = new StringBuffer();
	try {
	    String[] cmd = new String[] { "gconftool-2", "-g", key };
	    if (FileUtils.Exec(cmd, null, output)!=0)
	    	return null;
	} catch(IOException e) {
	    return null;
	}
	// still zero return value when key doesn't exist ... sigh
	if (output.toString().trim().equals("No value set for `"+key+"'"))
	    return null;
	return output.toString().trim();
    }
    
    /** Get default browser from KDE setting.
     * <p>
     * The parameter specifies which kde version is looked for; they
     * have different dot-directories.
     * 
     * @param versionid either "kde" or "kde4"
     */
    private String findDefaultBrowserKDE(String versionid) {
	logger.finer("Finding default browser using KDE, "+versionid);
	// http://docs.kde.org/development/en/kdebase-runtime/userguide/configuration-files.html
	String cfg = System.getenv("HOME") + "/." + versionid + "/share/config/kdeglobals";
	try {
	    BufferedReader read = new BufferedReader(new FileReader(cfg));
	    String line;
	    boolean inGeneralSection = false;
	    while ( (line = read.readLine()) != null ) {
		// keep track of current section in ini file
		if (line.trim().toLowerCase().startsWith("[general]"))
		    inGeneralSection = true;
		else if (line.trim().startsWith("["))
		    inGeneralSection = false;
		// catch default browser in general section
		if (inGeneralSection && line.trim().toLowerCase().startsWith("BrowserApplication")) {
		    final Pattern pat = Pattern.compile("^\\s*BrowserApplication(\\[.*?\\])?\\s*=\\s*(.*?)\\s*$");
		    String browser = pat.matcher(line).group(2);
		    // empty means default browser = get from mime-type
		    // TODO implement mime-type parsing ... but let's wait for users to actually complain before investing more time
		    if (browser == "") return "konqueror";
		    // custom entry begins with exclamation mark; not sure if this is always the case
		    if (browser.startsWith("!")) browser = browser.substring(1);
		    logger.fine("Default KDE browser: "+browser);
		    return browser;
		}
	    }
	} catch (IOException e) { }
	// not found
	return null;
    }
    
    /** Get default browser from {@code xdg-mime} utility */
    private String findDefaultBrowserXDG() {
	logger.finer("Finding default browser using XDG");
	// loosely based on http://cgit.freedesktop.org/xdg/xdg-utils/tree/scripts/xdg-mime
	StringBuffer output = new StringBuffer();
	try {
	    // get desktop file for html mimetype
	    String[] cmd = new String[]{ "xdg-mime", "query", "default", "x-scheme-handler/http" };
	    FileUtils.Exec(cmd, null, output);
	    if (output.toString().trim().isEmpty()) return null;
	    logger.finer("Default XDG browser desktop file name: "+output.toString().trim());
	    // locate desktop file
	    String desktop = getDesktopPath(output.toString().trim());
	    if (desktop==null) return null;
	    logger.finest("Default XDG browser desktop file: "+desktop);
	    // get executable from desktop file
	    BufferedReader ins = new BufferedReader(new FileReader(desktop));
	    String line;
	    while ((line=ins.readLine()) != null) {
		final Pattern pat = Pattern.compile("^Exec(\\[[^]=]*\\])?=\\s*(.*?)(\\s.*)?$");
		Matcher matcher = pat.matcher(line);
		if (matcher.matches()) {
		    logger.fine("Default XDG browser: "+matcher.group(2));
		    return matcher.group(2);
		}
	    }
	} catch(IOException e) { /* fall through */ }
	// failed
	return null;
    }
    /** Returns absolute location of a .desktop file, according to xdg spec. */
    private String getDesktopPath(String name) {
	// again based on http://cgit.freedesktop.org/xdg/xdg-utils/tree/scripts/xdg-mime
	ArrayList<String> searchPaths = new ArrayList<String>();
	if (System.getenv("XDG_DATA_HOME")!=null)
	    searchPaths.add(System.getenv("XDG_DATA_HOME"));
	else
	    searchPaths.add(System.getenv("HOME") + "/.local/share");
	if (System.getenv("XDG_DATA_DIRS")!=null)
	    searchPaths.addAll(Arrays.asList(System.getenv("XDG_DATA_DIRS").split(Pattern.quote(File.pathSeparator))));
	else {
	    searchPaths.add("/usr/local/share");
	    searchPaths.add("/usr/share");
	}
	// find desktop file in path
	for (Iterator<String> pathit=searchPaths.iterator(); pathit.hasNext();) {
	    String path = pathit.next() + "/applications/" + name;
	    if ((new File(path)).exists())
		return path;
	}
	// not found
	return null;
    }
    

    @Override
    public String getDefaultBrowser() {
	return defaultBrowser;
    }

    @Override
    public void openUrl(String browserid, String urlString)
	    throws BrowserNotAvailableException, BrowserExecutionException {
	
	// check if browserid is present
	if (!availableBrowsers.containsKey(browserid))
	    throw new BrowserNotAvailableException(browserid);
	
	// run the command
	String[] cmd = new String[] {
		availableBrowsers.get(browserid).getProperty("exe"),
		urlString
	};
	try {
	    // execute browser
	    //   don't wait for this, since starting a new browser when the
	    //   process isn't running yet can take a loooong time
	    logger.fine("running browser: "+Arrays.toString(cmd));
	    Runtime.getRuntime().exec(cmd);
	    /*
	    StringBuffer output = new StringBuffer();
	    if ( FileUtils.Exec(cmd, null, output) != 0)
		throw new BrowserExecutionException(browserid, output.toString());
	    */
	} catch (IOException e) {
	    throw new BrowserExecutionException(browserid, e);
	}
    }

    @Override
    protected void installPKCS12System(String browserid, File pkcs) throws BrowserExecutionException {
	throw new BrowserExecutionException(browserid,
		"There is no default certificate store on Unix/Linux,\n" +
		"please install the certificate manually in your browser.");
    }
    
    @Override
    protected HashMap<String, Properties> readKnownBrowsers() throws IOException {
	knownBrowsers = super.readKnownBrowsers();
	// filter browsers with exe only
	for (Iterator<Properties> it = knownBrowsers.values().iterator(); it.hasNext(); ) {
	    Properties p = it.next();
	    if (p.getProperty("exe")==null) it.remove();
	}
	return knownBrowsers;
    }
}
