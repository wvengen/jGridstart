package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.URLLauncher;

/** Renew a certificate
 * 
 * @author wvengen
 */
public class ActionRenew extends CertificateAction {
    
    protected CertificateStore store = null;
    
    public ActionRenew(JFrame parent, CertificateStore store, CertificateSelection s) {
	super(parent, s);
	this.store = store;
	putValue(NAME, "Renew...");
	putValue(MNEMONIC_KEY, new Integer('W'));
	URLLauncher.addAction("renew", this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	
	try {
	    // generate new request, since we have a CertificatePair it's a renewal
	    CertificatePair newCert = store.generateRenewal(getCertificatePair());
	    selection.setSelection(newCert);
	} catch (Exception e1) {
	    ErrorMessage.error(findWindow(e.getSource()), "Could not renew certificate", e1);
	}
    }
}
