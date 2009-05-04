package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.util.TempFileWriter;


public class ActionInstall extends CertificateAction {
    
    public ActionInstall(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Install...");
	putValue(MNEMONIC_KEY, new Integer('I'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control I"));
	BareBonesActionLaunch.addAction("install", this);
    }
    
    public void actionPerformed(ActionEvent e) {
	try {
	    CertificatePair cert = selection.getCertificatePair();
	    // TODO explain what'll happen in password dialog or option pane
	    // export to pkcs#12 certificate
	    final TempFileWriter pkcs = new TempFileWriter("browser", ".p12");
	    try {
		cert.exportTo(new File(pkcs.getPath()));
	    } catch (KeyStoreException e1) {
		e1.printStackTrace();
	    } catch (NoSuchProviderException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    } catch (NoSuchAlgorithmException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    } catch (CertificateException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	    // import into browser
	    BareBonesActionLaunch.openURL(
	    	new File(pkcs.getPath()).toURI().toURL(),
	    	parent);
	    // delete after a timeout
	    // TODO figure out reasonable timeout
	    /*
	    new Timer(1*60*1000, new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    pkcs.delete();
		}
	    });
	    */
	} catch (MalformedURLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	} catch (IOException e2) {
	    // TODO Auto-generated catch block
	    e2.printStackTrace();
	}
    }
}
