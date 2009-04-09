package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStore;


public class ActionImport extends AbstractAction {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.view");
    protected JFrame parent = null;
    protected CertificateStore store = null;
    
    public ActionImport(JFrame parent, CertificateStore store) {
	super();
	this.parent = parent;
	this.store = store;
	putValue(NAME, "Import...");
	putValue(MNEMONIC_KEY, new Integer('I'));
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
	    // TODO get password from user when required
	    CertificatePair cert = store.importFrom(f);
	    //TODO selection.select(cert);
	} catch(IOException e) {
	    logger.severe("Error importing certificate "+f+": "+e);
	}	
    }
    
}
