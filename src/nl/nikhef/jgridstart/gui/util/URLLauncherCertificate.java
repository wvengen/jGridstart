package nl.nikhef.jgridstart.gui.util;

import java.awt.Component;
import java.net.URL;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.CertificateAction;
import nl.nikhef.jgridstart.install.BrowserFactory;

/** URL Handler uses an external web browser specified by a {@linkplain CertificatePair}.
 * <p>
 * This version tracks the currently selected certificate, just like {@link CertificateAction},
 * and opens the web browser where this certificate was installed to. If this wasn't specified,
 * it runs the default web browser.
 * <p>
 * This only works after a {@link CertificateSelection} is made known, of course.
 * <p>
 * TODO should move to factory pattern, since openUrl is duplicated now
 * 
 * @author wvengen
 */
public class URLLauncherCertificate extends URLLauncher {
    
    private static SelectionTracker tracker = null;
    
    private final static String BROWSER_PROPERTY = "install.browser";

    protected static void runBrowser(String location, Component parent) {
	try {
	    if (tracker!=null && tracker.getCertificatePair()!=null) {
		CertificatePair cert = tracker.getCertificatePair();
		if (cert.getProperty(BROWSER_PROPERTY)!=null) {
		    BrowserFactory.getInstance().openUrl(cert.getProperty(BROWSER_PROPERTY), location);
		    return;
		}
	    }
	    // just open default browser
	    BrowserFactory.getInstance().openUrl(location);
	} catch (Exception e) {
	    ErrorMessage.error(parent, "Could not open web page", e, location);
	}
    }

    public static void openURL(String surl) {
	openURL(surl, null);
    }
    public static void openURL(URL url) {
	openURL(url, null);
    }
    public static void openURL(URL url, Component parent) {
	openURL(url.toExternalForm(), parent);
    }
    public static void openURL(String surl, Component parent) {
	if (surl.startsWith("action:"))
	    performAction(surl.substring(7), parent);
	else
	    runBrowser(surl, parent);
    }

    /** Bind a {@linkplain CertificateSelection} so that the currently selected
     * certificate can be tracked. */
    public static void setSelectionSource(CertificateSelection sel) {
	tracker = new SelectionTracker(sel);
    }
    
    private static class SelectionTracker implements ListSelectionListener {
	
	/** currently selected certificate */
	private CertificatePair certificatePair = null;
	/** selection source */
	private CertificateSelection selection = null;
	
	public SelectionTracker(CertificateSelection s) {
	    selection = s;
	    s.addListSelectionListener(this);
	}
	
	public void valueChanged(ListSelectionEvent e) {
	    // only operate at the end of a stream of selection events to avoid flickering
	    if (e.getValueIsAdjusting()) return;
	    // remove itemlistener for previous pair
	    certificatePair = selection.getCertificatePair();
	}
	
	public CertificatePair getCertificatePair() {
	    return certificatePair;
	}
    }
}
