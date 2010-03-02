package nl.nikhef.jgridstart.gui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import nl.nikhef.jgridstart.gui.util.FileFilterSuffix;

/** Filechooser for PEM and PKCS#12 files */
public class CertificateFileChooser extends JFileChooser {
    
    public CertificateFileChooser(boolean open) {
	super();
	
	addChoosableFileFilter(
		new FileFilterSuffix("PKCS#12 Certificate Bundle", ".p12,.pfx"));
	addChoosableFileFilter(
		new FileFilterSuffix("PEM Certificate Bundle", ".pem"));
	if (open)
	    addChoosableFileFilter(
		    new FileFilterSuffix("Certificate Bundle", ".p12,.pfx,.pem"));
	
	setAcceptAllFileFilterUsed(false);
	
	// Make sure a filefilter is selected; needed on Mac OS X only
	setFileFilter(getChoosableFileFilters()[0]);
	
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
		// get file and selected filefilter
		FileFilter ff = getFileFilter();
		String path = f.getPath();
		String[] exts = ((FileFilterSuffix)ff).getExtensions();
		// check if the selected file has an extension of this filter
		boolean hasext = false;
		for (int i=0; i<exts.length; i++)
		    if (path.endsWith(exts[i])) hasext = true;
		// if not, add first extension
		if (!hasext) path += exts[0];
		// generate filename
		f = new File(path);
	    }
	}
	return f;
    }
}
