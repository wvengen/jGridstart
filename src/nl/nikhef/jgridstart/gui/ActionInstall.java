package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.security.SecureRandom;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.Timer;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.install.GridCertInstallerFirefox;
import nl.nikhef.jgridstart.util.PasswordCache;
import nl.nikhef.jgridstart.util.TempFileWriter;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;


public class ActionInstall extends CertificateAction {
    
    public ActionInstall(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Install...");
	putValue(MNEMONIC_KEY, new Integer('I'));
	BareBonesActionLaunch.addAction("install", this);
    }
    
    @Override
    public boolean wantsEnabled() {
	CertificatePair cert = selection.getCertificatePair();
	return cert!=null && Boolean.valueOf(cert.getProperty("cert"));
    }

    public void actionPerformed(ActionEvent e) {
	CertificatePair cert = selection.getCertificatePair();
	// TODO explain what'll happen in password dialog or option pane
	try {
	    /*
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
	     */
	    
	    // generate temporary password used to import in browser and write pkcs file
	    SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
	    //char[] pw = new char[15];
	    char[] pw = new char[1]; // TODO Illegal key size exception if larger password :(
	    for (int i=0; i<pw.length; i++) pw[i] = (char)(random.nextInt(128-32)+32);
	    TempFileWriter pkcs = new TempFileWriter("browser", ".p12");
	    PasswordCache.getInstance().set(pkcs.getPath(), pw);
	    boolean oldAsk = PasswordCache.getInstance().setAlwaysAskForEncrypt(false);
	    cert.exportTo(new File(pkcs.getPath()));
	    PasswordCache.getInstance().setAlwaysAskForEncrypt(oldAsk);
	    // now install and cleanup
	    GridCertInstallerFirefox.install(pkcs.getPath(), pw);
	    pkcs.delete();
	} catch (PasswordCancelledException e1) {
	    // do nothing
	} catch (Exception e1) {
	    logger.severe("Error installing certificate "+cert+": "+e1);
	    ErrorMessage.error(parent, "Install failed", e1);
	}
    }
}
