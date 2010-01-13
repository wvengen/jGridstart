package nl.nikhef.jgridstart.gui;

import javax.swing.JFrame;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;

/** Set the currently selected certificate */
public class ActionSelectCertificate extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    protected JFrame parent = null;
    protected CertificatePair certificatePair = null;
    protected CertificateSelection selection = null;

    public ActionSelectCertificate(JFrame parent, CertificatePair cert, CertificateSelection cs) {
	super();
	this.parent = parent;
	this.certificatePair = cert;
	this.selection = cs;
	putValue(NAME, cert.toString());
    }

    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	selection.setSelection(certificatePair);
    }

}
