package nl.nikhef.jgridstart.ca.confusa;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateRequest;
import nl.nikhef.jgridstart.gui.wizard.ITemplateWizardPage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;
import nl.nikhef.jgridstart.gui.wizard.ValidationException;

/** Request wizard page: user details */
public class PageDetailsUser extends RequestWizardPage {

    public PageDetailsUser() throws ParserConfigurationException, SAXException, IOException {
	super("userdetails", PageDetailsUser.class.getResource("details_user.html"));
    }
    
    @Override
    public boolean isDone() {
	return getWizard().getCertificate()!=null;
    }
    
    @Override
    public void pageEnter(ITemplateWizardPage oldPage) {
	// make sure to keep passwords safe
	data().setProperty("password1.volatile", "true");
	data().setProperty("password2.volatile", "true");
	// lock password fields when key is present
	//   also enter dummy password to show user that is was set
	CertificatePair cert = getWizard().getCertificate();
	if (cert!=null) {
	    data().setProperty("password1", "dummy");
	    data().setProperty("password1.lock.volatile", "true");
	    data().setProperty("password1.lock", "true");
	    data().setProperty("password2", "dummy");
	    data().setProperty("password2.lock.volatile", "true");
	    data().setProperty("password2.lock", "true");
	}
    }

    @Override
    public void validate() throws ValidationException {
	
	// if certificate or request is present, we shouldn't check anymore
	if (getWizard().getCertificate()!=null) return;
	
	// make sure passwords are equal
	if (data().getProperty("password1")!=null &&
		!data().getProperty("password1").equals(data().getProperty("password2")))
	    throw new ValidationException("Passwords don't match, please make sure they are equal.");

	// make sure password is according to policy. Strict policy check first,
	try {
	    if (data().getProperty("password1")==null)
		throw new InvalidKeyException("Please supply a password");
	    CertificateRequest.validatePassword(data().getProperty("password1"), true);
	} catch (InvalidKeyException e) {
	    throw new ValidationException("The password must conform to the policy:\n"+e.getLocalizedMessage());
	}
	// and then the suggested policy check
	try {
	    CertificateRequest.validatePassword(data().getProperty("password1"), false);
	} catch (InvalidKeyException e) {
	    String msg = System.getProperty("jgridstart.password.explanation");
	    if (msg==null) msg = e.getLocalizedMessage();
	    int ret = JOptionPane.showConfirmDialog(getParent(),
		    "Password does not conform to the policy.\n"+
		    "Are you sure you want to use this password?\n"+msg,
		    "Password too simple", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
	    if (ret != JOptionPane.OK_OPTION)
		throw new ValidationException(null); // cancel without error message
	}
    }
}
