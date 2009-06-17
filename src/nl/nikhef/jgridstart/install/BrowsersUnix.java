package nl.nikhef.jgridstart.install;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import nl.nikhef.jgridstart.install.exception.BrowserExecutionException;
import nl.nikhef.jgridstart.install.exception.BrowserNotAvailableException;
import nl.nikhef.jgridstart.util.FileUtils;

class BrowsersUnix extends BrowsersCommon {
    
    private String defaultBrowser = null;
    
    public void initialize() throws IOException {
	availableBrowsers = readKnownBrowsers();
	// find known browsers, keep only which are in path
	for (Iterator<Entry<String, Properties>> it = availableBrowsers.entrySet().iterator(); it.hasNext(); ) {
	    Entry<String, Properties> entry = it.next();
	    Properties p = entry.getValue();
	    // we need an exe property
	    if (p.getProperty("exe")!=null) {
		// find using which; process spawning in unix is cheap
		try {
		    String[] cmd = new String[] { "which", p.getProperty("exe") };
		    int ret = Runtime.getRuntime().exec(cmd).waitFor();
		    if (ret==0) continue;
		} catch (InterruptedException e) { }
	    }
	    // error or not found, remove from list
	    it.remove();
	}
	
	// then find default browser
	defaultBrowser = findDefaultBrowserEnvironment();
	if (defaultBrowser==null)
	    defaultBrowser = findDefaultBrowserGConf();
	// we only want the basename
	if (defaultBrowser!=null)
	    defaultBrowser = new File(defaultBrowser).getName();
    }
    
    /** Get default browser from environment $BROWSER, or null if unset. */
    private String findDefaultBrowserEnvironment() {
	String browser = System.getenv("BROWSER");
	if (browser==null) return null;
	return normaliseBrowserPath(browser);
    }
    
    /** Normalise the browser path.
     * <p>
     * Currently this follows the symlink if its name is x-www-browser, so
     * that we find the actual browser pointed to by Debian's alternatives.
     */
    private String normaliseBrowserPath(String path) {
	if (path.equals("x-www-browser") || path.endsWith("/x-www-browser")) {
	    try {
		String link1;
		link1 = readLink(path);
		if (link1==null) return path;
		String link2 = readLink(link1);
		if (link2==null) return link1;
	    } catch(IOException e) {
		return path;
	    }
	}
	return path;
    }
    /** Unix readlink command, returns null it path was no symlink. 
     * @throws IOException */
    private String readLink(String path) throws IOException {
	String output = "";
	if (FileUtils.Exec(new String[] { "readlink", path }, output)!=0)
	    return null;
	return output.trim();
    }
    
    /** Get default browser from GConf setting */
    private String findDefaultBrowserGConf() {
	// make sure it is enabled
	if (!Boolean.valueOf(getGConfValue("/desktop/gnome/url-handlers/https/enabled")))
	    return null;
	// get value
	// TODO keep command-line arguments ... !
	String cmd = getGConfValue("/desktop/gnome/url-handlers/https/command");
	if (cmd==null) return null;
	return cmd.split("\\s", 2)[0];
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
	return output.toString().trim();
    }

    public String getDefaultBrowser() {
	return defaultBrowser;
    }

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
	    FileUtils.Exec(cmd);
	} catch (IOException e) {
	    throw new BrowserExecutionException(browserid, e);
	}
    }

    @Override
    protected void installPKCS12System(String browserid, File pkcs) throws BrowserExecutionException {
	throw new BrowserExecutionException(browserid,
		"There is no default certificate store, please install the certificate " +
		"manually in your browser.");
    }
}
