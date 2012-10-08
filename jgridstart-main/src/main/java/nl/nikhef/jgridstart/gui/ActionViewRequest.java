package nl.nikhef.jgridstart.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JFrame;

import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
import nl.nikhef.jgridstart.gui.wizard.IRequestWizard;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardCommon;

/** Show the request wizard for an existing certificate.
 * <p>
 * Shows the "request new" wizard from {@link ActionRequest}, but this
 * action just views the form of the currently selected certificate and
 * doesn't create a new one.
 * <p>
 * The currently shown step is derived from its state.
 * Alternatively, the action can be invoked with the argument
 * {@code id=<pageid>} to open the wizard on a certain page id.
 * In links, this would be: {@code action:viewrequest(id=user details)}
 * to open the page with id "{@literal user details}". 
 * 
 * @see ActionRequest
 * @author wvengen
 */
public class ActionViewRequest extends CertificateAction {
    
    public ActionViewRequest(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Current Request...");
	putValue(MNEMONIC_KEY, new Integer('R'));
	URLLauncherCertificate.addAction("viewrequest", this);
    }

    @Override
    public boolean wantsEnabled() {
	try {
	    // need valid request or certificate 
	    if (getCertificatePair()==null) return false;
	    if (getCertificatePair().getCertificate()==null &&
		    getCertificatePair().getCSR()==null) return false;
	    // ok!
	    return true;
	} catch (IOException e) {
	    return false;
	}
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	IRequestWizard dlg = null;
	
	try {
	    Window w = findWindow(e.getSource());
	    if (w instanceof Frame)
		dlg = RequestWizardCommon.createInstance((Frame)w, null, selection, getCertificatePair());
	    else if (w instanceof Dialog)
		dlg = RequestWizardCommon.createInstance((Dialog)w, null, selection, getCertificatePair());
	    else
		ErrorMessage.internal(w, "Expected Frame or Dialog as owner");

	    String id = null;
	    if (e.getActionCommand()!=null) {
		String[] parts = e.getActionCommand().split(",");
		for (int i=0; i<parts.length; i++) {
		    String[] kv = parts[i].split("=", 2);
		    if (kv[0].trim().equals("id"))
			id = kv[1];
		}
	    }
	    if (id!=null)
		dlg.setPage(id);
	    else
		dlg.setPageDetect();

	    dlg.setVisible(true);
	} catch (Exception e1) {
	    ErrorMessage.internal(parent, e1);
	}
    }
}
