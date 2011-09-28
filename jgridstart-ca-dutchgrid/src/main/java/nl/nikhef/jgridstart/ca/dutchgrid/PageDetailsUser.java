package nl.nikhef.jgridstart.ca.dutchgrid;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateRequest;
import nl.nikhef.jgridstart.Organisation;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.wizard.ITemplateWizardPage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;
import nl.nikhef.jgridstart.gui.wizard.ValidationException;
import nl.nikhef.jgridstart.passwordcache.PEMReader;
import nl.nikhef.jgridstart.passwordcache.PasswordCache;

/** Request wizard page: user details */
public class PageDetailsUser extends RequestWizardPage {

    public PageDetailsUser() throws ParserConfigurationException, SAXException, IOException {
	super("details_user", PageDetailsUser.class.getResource("requestwizard-01.html"));
    }
    
    @Override
    public boolean isDone() {
	return getWizard().getCertificate()!=null;	
    }
    
    @Override
    public void pageEnter(ITemplateWizardPage oldPage) {
	super.pageEnter(oldPage);
	CertificatePair cert = getWizard().getCertificate();
	
	// make sure to keep passwords safe
	data().setProperty("password1.volatile", "true");
	data().setProperty("password2.volatile", "true");
	data().setProperty("wizard.parentpass.volatile", "true");
	data().setProperty("wizard.privkeypass.volatile", "true");
	// lock password fields when key is present
	//   plus fields that DN is dependent on
	//   also enter dummy password to show user that is was set
	if (cert!=null && cert.getKeyFile().exists()) {
	    data().setProperty("password1", "dummy");
	    data().setProperty("password1.lock.volatile", "true");
	    data().setProperty("password1.lock", "true");
	    data().setProperty("password2", "dummy");
	    data().setProperty("password2.lock.volatile", "true");
	    data().setProperty("password2.lock", "true");
	    data().setProperty("wizard.parentpass.lock.volatile", "true");
	    data().setProperty("wizard.parentpass.lock", "true");
	}
	// populate organisations select box
	if (!data().contains("organisations.html.options")) {
	    String orgOptions = Organisation.getAllOptionsHTML(cert);
	    if (cert==null)
		orgOptions = "<option value=''>(select your organisation)</option>"+orgOptions;
	    data().setProperty("organisations.html.options.volatile", "true");
	    data().setProperty("organisations.html.options", orgOptions);
	}
    }

    @Override
    public void validate() throws ValidationException {
	
	// TODO hack
	CertificatePair cert = getWizard().getCertificate();
	CertificatePair certParent = getWizard().getParentCertificate();
	
	// if certificate or request is present, we shouldn't check anymore
	if (cert!=null) return;
	
	// make sure passwords are equal
	if (data().getProperty("password1")!=null &&
		!data().getProperty("password1").equals(data().getProperty("password2")))
	    throw new ValidationException("Passwords don't match, please make sure they are equal.");

	    
	// make sure an organisation is selected
	if (data().getProperty("org").length()==0)
	    throw new ValidationException("Please select the organisation you're associated with.");
	
	// make sure we have a valid email address
	//  >=Java6: new InternetAddress(...)
	// we want to avoid dependency on JavaMail so do very basic check
	if (!data().getProperty("email").matches("^.+@.+\\..+"))
	    throw new ValidationException("Please enter a valid email address");

	// Not needed for renewal, since it may be that the level wasn't
	//   specified because the parent hadn't set the level explictely
	//   after an import, for example.
	if (!Boolean.valueOf(data().getProperty("renewal"))) {

	    if (data().getProperty("givenname").length()==0 ||
		    data().getProperty("surname").length()==0 )
		throw new ValidationException("Please enter your full name");

	    // verify allowed characters for name
	    try {
		CertificateRequest.validateDN(data().getProperty("givenname"));
	    } catch(Exception e) {
		throw new ValidationException("Invalid characters used in given name.");
	    }
	    try {
		CertificateRequest.validateDN(data().getProperty("surname"));
	    } catch(Exception e) {
		throw new ValidationException("Invalid characters used in surname.");
	    }

	} else {
	    // for renewal make sure private key password is ok
	    try {
		// try to read private key and decrypt
		PasswordCache.getInstance().set(
			certParent.getKeyFile().getCanonicalPath(), 
			data().getProperty("wizard.parentpass").toCharArray());
		PEMReader.readObject(certParent.getKeyFile(), "private key");
	    } catch (Exception e) {
		if (PasswordCache.isPasswordWrongException(e))
		    throw new ValidationException("Please correct the password of your current certificate key.");
		// TODO check if this has proper user interaction!!!
		ErrorMessage.error(getParent(), "Couldn't read private key", e);
		throw new ValidationException(null); // cancel without error message
	    }
	    try {
		certParent.check(true);
	    } catch (CertificateCheckException e) {
		throw new ValidationException("Parent certificate/key invalid:\n"+e.getLocalizedMessage());
	    }
	}

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
