package nl.nikhef.jgridstart.install;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.nikhef.jgridstart.install.exception.BrowserExecutionException;
import nl.nikhef.jgridstart.install.exception.BrowserNotAvailableException;
import nl.nikhef.jgridstart.util.FileUtils;

/** Mac OS X implementation of browser discovery and launch */
class BrowsersMacOSX extends BrowsersCommon {
    
    private String defaultBrowser = null;
    
    /** Location of lsregister binary */
    private static String lsregloc = null;
    
    /** Blacklisted uti's.
     * <p>
     * Some browsers appear as a system browser but they are not really
     * a generic web browser. Any uti in this list is removed from the list
     * of discovered browsers.
     */
    private final static String[] blacklist = new String[] {
	    "com.realnetworks.realplayer"
    };

    @Override
    public void initialize() throws IOException {
	availableBrowsers = new HashMap<String, Properties>();
	
	// run lsregister util to obtain available browsers
	for (Iterator<Properties> it = parseSystemBrowsers(getRegisterDump()).iterator(); it.hasNext();) {
	    Properties p = it.next();
	    // and merge in static properties based on uti
	    for (Iterator<Properties> it2 = getKnownBrowsers().values().iterator(); it2.hasNext(); ) {
		Properties known = it2.next();
		if (p.getProperty("uti").equals(known.getProperty("uti"))) {
		    for (Enumeration<?> en = known.keys(); en.hasMoreElements(); ) {
			String key = (String)en.nextElement();
			if (!p.containsKey(key)) p.setProperty(key, known.getProperty(key));
		    }
		    logger.fine("found browser "+p.getProperty("uti"));
		}
	    }
	    availableBrowsers.put(p.getProperty("uti"), p);
	}
		
	// detect default browser using `defaults`
	defaultBrowser = parseDefaultBrowser(defaults(new String[] {"read", "com.apple.LaunchServices"}));
	
	// fallback to Safari as default browser
	if (defaultBrowser==null) {
	    defaultBrowser = "com.apple.safari";
	    logger.fine("default browser not found, falling back to: "+defaultBrowser);
	} else {
	    logger.fine("default browser: "+defaultBrowser);
	}
    }

    @Override
    public String getDefaultBrowser() {
	return defaultBrowser;
    }

    @Override
    public void openUrl(String browserid, String urlString)
	    throws BrowserNotAvailableException, BrowserExecutionException {
	try {
	    String[] cmd = new String[] { "open", "-b", browserid, urlString};
	    StringBuffer output = new StringBuffer();
	    if ( FileUtils.Exec(cmd, null, output) != 0)
		throw new BrowserExecutionException(browserid, output.toString());
	} catch (IOException e) {
	    throw new BrowserExecutionException(browserid, e);
	}
    }

    @Override
    protected void installPKCS12System(String browserid, File pkcs)
	    throws BrowserExecutionException {
	try {
	    String[] cmd = new String[] { "open", pkcs.toURI().toASCIIString()};
	    StringBuffer output = new StringBuffer();
	    if ( FileUtils.Exec(cmd, null, output) != 0)
		throw new BrowserExecutionException(browserid, output.toString());
	} catch (IOException e) {
	    throw new BrowserExecutionException(browserid, e);
	}
    }
    
    /** Return lsregister dump output. */
    protected String getRegisterDump() throws IOException {
	return lsregister(new String[] { "-dump" });
    }
    
    /** Run lsregister and return output. */
    protected String lsregister(String[] args) throws IOException {
	String[] cmd = new String[args.length+1];
	
	// Find the location of lsregister (only once)
	final String[] lsregLocs = new String[] {
		"/System/Library/Frameworks/ApplicationServices.framework/Frameworks/LaunchServices.framework/Support/lsregister",
		"/System/Library/Frameworks/CoreServices.framework/Frameworks/LaunchServices.framework/Support/lsregister",
	};
	for (int i=0; i<lsregLocs.length && lsregloc == null; i++) {
	    if (new File(lsregLocs[i]).exists())
		lsregloc = lsregLocs[i];
	}
	
	if (lsregloc==null)
	    throw new IOException("lsregister not found");
	    
	// run program and return output
	cmd[0] = lsregloc;
	System.arraycopy(args, 0, cmd, 1, args.length);
	
	StringBuffer output = new StringBuffer();
	FileUtils.Exec(cmd, null, output);
	
	return output.toString();
    }
    
    /** Parse the output of lsregister and return the system browsers */
    protected static ArrayList<Properties> parseSystemBrowsers(String lsregister) {
	ArrayList<Properties> browsers = new ArrayList<Properties>();
	
	String[] lines = lsregister.split("\\n");
	Properties p = new Properties();
	Pattern pBundle = Pattern.compile("^\\s*bundle\\s+id:\\s*(\\d+)\\s*$");
	Pattern pName = Pattern.compile("^\\s*name:\\s*(.*?)\\s*$");
	Pattern pCanId = Pattern.compile("^\\s*canonical id:\\s*(.*?)(\\s*\\(0x[0-9a-fA-F]+\\))?\\s*$");
	Pattern pId = Pattern.compile("^\\s*identifier:\\s*(.*?)(\\s*\\(0x[0-9a-fA-F]+\\))?\\s*$");
	Pattern pBindings = Pattern.compile("^\\s*bindings:\\s*(.*?)\\s*$");
	// this loop needs to go one more iteration lines.length to flush output !!!
	for (int i=0; i<=lines.length; i++) {
	    String line = "";
	    if (i<lines.length) line = lines[i];
	    // name:
	    Matcher mName = pName.matcher(line);
	    if (mName.matches() && p.getProperty("desc")==null) {
		p.setProperty("desc", mName.group(1));
		continue;
	    }
	    // canonical id:
	    Matcher mCanId = pCanId.matcher(line);
	    if (mCanId.matches() && p.getProperty("uti.canonical")==null) {
		p.setProperty("uti.canonical", mCanId.group(1));
		continue;
	    }
	    // id:
	    Matcher mId = pId.matcher(line);
	    if (mId.matches() && p.getProperty("uti")==null) {
		p.setProperty("uti", mId.group(1));
		continue;
	    }
	    // bindings: ; require http: or https: for the web browser + .html
	    Matcher mBindings = pBindings.matcher(line);
	    if (mBindings.matches()) {
		String[] bindings = mBindings.group(1).split(",\\s*");
		for (int j=0; j<bindings.length; j++) {
		    if (bindings[j].equals("http:") || bindings[j].equals("https:")) {
			p.setProperty("__supports_http", Boolean.toString(true));
		    } else if (bindings[j].equals(".html")) {
			p.setProperty("__supports_html", Boolean.toString(true));
		    }
		}
		continue;
	    }
	    // bundle id:
	    Matcher mBundle = pBundle.matcher(line);
	    if (mBundle.matches() || i==lines.length) {
		// process old one
		if (Boolean.valueOf(p.getProperty("__supports_http")) &&
			Boolean.valueOf(p.getProperty("__supports_html"))) {
		    // post-process properties
		    // Sometimes the uti is ambiguous (e.g. case; safari vs. Safari),
		    // so the optional canonical id is preferred.
		    p.remove("__supports_http");
		    p.remove("__supports_html");
		    if (p.getProperty("uti.canonical")!=null) {
			p.setProperty("uti", p.getProperty("uti.canonical"));
			p.remove("uti.canonical");
		    }
		    // older Mac OS X versions don't have canonical id's, we need to compare anyway
		    p.setProperty("uti", p.getProperty("uti").toLowerCase());
		    // don't add browsers found at multiple places, use only one
		    boolean addOk = true;
		    for (Iterator<Properties> it = browsers.iterator(); it.hasNext(); ) {
			if (p.getProperty("uti").equals(it.next().getProperty("uti"))) {
			    addOk = false;
			    break;
			}
		    }
		    // don't add blacklisted browsers 
		    for (int j=0; j<blacklist.length; j++) {
			if (blacklist[j].equals(p.getProperty("uti"))) {
			    addOk = false;
			    break;
			}
		    }
		    
		    if (addOk)
			browsers.add(p);
		}
		// and start a new application entry
		p = new Properties();
		continue;
	    }
	}
	
	return browsers;	
    }
    
    /** Run "defaults" and return output. */
    protected String defaults(String[] args) throws IOException {
	StringBuffer dfloutput = new StringBuffer();
	FileUtils.Exec(new String[] { "defaults", "read", "com.apple.LaunchServices" }, null, dfloutput);
	return dfloutput.toString();
    }
    
    /** Parse the output of defaults and return the default browser's uti.
     * <p>
     * Returns first match for http/https.
     * TODO more intelligently parse the output.
     * 
     * @return default browser's uti, or null if none set. */
    protected String parseDefaultBrowser(String prefs) {
	String defaultBrowser = null;
	Pattern pDefault = Pattern.compile(".*\\x7b(.*?)\\s*LSHandlerURLScheme\\s*=\\s*https?\\s*;\\s*(.*?)\\x7d.*", Pattern.MULTILINE|Pattern.DOTALL);
	Pattern pRole = Pattern.compile("^\\s*LSHandlerRole(All|Viewer)\\s*=\\s*\"(.*?)\"\\s*;", Pattern.MULTILINE);
	Matcher mDefault = pDefault.matcher(prefs);
	if (mDefault.matches()) {
	    String info = mDefault.group(1)+mDefault.group(2);
	    Matcher mRole = pRole.matcher(info);
	    if (mRole.matches()) {
		defaultBrowser = mRole.group(2);
	    }
	}	
	return defaultBrowser;
    }
    
    @Override
    protected HashMap<String, Properties> readKnownBrowsers() throws IOException {
	HashMap<String, Properties> browsers = super.readKnownBrowsers();
	knownBrowsers = new HashMap<String, Properties>();
	// use uti as primary key
	for (Iterator<Properties> it = browsers.values().iterator(); it.hasNext(); ) {
	    Properties p = it.next();
	    if (p.getProperty("uti")==null) continue;
	    knownBrowsers.put(p.getProperty("uti"), p);
	}
	return knownBrowsers;
    }
}
