package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;

public class ActionRevoke extends CertificateAction {
    
    public ActionRevoke(JFrame parent, CertificateSelection s) {
	super(parent, s);
	this.parent = parent;
	putValue(NAME, "Revoke...");
	putValue(MNEMONIC_KEY, new Integer('R'));
	URLLauncherCertificate.addAction("revoke", this);
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
	logger.finer("Action: "+getValue(NAME));
	String message = 
	    "<html><body>" +
	    "<h1>Request a revocation</h1>"+
	    "<p>When your certificate has been ... blah ..." +
	    "<p></p><p><em>Revocation is not yet implemented</em></p>" +
	    "</html></body>";
	Object [] options = { "Revoke", "Cancel" };
	int ret = JOptionPane.showOptionDialog(findWindow(e.getSource()),
		message,
		"Revoke certificate",
		JOptionPane.YES_NO_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null, // no icon
		options, options[0]);
    }
    
}
