package nl.nikhef.jgridstart.ca.dutchgrid;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.swingworker.SwingWorker;
import org.xml.sax.SAXException;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateRequest;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.wizard.ITemplateWizardPage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;
import nl.nikhef.jgridstart.gui.wizard.TemplateWizard;
import nl.nikhef.jgridstart.passwordcache.PasswordCache;
import nl.nikhef.jgridstart.passwordcache.PasswordCancelledException;

public class PageGenerateSubmit extends RequestWizardPage {
    
    /** Worker thread that does generation and submission */
    private GenerateWorker worker = null;
    
    public PageGenerateSubmit() throws ParserConfigurationException, SAXException, IOException {
	super("generate_submit", PageGenerateSubmit.class.getResource("requestwizard-02.html"));
    }

    @Override
    public boolean isDone() {
	return (Boolean.valueOf(data().getProperty("request.submitted")) ||
		Boolean.valueOf(data().getProperty("cert")));
    }
    
    @Override
    public void pageEnter(ITemplateWizardPage oldPage) {
	super.pageEnter(oldPage);
	// enable button only after certificate was generated
	getWizard().setButtonEnabled(TemplateWizard.BUTTON_NEXT,
		Boolean.valueOf(data().getProperty("cert")) ||
		Boolean.valueOf(data().getProperty("request")));
	// start worker thread
	if (worker==null) { 
	    worker = new GenerateWorker();
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
	return true;
    }
    
    /** worker thread for generation of a certificate */
    protected class GenerateWorker extends SwingWorker<Void, String> {
	protected Properties p;
	/** exception from background thread */
	protected Throwable e = null;
	/** Newly created certificate */
	CertificatePair newCert = null;

	public GenerateWorker() {
	    super();
	    this.p = data();
	}
	
	/** Worker thread that generates the certificate and submits it */
	@Override
	protected Void doInBackground() throws Exception {
	    // Generate a keypair and certificate signing request
	    // TODO verify concurrency 
	    // TODO make this configurable
	    try {
		CertificateStore store = getWizard().getStore();
		CertificatePair cert = getWizard().getCertificate();
		CertificatePair certParent = getWizard().getParentCertificate();
		
		// generate request when no key or certificate
		if (cert==null) {
		    // generate request
		    // TODO check thread safety ... :/
		    if (!Boolean.valueOf(p.getProperty("renewal"))) {
			CertificateRequest.postFillData(p);
			newCert = store.generateRequest(p, p.getProperty("password1").toCharArray());
		    } else {
			p.setProperty("subject", certParent.getProperty("subject"));
			newCert = store.generateRenewal(certParent, p.getProperty("password1").toCharArray());
		    }
		    // clear password
		    p.remove("password1");
		    p.remove("password1.volatile");
		    p.remove("password2");
		    p.remove("password2.volatile");
		    // copy properties to certificate pair
		    for (Enumeration<?> en = p.propertyNames(); en.hasMoreElements(); ) {
			String key = (String)en.nextElement();
			if (key!=null && p.getProperty(key)!=null)
			    newCert.setProperty(key, p.getProperty(key));
		    }
		    // and make sure data is saved
		    newCert.store();
		    cert = newCert;
		    publish("state.certificate_created");
		}
		// upload request only if no certificate present yet
		if (cert.getCertificate()==null && !Boolean.valueOf(cert.getProperty("request.processed"))) {
		    // upload request if it hasn't been done
		    if (!Boolean.valueOf(cert.getProperty("request.submitted"))) {
			cert.uploadRequest();
			publish("state.certificate_uploaded");
		    }
		}
		publish("state.done");
	    } catch (PasswordCancelledException e) {
		// special state to go to the previous page
		publish("state.cancelled");
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
			// show error message in pane
			ErrorMessage.logException(e);
			if (e.getLocalizedMessage()!=null && !e.getLocalizedMessage().equals("") && !e.getLocalizedMessage().equals("null"))
			    data().setProperty("wizard.error", e.getLocalizedMessage());
			else
			    data().setProperty("wizard.error", "Unknown error. Please go back and try again.");
			data().setProperty("wizard.error.volatile", "true");
			getWizard().refresh();
		    }
		    return;
		}
		// select certificate in main view on creation
		if (key.equals("state.certificate_created")) {
		    assert(newCert!=null);
		    getWizard().setCertificate(newCert);
		    CertificateSelection selection = getWizard().getSelection();
		    
		    CertificateRequest.postFillDataLock(newCert);
		    if (selection!=null) {
			selection.setSelection(newCert);
		    }
		    setData(newCert);
		}
	    }
	    // update content pane
	    getWizard().refresh();
	}
    }
}
