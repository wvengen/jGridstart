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

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;

import nl.nikhef.jgridstart.util.FileUtils;
import nl.nikhef.jgridstart.util.PasswordCache;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
import nl.nikhef.jgridstart.ca.CAException;
import nl.nikhef.jgridstart.gui.util.ArrayListModel;

public class CertificateStore extends ArrayListModel<CertificatePair> implements ItemListener {

    static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart");

    protected File path = null;
    protected PasswordCache pwcache = null;
    /** Default certificate (the one copied to {@code ~/.globus}) */
    protected CertificatePair defaultCert = null;
    /** Prefix of user certificate subdirs of {@code ~/.globus} to load from */
    protected final String userCertPrefix = "user-cert-";

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
     * All subdirectories of the supplied path that start with
     * {@link #userCertPrefix} are loaded as separate certificates; in
     * addition to this, the directory itself is loaded as well.
     * 
     * @param path Directory to load certificates from
     */
    public void load(File path) {
	this.path = path;
	if (!path.isDirectory()) return;
	File[] files = (File[])ArrayUtils.addAll(new File[]{path}, path.listFiles());
	
	// add new items
	for (int i=0; i<files.length; i++) {
	    File f = files[i];
	    // filter out unwanted items
	    if (f!=path && !f.getName().startsWith(userCertPrefix)) continue;
	    if (!f.isDirectory()) continue;
	    // for default cert, require key to exist
	    if (f==path && !new File(f, "userkey.pem").exists()) continue;
	    // make sure it doesn't exist already in this store
	    boolean found = false;
	    for (int j = 0; j < size(); j++) {
		File a = get(j).getPath();
		if (a.equals(f)) {
		    found = true;
		    break;
		}
	    }
	    // add when required
	    if (!found)
		tryAdd(f);
	}
    }

    /**
     * refresh the certificate list from its source and each certificate as well
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     * @throws IOException 
     */
    public void refresh() throws KeyManagementException, NoSuchAlgorithmException, IOException, CAException {
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
	load(path);
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
	CertificatePair cert = super.remove(index);
	deletePath(cert.getPath());
	return cert;
    }
    /** Deletes a path on which a CertificatePair is based from disk. */
    protected void deletePath(File certPath) throws IOException {
	// and from disk; subdirs are not deleted
	File[] items = certPath.listFiles();
	for (int i=0; i<items.length; i++)
	    if (!items[i].delete())
		throw new IOException("Could not remove file: "+items[i]);
	if (!certPath.delete())
	    throw new IOException("Could not remove certificate directory "+certPath); 
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
	    deletePath(dst);
	    throw e;
	} catch(NoSuchAlgorithmException e) {
	    deletePath(dst);
	    throw e;
	} catch (UnrecoverableKeyException e) {
	    deletePath(dst);
	    throw e;
	} catch (KeyStoreException e) {
	    deletePath(dst);
	    throw e;
	} catch (NoSuchProviderException e) {
	    deletePath(dst);
	    throw e;
	} catch (CertificateException e) {
	    deletePath(dst);
	    throw e;
	} catch (CertificateCheckException e) {
	    deletePath(dst);
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
	    deletePath(dst);
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
	    deletePath(dst);
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

    /** Return the default certificate.
     * <p>
     * The default certificate is the one in {@code ~/.globus}
     * (or the platform's equivalent).
     * <p>
     * This can be called pretty often, so the result is cached. Updates outside
     * of this program will not be picked up as a result while running.
     * 
     * @return Default CertificatePair, or null if not present or not matched. 
     */
    public CertificatePair getDefault() {
	if (defaultCert==null) {
	    // find certificate with same path as store, that's it
	    for (Iterator<CertificatePair> it = iterator(); it.hasNext(); ) {
		CertificatePair c = it.next();
		if (c.path == path) {
		    defaultCert = c;
		    break;
		}
	    }
	}
	return defaultCert;
    }
    
    /** Make a {@link CertificatePair} the default certificate.
     * <p>
     * This is done by switching the contents of the current default certificate
     * directory with the new default certificate directory.
     * <p>
     * When the supplied {@linkplain CertificatePair} is already the default,
     * nothing happens.
     * 
     * @throws IOException */
    public void setDefault(CertificatePair c) throws IOException {
	CertificatePair oldDefault = getDefault();
	if (oldDefault==c) return;
	
	// store current state
	c.store();
	if (oldDefault!=null) oldDefault.store();
	
	File defaultPath = path;
	File certPath = c.getPath();
	// first find a new directory to move current default files to
	File certPathTmp = certPath;
	while (certPathTmp.exists())
	    certPathTmp = new File(certPathTmp.getParent(), certPathTmp.getName() + ".new.tmp");
	certPathTmp.mkdir();
	
	// move old default certificate files to temporary directory
	FileUtils.MoveFiles(FileUtils.listFilesOnly(defaultPath), certPathTmp);
	try {
	    // move new certificate files to default place
	    FileUtils.MoveFiles(FileUtils.listFilesOnly(certPath), defaultPath);
	    try {
		// rename temporary new dir to original path, after removing old one
		if (!certPath.delete())
		    throw new IOException("remove "+certPath);
		if (!certPathTmp.renameTo(certPath))
		    throw new IOException("rename "+certPathTmp+" to "+certPath);
	    } catch(IOException e) {
		// restore certificate files from default place
		FileUtils.MoveFiles(FileUtils.listFilesOnly(defaultPath), certPath);
		throw e;
	    }
	    // success!
	    c.path = path;
	    defaultCert = c;
	    if (oldDefault!=null) oldDefault.path = certPath;
	    
	} catch (IOException e) {
	    // restore files that were moved out of the way, and cleanup
	    FileUtils.MoveFiles(FileUtils.listFilesOnly(certPathTmp), defaultPath);
	    certPathTmp.delete();
	    throw new IOException("Could not set default certificate\n("+e.getMessage()+")");
	    
	} finally {
	    // make sure we leave a valid status
	    defaultCert.load(defaultCert.getPath());
	    if (oldDefault!=null) oldDefault.load(oldDefault.getPath());
	}
    }
}
