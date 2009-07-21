package nl.nikhef.jgridstart.gui;

import java.io.File;
import javax.swing.JFileChooser;

import nl.nikhef.jgridstart.gui.util.FileFilterSuffix;

/** Filechooser for PEM and PKCS#12 files */
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
    
    /** {@inheritDoc}
     * <p>
     * This method adds the selected extension for opening a file
     * if the name supplied didn't have an extension.
     */
    @Override
    public File getSelectedFile() {
	File f = super.getSelectedFile();
	if (f!=null && getDialogType() == SAVE_DIALOG) {
	    if (!getFileFilter().accept(f)) {
		f = new File(f.getPath() + ((FileFilterSuffix)getFileFilter()).getExtensions()[0] );
	    }
	}
	return f;
    }
}
