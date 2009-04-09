package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStore;


public class ActionRequest extends AbstractAction {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.view");
    protected JFrame parent = null;
    protected CertificateStore store = null;
    
    public ActionRequest(JFrame parent, CertificateStore store) {
	super();
	this.parent = parent;
	this.store = store;
	putValue(NAME, "Request new...");
	putValue(MNEMONIC_KEY, new Integer('R'));
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	String name = (String)JOptionPane.showInputDialog(parent,
		"You are about to request a new certificate from the " +
		"Certificate Authority. Please provide your name, and blah blah.",
		"Request a new certificate",
		JOptionPane.PLAIN_MESSAGE);
	if (name != null) {
	    try {
		store.generateRequest("CN="+name);
	    } catch (InvalidKeyException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    } catch (NoSuchAlgorithmException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    } catch (NoSuchProviderException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    } catch (SignatureException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
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
