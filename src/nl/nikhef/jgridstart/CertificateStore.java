package nl.nikhef.jgridstart;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;

import nl.nikhef.jgridstart.util.FileUtils;
import nl.nikhef.jgridstart.util.PEMReader;
import nl.nikhef.jgridstart.util.PasswordCache;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
import nl.nikhef.jgridstart.gui.util.ArrayListModel;

public class CertificateStore extends ArrayListModel<CertificatePair> implements ItemListener {

    static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart");

    protected File path = null;
    protected PasswordCache pwcache = null;

    /** new empty certificate store */
    public CertificateStore() {
	super();
    }

    /** new certificate store and load from path */
    public CertificateStore(String path) {
	this();
	load(path);
    }

    /** new certificate store and load from path as File */
    public CertificateStore(File path) {
	this();
	load(path);
    }
    
    /** load certificates from the default directory
     * 
     * This is $HOME/.globus/ by default, but if the hostname starts
     * with "tutorial" we have something different.
     * 
     * TODO move this out of jGridStart and put it in a configfile
     */
    public void load() {
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
	    certhome += ".globus-test"; // XXX only for testing
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
	if (!path.isDirectory()) return;
	List<File> files = Arrays.asList(path.listFiles());
	// add new items
	for (Iterator<File> i = files.iterator(); i.hasNext();) {
	    File f = i.next();
	    if (!f.isDirectory()) continue;
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
	// now if only one certificate is present it should be the
	// default certificate
	trySetDefault();
    }

    /**
     * refresh the certificate list from its source and each certificate as well
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     * @throws IOException 
     */
    public void refresh() throws KeyManagementException, NoSuchAlgorithmException, IOException {
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
	    if (!f.isDirectory()) continue;
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
	// now if only one certificate is present it should be the
	// default certificate
	if (size()==1 && getDefault()==null)
	    trySetDefault(get(0));
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
	    logger.warning("Failed to load certificate from " + f + ": " + e.getMessage());
	    return false;
	} catch (CertificateCheckException e) {
	    logger.warning("Certificate invalid " + f + ": " + e.getMessage());
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
	    dst = new File(path, String.format("user-cert-%04d", i));
	    if (!dst.exists()) break;
	}
	if (dst.exists())
	    throw new IOException("Maximum number of certificates reached"); // very unlikely
	dst.mkdirs();
	return dst;
    }
    
    /** Hook parent to add an ItemListener when an item is added */
    @Override
    protected void notifyAdded(int start, int end) {
	super.notifyAdded(start, end);
	for (int i=start; i<=end; i++)
	    get(i).addItemListener(this);
    }
    /** Hook parent to remove an ItemListener when an item is removed */
    @Override
    protected void notifyRemoved(int start, int end) {
	for (int i=start; i<=end; i++)
	    get(i).removeItemListener(this);
	super.notifyRemoved(start, end);
    }
    /** ItemListener handler to catch changes in CertificatePair */
    public void itemStateChanged(ItemEvent e) {
	// now if only one certificate is present it should be the
	// default certificate
	trySetDefault();
	// and notify selection change listeners
	notifyChanged(indexOf(e.getItem()));
    }
    
    /** Deletes a CertificatePair from the store. This removes it permanently
     * from disk, so be careful. In the future it may be put into an archive
     * instead.
     * 
     * TODO should this be called 'remove' or is that too dangerous?
     * 
     * @throws IOException 
     */
    public CertificatePair delete(int index) throws IOException {
	logger.info("Deleting certificate #"+index+": "+get(index));
	// remove from list
	CertificatePair cert = super.remove(index);
	// and from disk; subdirs are not deleted
	File[] items = cert.getPath().listFiles();
	for (int i=0; i<items.length; i++)
	    if (!items[i].delete())
		throw new IOException("Could not remove file: "+items[i]);
	if (!cert.getPath().delete())
	    throw new IOException("Could not remove certificate directory "+cert.getPath()); 
	return null;
    }
    public CertificatePair delete(CertificatePair cert) throws IOException {
	return delete(indexOf(cert));
    }

    /**
     * Import a PKCS#12 or PEM file into the CertificateStore by creating a new
     * CertificatePair that is saved in this store.
     * 
     * @param src File to import from
     * @return Newly created CertificatePair
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     * @throws PasswordCancelledException 
     * @throws CertificateException 
     * @throws NoSuchProviderException 
     * @throws KeyStoreException 
     * @throws UnrecoverableKeyException 
     * @throws CertificateCheckException 
     */
    public CertificatePair importFrom(File src) throws IOException, NoSuchAlgorithmException, PasswordCancelledException, UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, CertificateException, CertificateCheckException {
	File dst = newItem();
	// import
	try {
	    CertificatePair cert = CertificatePair.importFrom(src, dst);
	    add(cert);
	    return cert;
	} catch(IOException e) {
	    dst.delete();
	    throw e;
	} catch(NoSuchAlgorithmException e) {
	    dst.delete();
	    throw e;
	} catch (UnrecoverableKeyException e) {
	    dst.delete();
	    throw e;
	} catch (KeyStoreException e) {
	    dst.delete();
	    throw e;
	} catch (NoSuchProviderException e) {
	    dst.delete();
	    throw e;
	} catch (CertificateException e) {
	    dst.delete();
	    throw e;
	}
    }
    
    /** Create a new certificate request in the CertificateStore
     *
     * @param p Properties to use for generation. See CertificatePair.generateRequest()
     * @throws SignatureException 
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws PasswordCancelledException 
     */
    public CertificatePair generateRequest(Properties p) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, PasswordCancelledException {
	File dst = newItem();
	try {
	    CertificatePair cert = CertificatePair.generateRequest(dst, p);
	    add(cert);
	    return cert;
	} catch(IOException e) {
	    dst.delete();
	    throw e;
	}
    }
    /** Create a new certificate request in the CertificateStore with preset password
     *
     * @param p Properties to use for generation. See CertificatePair.generateRequest()
     * @throws SignatureException 
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws PasswordCancelledException 
     */
    public CertificatePair generateRequest(Properties p, char[] pw) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, PasswordCancelledException {
	File dst = newItem();
	try {
	    CertificatePair cert = CertificatePair.generateRequest(dst, p, pw);
	    add(cert);
	    return cert;
	} catch(IOException e) {
	    dst.delete();
	    throw e;
	}
    }
    /** Renew a certificate.
     * <p>
     * This generates a new certificate request in the store, but S/MIME
     * signs it as well.
     * 
     * @throws IOException 
     * @throws PasswordCancelledException 
     * @throws IllegalArgumentException 
     * @throws SignatureException 
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws MessagingException 
     * @throws SMIMEException 
     */
    public CertificatePair generateRenewal(CertificatePair oldCert) throws IllegalArgumentException, PasswordCancelledException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, MessagingException, SMIMEException {
	// ask for private key first, cancel would skip everything
	PrivateKey oldKey = oldCert.getPrivateKey();
	
	// generate request
	CertificatePair newCert = generateRequest(oldCert);
	
	// create S/MIME message from it
	MimeBodyPart data = new MimeBodyPart();
	data.setText(FileUtils.readFile(newCert.getCSRFile()));
	// add signature
	SMIMESignedGenerator gen = new SMIMESignedGenerator();
	gen.addSigner(oldKey, oldCert.getCertificate(), SMIMESignedGenerator.DIGEST_SHA1);
	// TODO gen.addCertificates() ?
	MimeMultipart multipart = gen.generate(data, "BC");
	
	MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
	msg.setContent(multipart, multipart.getContentType());
	msg.saveChanges();
	
	// and store it as new CSR
	FileOutputStream out = new FileOutputStream(newCert.getCSRFile());
	msg.writeTo(out);
	out.close();
	
	return newCert;
    }

    /** Return the default certificate.<p>
     * The default certificate is the one in {@code ~/.globus}
     * (or the platform's equivalent). Normally this is a symlink to or copy of
     * a certificate in the store.
     * <p>
     * When the certificate is not found in the certificate store, it is added
     * as a new one. Because of this, make sure to invoke this method only after
     * a store is loaded using {@link #load} (or a constructor doing that).
     * 
     * @return Default CertificatePair, or null if not present or not matched. 
     */
    public CertificatePair getDefault() {
	File dflCertFile = new File(path, "usercert.pem");
	if (!dflCertFile.canRead()) return null;
	X509Certificate dflCert = null;
	try {
	    dflCert = (X509Certificate)PEMReader.readObject(dflCertFile, X509Certificate.class);
	    if (dflCert!=null) {
		for (Iterator<CertificatePair> it = iterator(); it.hasNext(); ) {
		    CertificatePair c = it.next();
		    if (c.getCertificate()!=null && c.getCertificate().equals(dflCert))
			return c;
		}
	    }
	} catch (IOException e) { }
	// no match: add to certificate store
	try {
	    logger.info("Adding existing default certificate to certificate store");
	    return importFrom(path);
	} catch (Exception e) { }
	return null;
    }
    
    /** Make a Certificate Pair the default certificate. This is done by
     * symlinking or copying it to ~/.globus (or the platform's equivalent)
     * so that the globus toolkit and other compatible tools can use it. 
     * @throws IOException */
    public void setDefault(CertificatePair c) throws IOException {
	CertificatePair oldDfl = getDefault();
	File dflKey = new File(path, "userkey.pem");
	File dflCert = new File(path, "usercert.pem");
	if (c.getCertificate()==null)
	    throw new IOException("Cannot set default to pending certificate: "+c);
	logger.info("Setting default certificate: "+c);
	// TODO check dflCert and dflKey are already present in certificate store!!!
	FileUtils.CopyFile(c.getKeyFile(), dflKey);
	FileUtils.CopyFile(c.getCertFile(), dflCert);
	// update listeners
	if (oldDfl!=null) notifyChanged(indexOf(oldDfl));
	notifyChanged(indexOf(c));
    }
    public void trySetDefault(CertificatePair c) {
	try {
	    setDefault(c);
	} catch(IOException e) {
	    logger.warning("Could not optionally set default certificate: "+e.getMessage());
	}
    }
    /** Set the default certificate when only one present */
    protected void trySetDefault() {
	// not that getDefault() must be first since it can add a
	// default certificate to the store
	try {
	    if (getDefault()==null && size()==1 && get(0).getCertificate()!=null)
	        trySetDefault(get(0));
	} catch (IOException e) { }
    }
}
