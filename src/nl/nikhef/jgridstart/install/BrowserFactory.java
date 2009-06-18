package nl.nikhef.jgridstart.install;

import java.io.IOException;

public class BrowserFactory {
    
    protected static IBrowsers browsers = null;
    
    /** Return an instance of {@link IBrowsers} for the current platform
     * @throws IOException if initialization failed */ 
    public static IBrowsers getInstance() throws IOException {
	// singleton
	if (browsers==null) {
	    browsers = createBrowsers();
	    browsers.initialize();
	}
	return browsers;
    }
    
    private static IBrowsers createBrowsers() {
	// instantiate the correct one, depending on platform
	String osName = System.getProperty("os.name");
	if (osName.startsWith("Mac OS")) {
	    browsers = new BrowsersMacOSX();
	    return browsers;
	}
	if (osName.startsWith("Windows")) {
	    browsers = new BrowsersWindows();
	    return browsers;
	}
	// Unix or similar
	browsers = new BrowsersUnix();
	return browsers;
    }
}
