package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.util.TempFileWriter;


public class ActionInstall extends CertificateAction {
    
    public ActionInstall(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Install...");
	putValue(MNEMONIC_KEY, new Integer('I'));
	BareBonesActionLaunch.addAction("install", this);
    }
    
    public boolean isEnabled() {
	CertificatePair cert = selection.getCertificatePair();
	return cert!=null && Boolean.valueOf(cert.getProperty("cert"));
    }
    
    public void actionPerformed(ActionEvent e) {
	CertificatePair cert = selection.getCertificatePair();
	// TODO explain what'll happen in password dialog or option pane
	try {
	    final TempFileWriter pkcs = new TempFileWriter("browser", ".p12");
	    // delete after a timeout
	    // TODO figure out reasonable timeout
	    new Timer(1*30*1000, new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    if (pkcs!=null) pkcs.delete();
		}
	    });
	    cert.exportTo(new File(pkcs.getPath()));
	    // import into browser
	    // TODO better export; maybe import info.ziyan.net.httpserver and supply mime-type
	    BareBonesActionLaunch.openURL(
		    new File(pkcs.getPath()).toURI().toURL(),
		    parent);
	} catch (Exception e1) {
	    logger.severe("Error installing certificate "+cert+": "+e1);
	    ErrorMessage.error(parent, "Install failed", e1);
	}
    }
}
