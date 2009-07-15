package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.gui.util.TemplateWizard;

/** Show the request wizard for an existing certificate.
 * <p>
 * Shows the "request new" wizard from {@link ActionRequest}, but this
 * action just views the form of the currently selected certificate and
 * doesn't create a new one.
 * 
 * @see ActionRequest
 * @author wvengen
 */
public class ActionViewRequest extends CertificateAction {
    
    /** Page of request wizard to show by default */
    protected int defaultPage = 0;
    
    public ActionViewRequest(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Request...");
	putValue(MNEMONIC_KEY, new Integer('R'));
	BareBonesActionLaunch.addAction("viewrequest", this);
    }
    public ActionViewRequest(JFrame parent, CertificateSelection s, int defaultPage) {
	this(parent, s);
	this.defaultPage = defaultPage;
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	TemplateWizard dlg = new RequestWizard(parent, getCertificatePair(), selection);
	dlg.setStep(defaultPage);
	dlg.setVisible(true);
    }
}
