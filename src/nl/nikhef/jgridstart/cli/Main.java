package nl.nikhef.jgridstart.cli;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.LogManager;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStore;

public class Main {

    // setup logging
    static {
	try {
	    LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
	} catch(Exception e) {
	    System.out.println("Warning: logging configuration could not be set");
	}
    }
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart");

    static private CertificateStore store = null;
    
    /** command-line interface entry point */
    public static void main(String[] args) {
	logger.fine("main starting");
	
	store = new CertificateStore();
	// load test path; use store.load() for production
	store.load(System.getProperty("user.home") + File.separator + ".globus-test");

	tryRequest();
	
	System.exit(0);
    }
    
    public static void tryRequest() {
	try {
	    Properties p = new Properties();
	    p.setProperty("subject", "O=dutchgrid, O=users, CN=John Doe");
	    store.generateRequest(p);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.exit(1);
	}
    }
    
    public static void tryImport() {
	File f = new File("/tmp/certtest/packed-cert.p12");
	try {
	    CertificatePair cert = store.importFrom(f);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
