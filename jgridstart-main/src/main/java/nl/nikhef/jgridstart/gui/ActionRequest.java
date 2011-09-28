package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;

import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.TemplateWizard;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
import nl.nikhef.jgridstart.gui.wizard.IRequestWizard;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardCommon;

/** Open the "request a new certificate" wizard
 * 
 * @see ActionViewRequest
 * @author wvengen
 */
public class ActionRequest extends AbstractAction {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    protected JFrame parent = null;
    protected CertificateStore store = null;
    protected CertificateSelection selection = null;
    
    public ActionRequest(JFrame parent, CertificateStore store, CertificateSelection selection) {
	super();
	this.parent = parent;
	this.store = store;
	this.selection = selection;
	putValue(NAME, "Request new...");
	putValue(MNEMONIC_KEY, new Integer('R'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
	URLLauncherCertificate.addAction("request", this);
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	try {
	    IRequestWizard dlg = RequestWizardCommon.createInstance(parent, store, selection);
	    dlg.setVisible(true);
	} catch(Exception e1) {
	    ErrorMessage.internal(parent, e1);
	}
    }
}
