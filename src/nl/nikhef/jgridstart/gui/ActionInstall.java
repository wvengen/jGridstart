package nl.nikhef.jgridstart.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.security.SecureRandom;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.install.BrowserFactory;
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
	return getCertificatePair()!=null && getCertificatePair().getCertificate()!=null;
    }

    public void actionPerformed(ActionEvent e) {
	TempFileWriter pkcs = null;
	CertificatePair cert = null;
	try {
	    // generate temporary password used to import in browser and write pkcs file
	    SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
	    char[] pw = new char[7]; // TODO Illegal key size exception if larger password :(
	    for (int i=0; i<pw.length; i++) pw[i] = (char)(random.nextInt(128-32)+32);

	    // explain what'll happen in password dialog or option pane
	    String message = 
		"You are about to install the selected certificate into your\n" +
		"web browser, so that you can access protected websites.\n\n" +
		"The installation procedure will ask you for your master password\n" +
		"if you have set any. The additional password for importing this\n" +
		"certificate, will be copied to the clipboard.";
	    Object [] options = { "Install", "Cancel" };
	    int ret = JOptionPane.showOptionDialog(parent,
		    message,
		    "Install certificate into browser",
		    JOptionPane.OK_CANCEL_OPTION,
		    JOptionPane.INFORMATION_MESSAGE,
		    null, // no icon
		    options, options[0]);
	    if (ret!=JOptionPane.OK_OPTION) return;

	    cert = selection.getCertificatePair();
	    logger.finer("Action: "+getValue(NAME));
	    
	    // copy password to clipboard
	    Transferable transPassw = new StringSelection(new String(pw));
	    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transPassw, null);

	    pkcs = new TempFileWriter("browser", ".p12");
	    // TODO change PasswordCache.set() to have File argument
	    PasswordCache.getInstance().set(new File(pkcs.getPath()).getCanonicalPath(), pw);
	    boolean oldAsk = PasswordCache.getInstance().setAlwaysAskForEncrypt(false);
	    cert.exportTo(new File(pkcs.getPath()));
	    PasswordCache.getInstance().setAlwaysAskForEncrypt(oldAsk);
	    // now install and cleanup
	    BrowserFactory.getInstance().installPKCS12(new File(pkcs.getPath()));
	    pkcs=null; // to avoid deleting it since it still may be needed during install
	               // it is deleted anyway on program exit
 	    
	} catch (PasswordCancelledException e1) {
	    // do nothing
	} catch (Exception e1) {
	    logger.severe("Error installing certificate "+cert+": "+e1);
	    ErrorMessage.error(parent, "Install failed", e1);
	} finally {
	    if (pkcs!=null) pkcs.delete();
	}
    }
}
