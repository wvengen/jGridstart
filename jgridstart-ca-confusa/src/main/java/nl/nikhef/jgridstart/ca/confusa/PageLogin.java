package nl.nikhef.jgridstart.ca.confusa;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.xml.sax.SAXException;

import nl.nikhef.jgridstart.ca.CA;
import nl.nikhef.jgridstart.ca.CAException;
import nl.nikhef.jgridstart.ca.CAFactory;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.wizard.ITemplateWizardPage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;
import nl.nikhef.jgridstart.gui.wizard.TemplateWizard;
import nl.nikhef.jgridstart.gui.wizard.TemplateWizardWorker;

/** Confusa login page
 * <p>
 * This page presents the user with a link to the Confusa IdP login page
 * if he wasn't logged in before.
 * <p>
 * The property <tt>jgridstart.ca.oauth.poll</tt> specifies the number of 
 * milliseconds to wait between each poll to the Confusa website to see
 * if the user has authenticated properly.
 * 
 * @author wvengen
 *
 */
public class PageLogin extends RequestWizardPage {
    /** Worker thread that handles OAuth interaction */
    private GenerateWorker worker = null;
    
    public PageLogin() throws ParserConfigurationException, SAXException, IOException {
	super("login", PageLogin.class.getResource("login.html"));
    }

    @Override
    public boolean isDone() {
	try {
	    return getCA().loginIsDone();
	} catch (CAException e) {
	    ErrorMessage.logException(e);
	    return false;
	}
    }
    
    @Override
    public void pageEnter(ITemplateWizardPage oldPage) {
	// if the next button becomes enabled, just move to next page
	//if (!getWizard().getButtonEnabled(TemplateWizard.BUTTON_NEXT) && isDone())
	//    getWizard().setPageRelative(1);
	// enable button only after login was successful
	getWizard().setButtonEnabled(TemplateWizard.BUTTON_NEXT, isDone());
	
	// start worker thread
	if (worker==null) { 
	    worker = new GenerateWorker(this);
	    worker.execute();
	}
	// update properties
	data().setProperty("wizard.authdone.volatile", Boolean.toString(true));
	data().setProperty("wizard.authdone", Boolean.toString(isDone()));
    }
    
    @Override
    public boolean pageLeave(ITemplateWizardPage newPage, boolean isNext) {
	// if we are logged in, update user details when needed
	if (isDone() && !Boolean.valueOf("subject")) {
	    try {
		for (Map.Entry<Object, Object> entry: getCA().getUserInfo().entrySet())
		    data().setProperty((String)entry.getKey(), (String)entry.getValue());
	    } catch (Exception e) {
		// TODO this is ok for CAException, but is it for IOException?
		ErrorMessage.internal(getParent(), e);
	    }
	}
	// don't do anything yet if we return to the same page
	if (newPage==this) return true;
	// stop worker
	if (worker!=null) {
	    worker.cancel(true);
	    worker = null;
	}
	return true;
    }

    /** Return the Confusa CA
     * <p>
     * TODO get CA from certificate instead of {@link CAFactory}
     */
    protected ConfusaCA getCA() throws CAException {
	// TODO get CA from certificate for multiple CA support
	CA ca = CAFactory.getDefault();
	if (!(ca instanceof ConfusaCA))
	    throw new CAException("Must use ConfusaCA in Confusa request wizard");
	return (ConfusaCA)ca;
    }
    
    /** worker thread setting up and checking authentication */
    protected class GenerateWorker extends TemplateWizardWorker<Void> {
	
	private int timeout = 5000;

	public GenerateWorker(ITemplateWizardPage page) {
	    super(page);
	    // polling timeout
	    try {
		timeout = Integer.valueOf(data().getProperty("jgridstart.ca.oauth.poll"));
	    } catch (NumberFormatException e) { }
	    data().setProperty("jgridstart.ca.oauth.poll", Integer.toString(timeout));
	}
	
	/** Worker thread that handles OAuth interaction */
	@Override
	protected Void doWorkInBackground() throws Exception {
	    // if done, we don't need to do anything
	    if (PageLogin.this.isDone()) return null;
	    // retrieve new request token; publish does refresh
	    data().setProperty("wizard.authurl.volatile", Boolean.toString(true));
	    data().setProperty("wizard.authurl", getCA().loginPrepare().toExternalForm());
	    publish("state.authurl");
	    
	    // keep trying to see if user logged in
	    assert(!PageLogin.this.isDone());
	    while(true) {
		Thread.sleep(timeout);
		// else try to see if user logged in
		try {
		    getCA().loginProcess(null, null);
		} catch (OAuthNotAuthorizedException e) {
		    // if not logged in keep trying
		    continue;
		} catch(OAuthCommunicationException e) {
		    // SimpleSAMLphp can give server error when asking this twice
		    if (e.getMessage().contains("Internal server error")) continue;
		    else throw e;
		}
		// return when done
		if (PageLogin.this.isDone()) return null;
	    }
	}
    }
}
