package nl.nikhef.jgridstart.ca.confusa;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.wizard.ITemplateWizardPage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;
import nl.nikhef.jgridstart.gui.wizard.TemplateWizard;
import nl.nikhef.jgridstart.gui.wizard.TemplateWizardWorker;
import nl.nikhef.jgridstart.passwordcache.PasswordCancelledException;

public class PageGenerateSubmit extends RequestWizardPage {
    
    /** Worker thread that does generation and submission */
    private GenerateWorker worker = null;
    
    public PageGenerateSubmit() throws ParserConfigurationException, SAXException, IOException {
	super("generate_submit", PageGenerateSubmit.class.getResource("generate_submit.html"));
    }

    @Override
    public boolean isDone() {
	return (Boolean.valueOf(data().getProperty("request.submitted")) ||
		Boolean.valueOf(data().getProperty("cert")));
    }
    
    @Override
    public void pageEnter(ITemplateWizardPage oldPage) {
	// if the next button becomes enabled, just move to next page
	//if (!getWizard().getButtonEnabled(TemplateWizard.BUTTON_NEXT) && isDone())
	//    getWizard().setPageRelative(1);
	// enable button only after certificate was submitted
	getWizard().setButtonEnabled(TemplateWizard.BUTTON_NEXT, isDone());
	// start worker thread
	if (worker==null) { 
	    worker = new GenerateWorker(this);
	    worker.execute();
	}
    }
    
    @Override
    public boolean pageLeave(ITemplateWizardPage newPage, boolean isNext) {
	// don't do anything yet if we return to the same page
	if (newPage==this) return true;
	// stop worker when running
	if (worker!=null) {
	    worker.cancel(true);
	    worker = null;
	}
	return true;
    }
    
    /** worker thread for generation of a certificate */
    protected class GenerateWorker extends TemplateWizardWorker<Void> {
	protected Properties p;
	/** Newly created certificate */
	CertificatePair newCert = null;

	public GenerateWorker(ITemplateWizardPage page) {
	    super(page);
	    this.p = data();
	}
	
	/** Worker thread that generates the certificate and submits it */
	@Override
	protected Void doWorkInBackground() throws Exception {
	    // Generate a keypair and certificate signing request
	    try {
		CertificateStore store = getWizard().getStore();
		CertificatePair cert = getWizard().getCertificate();
		
		// generate request when no key or certificate
		if (cert==null) {
		    // generate request
		    // TODO check thread safety ... :/
		    assert(p.getProperty("subject")!=null);
		    newCert = store.generateRequest(p, p.getProperty("password1").toCharArray());
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
	    } catch (PasswordCancelledException e) {
		// special state to go to the previous page
		publish("state.cancelled");
	    }
	    return null;
	}

	@Override
	protected void process(String key) {
	    // select certificate in main view on creation
	    if (key.equals("state.certificate_created")) {
		assert(newCert!=null);
		getWizard().setCertificate(newCert);
		CertificateSelection selection = getWizard().getSelection();

		if (selection!=null) {
		    selection.setSelection(newCert);
		}
		getWizard().setData(newCert);
	    } else {
		super.process(key);
	    }
	}
    }
}
