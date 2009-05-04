package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;


public class ActionExport extends CertificateAction {
    
    public ActionExport(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Export...");
	putValue(MNEMONIC_KEY, new Integer('E'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
	BareBonesActionLaunch.addAction("export", this);
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	JFileChooser chooser = new CertificateFileChooser(false);
	chooser.setDialogTitle("Export the currently selected certificate");
	chooser.setApproveButtonText("Export");
	chooser.setApproveButtonMnemonic('E');
	int result = chooser.showDialog(parent, null);
	if (result == JFileChooser.APPROVE_OPTION) {
	    doExport(chooser.getSelectedFile());
	}
    }
    
    /** Export the current certificate to a file
     * 
     * @param f File to export to
     */
    public void doExport(File f) {
	CertificatePair cert = getCertificatePair();
	logger.info("Exporting certificate "+cert+" to: "+f);
	try {
	    cert.exportTo(f);
	} catch (KeyStoreException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NoSuchProviderException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (CertificateException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
