package nl.nikhef.jgridstart.install;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import at.jta.Key;
import at.jta.RegistryErrorException;
import at.jta.Regor;

import nl.nikhef.jgridstart.install.exception.BrowserExecutionException;
import nl.nikhef.jgridstart.install.exception.BrowserNotAvailableException;
import nl.nikhef.jgridstart.util.FileUtils;

/** Windows implementation of browser discovery and launch */
class BrowsersWindows extends BrowsersCommon {
    
    /** the detected default browser name */
    private String defaultBrowser = null;
    /** cache object for registry access */
    private Regor regor = null;

    @Override @SuppressWarnings("unchecked") // for clone() cast
    public void initialize() throws IOException {
	availableBrowsers = (HashMap<String, Properties>)readKnownBrowsers().clone();
	// and keep only browsers that can be found in either registry,
	// PATH, or inside a subdir of "Program Files".
	for (Iterator<Properties> it = availableBrowsers.values().iterator(); it.hasNext(); ) {
	    Properties p = it.next();
	    // keep only browsers that have an executable name
	    String exe = p.getProperty("winexe");
	    if (exe==null) exe = p.getProperty("exe");
	    if (exe==null) {
		it.remove();
		continue;
	    }

	    // try to find the executable: registry, PATH, subdir of "Program Files"
	    String path = null;
	    path=findBrowserRegistry1(exe);
	    if (path==null) path = findBrowserRegistry2(exe);
	    if (path==null) path = findBrowserPath(exe);
	    //TODO if (path==null) path = findBrowserProgramFiles(exe);
	    // not found, remove
	    if (path==null) {
		it.remove();
		continue;
	    }
	    // set path to full executable
	    p.setProperty("exe", new File(path).getCanonicalPath());

	    logger.fine("found browser "+exe+" at: "+path);
	}
	
	// get default browser from registry or Java
	String defaultExe = findDefaultBrowserRegistry();
	if (defaultExe == null)
	    defaultExe = findDefaultBrowserJava();

	if (defaultExe!=null) {
	    defaultExe = new File(defaultExe).getCanonicalPath();
	    logger.fine("default browser path: "+defaultBrowser);
	    for (Iterator<Entry<String, Properties>> it = availableBrowsers.entrySet().iterator(); it.hasNext(); ) {
		Entry<String, Properties> e = it.next();
		Properties p = e.getValue();
		if (defaultExe.toLowerCase().equals(p.getProperty("exe").toLowerCase())) {
		    defaultBrowser = e.getKey();
		    logger.fine("default browser name: "+defaultBrowser);
		    break;
		}
	    }
	}
	
	// free registry object since we're done
	regor = null;
    }

    @Override
    public String getDefaultBrowser() {
	return defaultBrowser;
    }

    @Override
    public void openUrl(String browserid, String urlString)
	    throws BrowserNotAvailableException, BrowserExecutionException {
	
	String[] cmd = null;
	
	if (browserid!=null) {
	    // check if browserid is present
	    if (!availableBrowsers.containsKey(browserid))
		throw new BrowserNotAvailableException(browserid);

	    // run the command
	    cmd = new String[] {
		    availableBrowsers.get(browserid).getProperty("exe"),
		    urlString
	    };
	    
	} else {
	    // run default browser if no browser specified
	    cmd = new String[] { "rundll32", "url.dll,FileProtocolHandler", urlString };
	}

	try {
	    // execute browser
	    //   don't wait for this, since starting a new browser when the
	    //   process isn't running yet can take a loooong time
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
    protected void installPKCS12System(String browserid, File pkcs)
	    throws BrowserExecutionException {
	
	// run the command
	String[] cmd = new String[] {
		"cmd", "/Q", "/C",
		"start",
		pkcs.getAbsolutePath()
	};
	try {
	    StringBuffer output = new StringBuffer();
	    if ( FileUtils.Exec(cmd, null, output) != 0)
		throw new BrowserExecutionException(browserid, output.toString());
	} catch (IOException e) {
	    throw new BrowserExecutionException(browserid, e);
	}
    }
    
    /** Find a browser in the registry's list of executables, version 1.
     * <p>
     * {@literal HKEY_LOCAL_MACHINE\Software\Microsoft\Windows\CurrentVersion\App Paths}
     * is being looked in for full application paths.
     * 
     * @param exename name of executable, case-insensitive, without ".exe"
     * @return full path if found, null if not found.
     */
    private String findBrowserRegistry1(String exename) {
	try {
	    String canonExename = exename.toLowerCase()+".exe";
	    final Key root = Regor.HKEY_LOCAL_MACHINE;
	    final String appPaths = "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths";
	    
	    for (Iterator<?> it = getRegor().listKeys(root, appPaths).iterator(); it.hasNext(); ) {
		String key = (String)it.next();
		if (key.toLowerCase().equals(canonExename)) {
		    // folder is equal to executable name, return full path inside
		    String fullPathKey = appPaths+"\\"+key;
		    Key pathKey = getRegor().openKey(root, fullPathKey, Regor.KEY_READ);
		    if (pathKey==null) continue;
		    byte[] fullPath = getRegor().readValue(pathKey, null);
		    getRegor().closeKey(pathKey);
		    if (fullPath==null) continue;
		    return Regor.parseValue(fullPath);
		}
	    }
	} catch (RegistryErrorException e) { }
	// failed
	return null;
    }
    /** Find a browser in the registry's list of executables, version 2.
     * <p>
     * {@literal HKEY_CLASSES_ROOT\Applications}
     * is being looked in for open commands.
     * 
     * @param exename name of executable, case-insensitive, without ".exe"
     * @return full path if found, null if not found.
     */
    private String findBrowserRegistry2(String exename) {
	String canonExename = exename.toLowerCase()+".exe";
	return findRegistryClassesRoot("Applications\\"+canonExename);
    }
    
    /** Find a browser in the current PATH
     * <p>
     * TODO make it work when a ";" is inside quotes in a path. Most Java code I've
     * seen doesn't do this but it is needed.
     * 
     * @param exename name of executable, case-insensitive, without ".exe"
     * @return the browser executable if found, null if not found
     */
    private String findBrowserPath(String exename) {
	String canonExename = exename.toLowerCase()+".exe";
	String path = System.getenv("PATH");
	if (path==null) return null;
	for (StringTokenizer t = new StringTokenizer(path, File.pathSeparator); t.hasMoreTokens(); ) {
	    String e = t.nextToken();
	    if (e.startsWith("\"") && e.endsWith("\""))
		e = e.substring(1, e.length()-1);
	    if (new File(e, canonExename).exists())
		return canonExename;
	}
	// not found
	return null;
    }
    
    /** Find the default browser from the registry.
     * <p>
     * This looks at {@code HKEY_CLASSES_ROOT\http[s]}
     * 
     * @return default browser executable, or null if not specified.
     */
    private String findDefaultBrowserRegistry() {
	String b;
	if ( (b=findRegistryClassesRoot("https")) !=null ) return b;
	if ( (b=findRegistryClassesRoot("http")) !=null ) return b;
	// failed
	return null;
    }
    /** Return browser from the registry for {@literal HKEY_CLASSES_ROOT} entry. */
    private String findRegistryClassesRoot(String path) {
	try {
	    final Key root = Regor.HKEY_CLASSES_ROOT;
	    final String subPath = "shell\\open\\command";
	    Key key = getRegor().openKey(root, path+"\\"+subPath, Regor.KEY_READ);
	    if (key==null) return null;
	    byte[] value = getRegor().readValue(key, null);
	    getRegor().closeKey(key);
	    if (value==null) return null;
	    // now we need to get the first argument, taking care of quotes
	    // TODO this discards any extra arguments!
	    String cmd = Regor.parseValue(value).trim();
	    if (cmd.startsWith("\"")) {
		int idx;
		for (idx=1; idx<cmd.length(); idx++)
		    if (cmd.charAt(idx)=='"' && cmd.charAt(idx-1)!='\\') break;
		if (idx<1) return null;
		cmd = cmd.substring(1, idx);
		return cmd;
	    }
	    // no quotes, just return all before first whitespace
	    return cmd.split("\\s", 2)[0];
	} catch (RegistryErrorException e) { }
	// failed
	return null;
    }
    
    /** Return a Regor instance, cache as well 
     * @throws RegistryErrorException */
    private Regor getRegor() throws RegistryErrorException {
	if (regor==null)
	    regor = new Regor();
	return regor;
    }
}
