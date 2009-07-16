package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.URLLauncher;

public class ActionShowDetails extends CertificateAction {

    public ActionShowDetails(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Details");
	putValue(MNEMONIC_KEY, new Integer('D'));
	URLLauncher.addAction("showdetails", this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	CertificatePair cert = getCertificatePair();
	boolean curVal = Boolean.valueOf(cert.getProperty("view.showdetails"));
	cert.setProperty("view.showdetails", Boolean.toString(!curVal));
    }
}
