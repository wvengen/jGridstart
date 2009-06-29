package nl.nikhef.jgridstart.gui;

import javax.swing.JFileChooser;

import nl.nikhef.jgridstart.gui.util.FileFilterSuffix;

public class CertificateFileChooser extends JFileChooser {

    public CertificateFileChooser(boolean open) {
	super();
	
	addChoosableFileFilter(
		new FileFilterSuffix("PEM Certificate Bundle", ".pem"));
	addChoosableFileFilter(
		new FileFilterSuffix("PKCS#12 Certificate Bundle", ".p12,.pfx"));
	if (open)
	    addChoosableFileFilter(
		    new FileFilterSuffix("Certificate Bundle", ".p12,.pfx,.pem"));
	
	setAcceptAllFileFilterUsed(false);
	
	setFileSelectionMode(FILES_ONLY);
	setDialogType(open ? OPEN_DIALOG : SAVE_DIALOG);
    }
}
