package nl.nikhef.jgridstart.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.URLLauncher;
import nl.nikhef.jgridstart.gui.util.TemplateWizard;

/** Show the request wizard for an existing certificate.
 * <p>
 * Shows the "request new" wizard from {@link ActionRequest}, but this
 * action just views the form of the currently selected certificate and
 * doesn't create a new one.
 * <p>
 * The currently shown step is derived from its state.
 * Alternatively, the action can be invoked with a number (as string)
 * in the actioncommand to display a specified step.
 * 
 * @see ActionRequest
 * @author wvengen
 */
public class ActionViewRequest extends CertificateAction {
    
    public ActionViewRequest(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Request...");
	putValue(MNEMONIC_KEY, new Integer('R'));
	URLLauncher.addAction("viewrequest", this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	TemplateWizard dlg = null;
	
	Window w = findWindow(e.getSource());
	if (w instanceof Frame)
	    dlg = new RequestWizard((Frame)w, getCertificatePair(), selection);
	else if (w instanceof Dialog)
	    dlg = new RequestWizard((Dialog)w, getCertificatePair(), selection);
	else
	    ErrorMessage.internal(w, "Expected Frame or Dialog as owner");

	try {
	    dlg.setStep(Integer.valueOf(e.getActionCommand()));
	} catch (NumberFormatException e1) {
	    // figure out correct step
	    if (!Boolean.valueOf(getCertificatePair().getProperty("request.submitted")))
		dlg.setStep(1);
	    else if (!Boolean.valueOf(getCertificatePair().getProperty("request.processed")))
		dlg.setStep(2);
	    else if (!Boolean.valueOf(getCertificatePair().getProperty("request.installed")))
		dlg.setStep(3);
	    else
		dlg.setStep(2); // form by default
	}
	dlg.setVisible(true);
    }
}
