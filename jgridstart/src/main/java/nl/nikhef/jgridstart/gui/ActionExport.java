package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
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
import nl.nikhef.jgridstart.gui.util.CertificateFileChooser;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
import nl.nikhef.jgridstart.passwordcache.PasswordCache;
import nl.nikhef.jgridstart.passwordcache.PasswordCancelledException;

/** Export selected certificate to PKCS#12/PEM file */
public class ActionExport extends CertificateAction {
    
    public ActionExport(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Export...");
	putValue(MNEMONIC_KEY, new Integer('E'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
	URLLauncherCertificate.addAction("export", this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));

	String filename = null;
	String[] args = e.getActionCommand().split(",\\s*");
	for (int i=0; i<args.length; i++) {
	    if (args[i].startsWith("filename="))
		filename = args[i].substring(9);
	}
	
	final JFileChooser chooser = new CertificateFileChooser(false);
	
	// embed in frame with password selection fields
	final JDialog dlg = new JDialog(parent,
		"Export the currently selected certificate");

	final JPanel hpane = new JPanel();
	hpane.setLayout(new BoxLayout(hpane, BoxLayout.X_AXIS));
	final JCheckBox check = new JCheckBox("Use private key password for the exported file");
	check.setMnemonic('p');
	check.setSelected(true);
	hpane.add(check);
	hpane.add(Box.createHorizontalGlue());
	
	if (filename!=null) {
	    chooser.setSelectedFile(new File(filename));
	}
	
	JPanel pane = CertificateFileChooser.customFileChooser(dlg, chooser,
		new AbstractAction("Export") {
        	    public void actionPerformed(ActionEvent e) {
        		try {
        		    File f = chooser.getSelectedFile();
        		    char[] pw = null;
        		    // request password if wanted
        		    if (!check.isSelected()) {
        			pw = PasswordCache.getInstance().getForEncrypt(
        				"PKCS#12 key password for "+f.getName(),
        				f.getCanonicalPath());
        		    }
        		    doExport(e, f, pw);
        		} catch (PasswordCancelledException e1) {
        		    /* do nothing */
        		} catch (Exception e1) {
        		    ErrorMessage.error(parent, "Export error", e1);
        		}
        		dlg.dispose();
        	    }
		}
	);
	pane.add(hpane);
	
	dlg.setName("jgridstart-export-file-dialog");
	dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	dlg.pack();
	dlg.setVisible(true);
    }
    
    /** Export the current certificate to a file
     * 
     * @param e originating event
     * @param f File to export to
     * @param pw password to use, or {@code null} to use private key password
     */
    public void doExport(ActionEvent e, File f, char[] pw) {
	CertificatePair cert = getCertificatePair();
	logger.info("Exporting certificate "+cert+" to: "+f);
	try {
	    cert.exportTo(f, pw);
	} catch (PasswordCancelledException e1) {
	    // do nothing
	} catch (Exception e1) {
	    logger.severe("Error exporting certificate "+f+": "+e1);
	    ErrorMessage.error(findWindow(e.getSource()), "Export failed", e1);
	}
    }
}
