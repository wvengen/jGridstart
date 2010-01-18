package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
	try {
	    return getCertificatePair()!=null &&
	           getCertificatePair().getCertificate()!=null &&
	           Boolean.valueOf(getCertificatePair().getProperty("valid"));
	} catch (IOException e) {
	    return false;
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME)+" of "+getCertificatePair());
	try {
	    store.setDefault(getCertificatePair());
	} catch (IOException e1) {
	    ErrorMessage.error(findWindow(e.getSource()), "Could not set default certificate", e1);
	}
    }
}
