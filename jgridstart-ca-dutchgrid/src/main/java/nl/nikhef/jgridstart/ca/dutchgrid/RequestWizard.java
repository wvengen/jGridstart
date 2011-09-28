package nl.nikhef.jgridstart.ca.dutchgrid;

import java.awt.Dialog;
import java.awt.Frame;
import java.util.Properties;

import nl.nikhef.jgridstart.CertificateRequest;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardCommon;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;
import nl.nikhef.jgridstart.gui.wizard.TemplateWizard;
import nl.nikhef.jgridstart.util.GeneralUtils;

/** Wizard that asks the user for information and generates the certificate.
 * <p>
 * The most important jGridstart user functionality is implemented here.
 * <p>
 * The wizard is based on {@link TemplateWizard}, which uses html files for
 * page contents. These are present in this package as well, called
 * <tt>requestwizard-xx.html</tt> (with <tt>xx</tt> a decimal number).
 * <p>
 * TODO this could use some improvement, also to allow easier customization 
 * by certificate authorities.
 */
public class RequestWizard extends RequestWizardCommon {
    
    protected RequestWizard(Frame parent) { super(parent); }
    protected RequestWizard(Dialog parent) { super(parent); }

    @Override
    protected void initialize() {
	super.initialize();
	// add the html pages
	try {
	    addPage(new PageDetailsUser());
	    addPage(new PageGenerateSubmit());
	    addPage(new PageIdentityVetting());
	    addPage(new PageInstall());
	    addPage(new RequestWizardPage("whatsnext", getClass().getResource("requestwizard-05.html")));
	} catch (Exception e) {
	    ErrorMessage.internal(this, e);
	}
    }
    
    @Override
    public void setData(Properties p) {
	super.setData(p);
	// also set static properties for the forms
	// initialize properties when new request / renewal
	if (cert==null) {
	    if (Boolean.valueOf(data().getProperty("renewal"))) {
		// parse fields from dn if needed
		CertificateRequest.completeData(certParent);
		CertificateRequest.preFillData(data(), certParent);
		// cannot edit fields for renewal; except email & policyagree!!!
		CertificateRequest.postFillDataLock(data());
		data().remove("email.lock");
		data().remove("agreecps.lock");
		data().setProperty("wizard.title", "Renew your certificate");
	    } else {
		// set from default properties only
		CertificateRequest.preFillData(data(), null);
		data().setProperty("wizard.title", "Request a new certificate");
	    }
	} else {
	    // existing request: parse data from DN when needed
	    CertificateRequest.completeData(cert);
	    data().setProperty("wizard.title", "Certificate request");
	}
	/** @todo might need to enable setting "true" to "true", but check first */ /* 
	// workaround for checkboxes without a name; even with checked="checked" they
	// would sometimes not be shown as checked (irregular behaviour though)
	data().setProperty("true", "true");
	data().setProperty("true.volatile", "true");
	*/
    }
}