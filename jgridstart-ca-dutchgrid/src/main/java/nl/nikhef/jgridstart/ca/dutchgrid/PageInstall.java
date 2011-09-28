package nl.nikhef.jgridstart.ca.dutchgrid;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.swingworker.SwingWorker;
import org.xml.sax.SAXException;

import nl.nikhef.browsers.BrowserFactory;
import nl.nikhef.browsers.exception.BrowserNotAvailableException;
import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;
import nl.nikhef.jgridstart.gui.wizard.ITemplateWizardPage;
import nl.nikhef.jgridstart.passwordcache.PasswordCache;

public class PageInstall extends RequestWizardPage {
    
    /** Worker thread that downloads certificate */
    private DownloadWorker worker = null;
    
    public PageInstall() throws ParserConfigurationException, SAXException, IOException {
	super("install", PageInstall.class.getResource("requestwizard-04.html"));
    }

    @Override
    public boolean isDone() {
	return Boolean.valueOf(data().getProperty("install.done"));	
    }

    @Override
    public void pageEnter(ITemplateWizardPage oldPage) {
	super.pageEnter(oldPage);
	
	// make sure to keep the password safe
	data().setProperty("wizard.privkeypass.volatile", "true");
	
	// set browser properties
	try {
	    // First set system property from defaults, then retrieve data() property.
	    // data().getProperty() falls back to System if not defined, so the result
	    // is that if install.browser is set, the one specified is used, and if not,
	    // the default browser is used.
	    String dflBrowser = BrowserFactory.getInstance().getDefaultBrowser();
	    if (dflBrowser!=null)
		System.setProperty("install.browser", dflBrowser);
	    String browserid = data().getProperty("install.browser");
	    if (browserid==null) browserid = System.getProperty("install.browser");
	    // remove the old browser Properties
	    for (Enumeration<?> en = data().propertyNames(); en.hasMoreElements(); ) {
		String key = (String)en.nextElement();
		if (!key.startsWith("install.browser.")) continue;
		data().remove(key);
	    }
	    // copy the browser's Properties
	    Properties p = BrowserFactory.getInstance().getBrowserProperties(browserid);
	    for (Enumeration<?> en = p.propertyNames(); en.hasMoreElements(); ) {
		String key = (String)en.nextElement();
		data().setProperty("install.browser."+key+".volatile", "true");
		data().setProperty("install.browser."+key, p.getProperty(key));
	    }
	} catch (IOException e) {
	    // no browser info, handled in html by !${install.browser*}
	} catch (BrowserNotAvailableException e) {
	    // may happen when default browser is not recognised (though behaviour can
	    // be different for different platforms). This is no problem, since the
	    // case is handled by the html template.
	} catch (Exception e) {
	    ErrorMessage.error(getParent(), "Certificate installation problem", e);
	}

	// start worker thread to download certificate
	if (worker==null) { 
	    worker = new DownloadWorker();
	    worker.execute();
	}
    }
    
    @Override
    public boolean pageLeave(ITemplateWizardPage newPage, boolean isNext) {
	if (!super.pageLeave(newPage, isNext))
	    return false;
	// don't do anything yet if we return to the same page
	if (newPage==this) return true;
	// stop worker
	if (worker!=null) {
	    worker.cancel(true);
	    worker = null;
	}
	// clear password
	data().remove("wizard.privkeypass");
	data().remove("wizard.privkeypass.volatile");
	return true;
    }

    /** worker thread to download certificate */
    protected class DownloadWorker extends SwingWorker<Void, String> {
	protected Properties p;
	/** exception from background thread */
	protected Throwable e = null;

	public DownloadWorker() {
	    super();
	    this.p = data();
	}
	
	/** Worker thread that downloads certificate */
	@Override
	protected Void doInBackground() throws Exception {
	    // TODO verify concurrency 
	    try {
		// TODO hack
		CertificatePair cert = getWizard().getCertificate();
		if (cert==null) return null;
		
		// download certificate if not already present
		if (cert.getCertificate()==null) {
		    cert.downloadCertificate();
		}
		publish("state.done");
	    } catch (Throwable e) {
		// store exception so it can be shown to the user
		this.e = e;
		publish("state.cancelled");
	    }
	    return null;
	}

	/** process publish() event from worker thread. This updates the
	 * gui in the sense that a property is added to the TemplateWizard
	 * and it is refreshed. This gives a template the opportunity to
	 * change the display based on a property (e.g. a checkbox) */
	@Override
	protected void process(List<String> keys) {
	    // process messages
	    for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
		String key = it.next();
		if (key==null) continue;
		// process cancel
		if (key.equals("state.cancelled")) {
		    // if user cancelled, go back one step
		    if (e==null || PasswordCache.isPasswordCancelledException(e)) {
			getWizard().setPageRelative(-1);
			
		    } else {
			// either show with dialog or error message in pane
			ErrorMessage.error(getParent(), "Could not download certificate", e);
		    }
		    return;
		}
	    }
	    // update content pane
	    getWizard().refresh();
	}
    }
}
