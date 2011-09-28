package nl.nikhef.jgridstart.ca.dutchgrid;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.Organisation;
import nl.nikhef.jgridstart.gui.wizard.ITemplateWizardPage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;
import nl.nikhef.jgridstart.gui.wizard.TemplateWizard;

public class PageIdentityVetting extends RequestWizardPage {
    
    public PageIdentityVetting() throws ParserConfigurationException, SAXException, IOException {
	super("identity_vetting", PageIdentityVetting.class.getResource("requestwizard-03.html"));
    }

    @Override
    public boolean isDone() {
	return Boolean.valueOf(data().getProperty("request.processed")) ||
		Boolean.valueOf(data().getProperty("cert"));
    }

    @Override
    public void pageEnter(ITemplateWizardPage oldPage) {
	super.pageEnter(oldPage);
	// TODO hack
	CertificatePair cert = getWizard().getCertificate();
	// cannot continue past this without certificate
	boolean certPresent = cert!=null ? Boolean.valueOf(cert.getProperty("cert")) : false;
	boolean certReady = certPresent || (cert!=null && Boolean.valueOf(cert.getProperty("request.processed")));
	getWizard().setButtonEnabled(TemplateWizard.BUTTON_NEXT, certReady);
	if (cert!=null) getWizard().setSystemAffected(true);
	// retrieve RA info
	if (data().getProperty("org.ras")==null) {
	    Organisation org = Organisation.get(data().getProperty("org"));
	    if (org!=null)
		org.copyTo(data(), "org.");
	}
    }
}
