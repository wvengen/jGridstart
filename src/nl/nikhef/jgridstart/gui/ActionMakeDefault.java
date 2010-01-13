package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStoreWithDefault;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;

/** Make selected certificate the default.
 * <p>
 * See {@link CertificateStoreWithDefault} for an explanation of the default certificate.
 * 
 * @author wvengen
 */
public class ActionMakeDefault extends CertificateAction {
    
    protected CertificateStoreWithDefault store = null;
    
    public ActionMakeDefault(JFrame parent, CertificateStoreWithDefault store, CertificateSelection s) {
	super(parent, s);
	this.store = store;
	putValue(NAME, "Make default");
	putValue(MNEMONIC_KEY, new Integer('D'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
	URLLauncherCertificate.addAction("makedefault", this);
    }
    
    @Override
    public boolean wantsEnabled() {
	CertificatePair cert = selection.getCertificatePair();
	try {
	    return cert!=null && cert.getCertificate()!=null && !cert.equals(store.getDefault());
	} catch (IOException e) {
	    return false;
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	CertificatePair cert = selection.getCertificatePair();
	logger.finer("Action: "+getValue(NAME)+" of "+cert);
	try {
	    store.setDefault(cert);
	} catch (IOException e1) {
	    ErrorMessage.error(findWindow(e.getSource()), "Could not set default certificate", e1);
	}
    }
}
