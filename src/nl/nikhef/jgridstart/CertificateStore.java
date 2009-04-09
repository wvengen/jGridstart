package nl.nikhef.jgridstart;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.util.PasswordCache;
import nl.nikhef.jgridstart.gui.util.ArrayListModel;

public class CertificateStore extends ArrayListModel<CertificatePair> {

    static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart");

    protected File path = null;
    protected PasswordCache pwcache = null;

    /** new empty certificate store */
    public CertificateStore() {
	super();
    }

    /** new certificate store and load from path */
    public CertificateStore(String path) {
	super();
	load(path);
    }

    /** new certificate store and load from path as File */
    public CertificateStore(File path) {
	super();
	load(path);
    }
    
    /** load certificates from the default directory */
    public void load() {
	// Certificate directory: $HOME/.globus/ by default, but if the
	// hostname starts with "tutorial" we have something different.
	// Ends with a slash.
	String hostname;
	try {
	    hostname = java.net.InetAddress.getLocalHost().getHostName();
	} catch (UnknownHostException e) {
	    hostname = "";
	}
	String certhome = System.getProperty("user.home") + File.separator;
	if (hostname.startsWith("tutorial")) {
	    certhome += "personal-dutchgrid-certificate";
	} else {
	    certhome += ".globus";
	}
	certhome += File.separator;
	load(certhome);
    }
    
    /**
     * load certificates
     * 
     * All subdirectories of the supplied path are loaded as separate
     * certificates.
     * 
     * @param path
     *            Path to load certificates from
     */
    public void load(String path) {
	load(new File(path));
    }

    /**
     * load certificates
     * 
     * All subdirectories of the supplied path are loaded as separate
     * certificates.
     * 
     * @param path
     *            Directory to load certificates from
     */
    public void load(File path) {
	this.path = path;
	List<File> files = Arrays.asList(path.listFiles());
	// add new items
	for (Iterator<File> i = files.iterator(); i.hasNext();) {
	    File f = i.next();
	    boolean found = false;
	    for (int j = 0; j < size(); j++) {
		File a = get(j).getPath();
		if (a.equals(f)) {
		    found = true;
		    break;
		}
	    }
	    if (!found)
		tryAdd(f);
	}
    }

    /**
     * refresh the certificate list from its source and each certificate as well
     */
    public void refresh() {
	if (path == null) {
	    logger.warning("Refresh of empty certificate store");
	    return;
	}
	List<File> files = Arrays.asList(path.listFiles());
	ArrayList<CertificatePair> removals = new ArrayList<CertificatePair>();
	// refresh each existing item and remove old ones
	for (int i = 0; i < size(); i++) {
	    CertificatePair c = get(i);
	    if (files.contains(c.getPath()))
		c.refresh();
	    else
		removals.add(c);
	}
	removeAll(removals);
	// add new items
	for (Iterator<File> i = files.iterator(); i.hasNext();) {
	    File f = i.next();
	    boolean found = false;
	    for (int j = 0; j < size(); j++) {
		File a = get(j).getPath();
		if (a.equals(f)) {
		    found = true;
		    break;
		}
	    }
	    if (!found)
		tryAdd(f);
	}
    }

    /**
     * Try to add a certificate file to this store but don't fail if an error
     * occurs.
     * 
     * @param f
     *            File to add
     * @return true if the certificate was succesfully added
     */
    protected boolean tryAdd(File f) {
	try {
	    add(new CertificatePair(f));
	} catch (IOException e) {
	    logger.warning("Failed to load certificate from " + f + ": "
		    + e.getMessage());
	    return false;
	}
	return true;
    }
    
    /** Create a new subdirectory for a CertificatePair in this store.
     *
     * @return Newly created directory name
     * @throws IOException
     */
    protected File newItem() throws IOException {
	File dst = null;
	for (int i = 0; i < Integer.MAX_VALUE; i++) {
	    dst = new File(path, String.format("grid-cert-%04d", i));
	    if (!dst.exists()) break;
	}
	if (dst.exists())
	    throw new IOException("Maximum number of certificates reached"); // very unlikely
	dst.mkdirs();
	return dst;
    }

    /**
     * Import a PKCS#12 or PEM file into the CertificateStore by creating a new
     * CertificatePair that is saved in this store.
     * 
     * @param src File to import from
     * @return Newly created CertificatePair
     * @throws IOException
     */
    public CertificatePair importFrom(File src) throws IOException {
	File dst = newItem();
	// import
	try {
	    CertificatePair cert = CertificatePair.importFrom(src, dst);
	    add(cert);
	    return cert;
	} catch(IOException e) {
	    dst.delete();
	    throw e;
	}
    }
    
    /** Create a new certificate request in the CertificateStore
     *
     * @param name CN to request for
     * @throws SignatureException 
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public CertificatePair generateRequest(String name) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
	File dst = newItem();
	try {
	    CertificatePair cert = CertificatePair.generateRequest(dst, name);
	    add(cert);
	    return cert;
	} catch(IOException e) {
	    dst.delete();
	    throw e;
	}
    }

}
