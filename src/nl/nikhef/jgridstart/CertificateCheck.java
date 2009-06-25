package nl.nikhef.jgridstart;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.util.PEMReader;
import nl.nikhef.jgridstart.util.PasswordCache;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;

/** Security checks for a certificate directory.
 * <p>
 * All checks have a void return type and throw an (TODO x) Exception on failure.
 */
public class CertificateCheck {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart");


    /** CertificatePair under investivation */
    protected CertificatePair cert = null;

    /** Create a new instance. */
    public CertificateCheck(CertificatePair c) {
	cert = c;
    }
    
    /** Run the default checks.
     * <p>
     * Exceptions work such that only one exception is shown at once.
     * So the order in which the checks appear <em>is</em> important.
     */
    public void check() throws IOException {
	checkAccessPath();
	checkPrivateKey();
	checkCertificate();
    }
    
    /** Run the private key checks.
     * <p>
     * This is a separate method from {@linkplain #check} because it
     * requires the private key's password so either make sure the
     * user is expecting a password dialog, or the password is already
     * present in the {@link PasswordCache}. 
     */
    public void checkPrivate() throws IOException {
	checkPrivateKeyMatchesCertificate();
    }
    
    /** Check access to certificate directory. Must exist. */
    protected void checkAccessPath() throws IOException {
	File f = cert.getPath();
	if (!f.exists())
	    fail("Certificate directory not found", f);
	if (!f.isDirectory())
	    fail("Certificate directory is not a directory", f);
	if (!f.canRead())
	    fail("Certificate directory cannot be read", f);
    }

    /** Check that the private key is valid.
     * <p>
     * It only checks that a private key file is present and has a
     * valid format, not if it can be decrypted since we don't want
     * to use the password.
     */
    protected void checkPrivateKey() throws IOException {
	File f = cert.getKeyFile();
	if (!f.exists())
	    fail("Private key not found", f);
	if (!f.isFile())
	    fail("Private key is not a file", f);
	if (!f.canRead())
	    fail("Private key cannot be read", f);
	// TODO check that others cannot read this key!
	try {
	    Object o = PEMReader.readObject(f, KeyPair.class);
	    if (o==null)
		fail("Private key file contains no private key", f);
	} catch (IOException e) {
	    // Since readPEM "throws IOException" the specific information
	    // that it might have been a PasswordException is lost :(
	    // So now I have to parse the message string ...
	    if (!e.getMessage().contains("org.bouncycastle.openssl.PasswordException") &&
		    !e.getMessage().contains("wrong password"))
		throw e;
	}
    }

    /** Check the certificate.
     * <p>
     * This is only checked if the certificate really
     * exists, because it is optional (e.g. when the certificate signing
     * request was made but the certificate not received from the CA).
     */
    protected void checkCertificate() throws IOException {
	File f = cert.getCertFile();
	if (!f.exists())
	    return;
	if (!f.isFile())
	    fail("Certificate is not a file", f);
	if (!f.canRead())
	    fail("Certificate cannot be read", f);
	// check if certificate was loaded or can be loaded
	if (cert.getCertificate() == null) {
	    fail("Certificate file contains no certificate", f);
	}
    }
    
    /** Check if the private key and certificate belong together.
     * <p>
     * This requires the private key to be decrypted so a password
     * may be asked.
     * <p>
     * TODO don't use IOException
     */
    protected void checkPrivateKeyMatchesCertificate() throws IOException {
	try {
	    PublicKey pub = cert.getCertificate().getPublicKey();
	    PrivateKey priv = cert.getPrivateKey();
	    if (!pub.getAlgorithm().equals(priv.getAlgorithm()))
		    fail("Private key doesn't belong to certificate (algorithm)");
	    
	    if (pub.getAlgorithm().equals("DSA")) {
		DSAPublicKey dpub = (DSAPublicKey)pub;
		DSAPrivateKey dpriv = (DSAPrivateKey)priv;
		DSAParams dpubp = dpub.getParams();
		DSAParams dprivp = dpriv.getParams();
		if ( !dpubp.getG().equals(dprivp.getG()) ||
		     !dpubp.getP().equals(dprivp.getP()) ||
		     !dpubp.getQ().equals(dprivp.getQ()) )
		    fail("Private key doesn't belong to certificate (DSA params)");
		    
	    } else if (pub.getAlgorithm().equals("RSA")) {
		RSAPublicKey rpub = (RSAPublicKey)pub;
		RSAPrivateKey rpriv = (RSAPrivateKey)priv;
		if (!rpub.getModulus().equals(rpriv.getModulus()))
			fail("Private key doesn't belong to certificate (modulus)");
		
	    } else 
		fail("Unsupported key format");
	} catch (PasswordCancelledException e) { }
    }
    
    /** Called when a check fails, throws an Exception. */
    protected void fail(String msg) throws IOException {
	_fail(msg+": "+cert);
    }
    /** Called when a check fails, throws an Exception. */
    protected void fail(String msg, File f) throws IOException {
	_fail(msg+": "+f);
    }
    /** Called when a check fails, throws an Exception (low level, don't use) */
    private void _fail(String msg) throws IOException {
	logger.warning("Check failed: "+msg);
	throw new IOException(msg);
    }
}
