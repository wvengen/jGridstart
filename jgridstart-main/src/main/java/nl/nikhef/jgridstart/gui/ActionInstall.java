package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.JFrame;
import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.URLLauncher;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
import nl.nikhef.browsers.BrowserFactory;
import nl.nikhef.jgridstart.osutils.TempFileWriter;
import nl.nikhef.jgridstart.passwordcache.PasswordCancelledException;

/** Install certificate into web browser.
 * <p>
 *  
 * 
 * @author wvengen
 *
 */
public class ActionInstall extends CertificateAction {
    
    public ActionInstall(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Install in browser...");
	putValue(MNEMONIC_KEY, new Integer('I'));
	URLLauncherCertificate.addAction("install", this);
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
	TempFileWriter pkcs = null;
	CertificatePair cert = getCertificatePair();
	
	try {
	    // parse arguments
	    boolean silent = false;	// whether to silently install or inform user
	    String[] args = e.getActionCommand().split(",\\s*");
	    for (int i=0; i<args.length; i++) {
		if (args[i].equals("silent"))
		    silent = true;
	    }

	    if (!silent) {
		// explain what'll happen in password dialog or option pane
		// this redirects to the RequestWizard's install page for jGridStart
		URLLauncher.performAction("viewrequest(install)", findWindow(e.getSource()));
		return;
		
		// An alternative implementation would be (though the explanation is
		// a little brief):
		/*
		String message = 
		    "You are about to install the selected certificate into your\n" +
		    "web browser, so that you can access protected websites.\n\n" +
		    "The installation procedure will ask you for your master password\n" +
		    "if you have set any. The additional password for importing this\n" +
		    "certificate is your private key password.";
		Object [] options = { "Install", "Cancel" };
		int ret = JOptionPane.showOptionDialog(findWindow(e.getSource()),
			message,
			"Install certificate into browser",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE,
			null, // no icon
			options, options[0]);
		if (ret!=JOptionPane.OK_OPTION) return;
		*/

	    }
	    logger.finer("Action: "+getValue(NAME));
	    
	    pkcs = new TempFileWriter("browser", ".p12");
	    cert.exportTo(pkcs.getFile());
	    // now install and cleanup
	    pkcs.close(); // required for Windows to avoid "being used by another process" error
	    if (cert.getProperty("install.browser")!=null)
		BrowserFactory.getInstance().installPKCS12(cert.getProperty("install.browser"), pkcs.getFile());
	    else
		BrowserFactory.getInstance().installPKCS12(pkcs.getFile());
	    pkcs=null; // to avoid deleting it since it still may be needed during install
	               // it is deleted anyway on program exit
 	    cert.setProperty("install.done", "true");
	    
	} catch (PasswordCancelledException e1) {
	    // do nothing
	} catch (Exception e1) {
	    logger.severe("Error installing certificate "+cert+": "+e1);
	    ErrorMessage.error(findWindow(e.getSource()), "Install failed", e1);
	} finally {
	    if (pkcs!=null) pkcs.delete();
	}
    }
}
