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
public class BrowsersMacOSX extends BrowsersCommon {
    
    private String defaultBrowser = null;
    
    /** Location of lsregister binary */
    private static String lsregloc = null;

    @Override
    public void initialize() throws IOException {
	availableBrowsers = new HashMap<String, Properties>();
	
	// run lsregister util to obtain available browsers
	for (Iterator<Properties> it = parseSystemBrowsers(lsregister(new String[] { "-dump", "-apps" })).iterator(); it.hasNext();) {
	    Properties p = it.next();
	    // and merge in static properties based on uti
	    for (Iterator<Properties> it2 = getKnownBrowsers().values().iterator(); it2.hasNext(); ) {
		Properties known = it2.next();
		if (p.getProperty("uti").equals(known.getProperty("uti"))) {
		    for (Enumeration<?> en = known.keys(); en.hasMoreElements(); ) {
			String key = (String)en.nextElement();
			if (!p.containsKey(key)) p.setProperty(key, known.getProperty(key));			    }
		}
	    }
	    availableBrowsers.put(p.getProperty("uti"), p);
	}
		
	// detect default browser using `defaults`
	defaultBrowser = parseDefaultBrowser(defaults(new String[] {"read", "com.apple.LaunchServices"}));
	
	// fallback to Safari as default browser
	if (defaultBrowser==null) defaultBrowser = "com.apple.safari";
    }

    @Override
    public String getDefaultBrowser() {
	return defaultBrowser;
    }

    @Override
    public void openUrl(String browserid, String urlString)
	    throws BrowserNotAvailableException, BrowserExecutionException {
	try {
	    FileUtils.Exec(new String[] { "open", "-b", browserid, urlString});
	} catch (IOException e) {
	    throw new BrowserExecutionException(browserid, e);
	}
    }

    @Override
    protected void installPKCS12System(String browserid, File pkcs)
	    throws BrowserExecutionException {
	try {
	    FileUtils.Exec(new String[] { "open", pkcs.toURI().toASCIIString()});
	} catch (IOException e) {
	    throw new BrowserExecutionException(browserid, e);
	}
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
    protected ArrayList<Properties> parseSystemBrowsers(String lsregister) {
	ArrayList<Properties> browsers = new ArrayList<Properties>();
	
	String[] lines = lsregister.split("\\n");
	Properties p = new Properties();
	Pattern pBundle = Pattern.compile("^\\s*bundle\\s+id:\\s*(\\d+)\\s*$");
	Pattern pName = Pattern.compile("^\\s*name:\\s*(.*?)\\s*$");
	Pattern pCanId = Pattern.compile("^\\s*canonical id:\\s*(.*?)\\s*\\(0x[0-9a-fA-F]+\\)\\s*$");
	Pattern pId = Pattern.compile("^\\s*identifier:\\s*(.*?)\\s*\\(0x[0-9a-fA-F]+\\)\\s*$");
	Pattern pBindings = Pattern.compile("^\\s*bindings:\\s*(.*?)\\s*$");
	for (int i=0; i<lines.length; i++) {
	    // name:
	    Matcher mName = pName.matcher(lines[i]);
	    if (mName.matches() && p.getProperty("desc")==null) {
		p.setProperty("desc", mName.group(1));
		continue;
	    }
	    // canonical id:
	    Matcher mCanId = pCanId.matcher(lines[i]);
	    if (mCanId.matches() && p.getProperty("uti.canonical")==null) {
		p.setProperty("uti.canonical", mCanId.group(1));
		continue;
	    }
	    // id:
	    Matcher mId = pId.matcher(lines[i]);
	    if (mId.matches() && p.getProperty("uti")==null) {
		p.setProperty("uti", mId.group(1));
		continue;
	    }
	    // bindings: ; require http: or https: for the web browser
	    Matcher mBindings = pBindings.matcher(lines[i]);
	    if (mBindings.matches()) {
		String[] bindings = mBindings.group(1).split(",\\s*");
		for (int j=0; j<bindings.length; j++) {
		    if (bindings[j].equals("http:") || bindings[j].equals("https:")) {
			p.setProperty("__ok__", Boolean.toString(true));
		    }
		}
		continue;
	    }
	    // bundle id:
	    Matcher mBundle = pBundle.matcher(lines[i]);
	    if (mBundle.matches()) {
		// process old one
		if (Boolean.valueOf(p.getProperty("__ok__"))) {
		    // post-process properties
		    // Sometimes the uti is ambiguous (e.g. case; safari vs. Safari),
		    // so the optional canonical id is preferred.
		    p.remove("__ok__");
		    if (p.getProperty("uti.canonical")!=null) {
			p.setProperty("uti", p.getProperty("uti.canonical"));
			p.remove("uti.canonical");
		    }
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
