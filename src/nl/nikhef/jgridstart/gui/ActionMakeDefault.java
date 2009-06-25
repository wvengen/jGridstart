package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;


public class ActionMakeDefault extends CertificateAction {
    
    protected CertificateStore store = null;
    
    public ActionMakeDefault(JFrame parent, CertificateStore store, CertificateSelection s) {
	super(parent, s);
	this.store = store;
	putValue(NAME, "Make default");
	putValue(MNEMONIC_KEY, new Integer('D'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
	BareBonesActionLaunch.addAction("makedefault", this);
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

    public void actionPerformed(ActionEvent e) {
	CertificatePair cert = selection.getCertificatePair();
	try {
	    store.setDefault(cert);
	} catch (IOException e1) {
	    ErrorMessage.error(parent, "Could not set default certificate", e1);
	}
    }
}
