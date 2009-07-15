package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.URLLauncher;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;

/** Import a new certificate from a PKCS#12/PEM file */
public class ActionImport extends AbstractAction {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    protected JFrame parent = null;
    protected CertificateStore store = null;
    protected CertificateSelection selection = null;
    
    public ActionImport(JFrame parent, CertificateStore store, CertificateSelection selection) {
	super();
	this.parent = parent;
	this.store = store;
	this.selection = selection;
	putValue(NAME, "Import...");
	putValue(MNEMONIC_KEY, new Integer('I'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control I"));
	URLLauncher.addAction("import", this);
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	JFileChooser chooser = new CertificateFileChooser(true);
	chooser.setDialogTitle("Import a new certificate");
	chooser.setApproveButtonText("Import");
	chooser.setApproveButtonMnemonic('I');
	int result = chooser.showDialog(parent, null);
	if (result == JFileChooser.APPROVE_OPTION) {
	    doImport(chooser.getSelectedFile());
	}
    }
    
    /** Import a file and add the certificate to the global list
     * 
     * @param f File to import
     */
    public void doImport(File f) {
	logger.info("Importing certificate: "+f);
	
	try {
	    CertificatePair cert = store.importFrom(f);
	    selection.setSelection(store.indexOf(cert));
	} catch (PasswordCancelledException e) {
	    // do nothing
	} catch(Exception e) {
	    logger.severe("Error importing certificate "+f+": "+e);
	    ErrorMessage.error(parent, "Import failed", e);
	}
    }
    
}
