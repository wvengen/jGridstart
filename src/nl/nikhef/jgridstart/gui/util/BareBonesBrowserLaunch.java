/////////////////////////////////////////////////////////
//  Bare Bones Browser Launch                          //
//  Version 1.5.99.2 (April 6, 2009)                   //
//  By Dem Pilafian and Willem van Engen               //
//  Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
//  Example Usage:                                     //
//     String url = "http://www.centerkey.com/";       //
//     BareBonesBrowserLaunch.openURL(url);            //
//  Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////

package nl.nikhef.jgridstart.gui.util;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

public class BareBonesBrowserLaunch {

    private static final String errMsg = "Error attempting to launch web browser";
    private static Logger logger = Logger.getLogger("nl.nikhef.jgridstart");

    public static void openURL(URL url) {
	openURL(url.toExternalForm());
    }

    @SuppressWarnings("unchecked") // to support older java compilers
    public static void openURL(String url) {
	// Try java desktop API first (new in Java 1.6)
	// basically: java.awt.Desktop.getDesktop().browse(new URI(url));
	try {
	    Class desktop = Class.forName("java.awt.Desktop");
	    Method getDesktop = desktop.getDeclaredMethod("getDesktop", new Class[] {});
	    Object desktopInstance = getDesktop.invoke(null, new Object[] {});
	    Method browse = desktop.getDeclaredMethod("browse", new Class[] {URI.class});
	    URI uri = new URI(url);
	    logger.fine("Using Java Desktop API to open URL '"+url+"'");
	    browse.invoke(desktopInstance, new Object[] {uri});
	    return;
	} catch(Exception e) { }
	
	// Failed, resort to executing the browser manually
	String osName = System.getProperty("os.name");
	try {
	    if (osName.startsWith("Mac OS")) {
		Class fileMgr = Class.forName("com.apple.eio.FileManager");
		Method openURL = fileMgr.getDeclaredMethod("openURL",
			new Class[] {String.class});
		logger.fine("Using "+fileMgr+" to open URL '"+url+"'");
		openURL.invoke(null, new Object[] {url});
	    }
	    else if (osName.startsWith("Windows")) {
		String cmd = "rundll32 url.dll,FileProtocolHandler " + url;
		logger.fine("Executing: "+cmd);
		Runtime.getRuntime().exec(cmd);
	    } else { //assume Unix or Linux
		String[] browsers = {
			// Freedesktop, http://portland.freedesktop.org/xdg-utils-1.0/xdg-open.html
			"xdg-open",
			// Debian
			"sensible-browser",
			// Otherwise call browsers directly
			"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
		String browser = null;
		for (int count = 0; count < browsers.length && browser == null; count++)
		    if (Runtime.getRuntime().exec(
			    new String[] {"which", browsers[count]}).waitFor() == 0)
			browser = browsers[count];
		if (browser == null) {
		    logger.warning("No web browser found");
		    throw new Exception("Could not find web browser");
		} else {
		    logger.fine("Executing: "+browser+" "+url);
		    Runtime.getRuntime().exec(new String[] {browser, url});
		}
	    }
	}
	catch (Exception e) {
	    JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
	}
    }

}
