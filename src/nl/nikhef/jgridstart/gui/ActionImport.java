package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
import nl.nikhef.jgridstart.util.PasswordCache;
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
	URLLauncherCertificate.addAction("import", this);
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	final JFileChooser chooser = new CertificateFileChooser(false);
	
	// embed in frame with password selection fields
	final JDialog dlg = new JDialog(parent,
		"Import a new certificate");

	final JPanel hpane = new JPanel();
	hpane.setLayout(new BoxLayout(hpane, BoxLayout.X_AXIS));
	final JCheckBox check = new JCheckBox("Use import password as private key password");
	check.setMnemonic('p');
	check.setSelected(true);
	hpane.add(check);
	hpane.add(Box.createHorizontalGlue());
	
	JPanel pane = ActionExport.customFileChooser(dlg, chooser,
		new AbstractAction("Import") {
        	    public void actionPerformed(ActionEvent e) {
        		try {
        		    File f = chooser.getSelectedFile();
        		    char[] pw = null;
        		    // request password if wanted
        		    if (!check.isSelected()) {
        			pw = PasswordCache.getInstance().getForEncrypt(
        				"New private key password "+f.getName(),
        				f.getCanonicalPath());
        		    }
        		    doImport(e, f, pw);
        		} catch (PasswordCancelledException e1) {
        		    /* do nothing */
        		} catch (Exception e1) {
        		    ErrorMessage.error(parent, "Import error", e1);
        		}
        		dlg.dispose();
        	    }
		}
	);
	pane.add(hpane);
	
	dlg.setName("jgridstart-import-file-dialog");
	dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	dlg.pack();
	dlg.setVisible(true);
    }
    
    /** Import a file and add the certificate to the global list
     * 
     * @param e originating event
     * @param f File to import
     * @param pw password to encrypt new private key with, or {@code null} to use same password as input file
     */
    public void doImport(ActionEvent e, File f, char[] pw) {
	logger.info("Importing certificate: "+f);
	
	try {
	    CertificatePair cert = store.importFrom(f, pw);
	    selection.setSelection(store.indexOf(cert));
	} catch (PasswordCancelledException e1) {
	    // do nothing
	} catch(Exception e1) {
	    logger.severe("Error importing certificate "+f+": "+e1);
	    ErrorMessage.error(CertificateAction.findWindow(e.getSource()), "Import failed", e1);
	}
    }
    
}
