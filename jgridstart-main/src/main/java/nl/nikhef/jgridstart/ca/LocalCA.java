package nl.nikhef.jgridstart.ca;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

/**
 * A certificate authority that runs locally and signs using a
 * generated certificate.
 * <p>
 * This implementation is meant for testing.
 * <p>
 * System properties used:
 * <ul>
 *   <li><tt>jgridstart.ca.local.dn</tt> - distinguished name of CA certificate
 *   <li><tt>jgridstart.ca.local.valid</tt> - number of seconds the certificates are valid
 *   <li><tt>jgridstart.ca.local.hold</tt> - if <i>true</i>, {@linkplain #isCertificationRequestProcessed}
 *     returns false; this can be used to test the situation where a certificate isn't signed yet.
 * </ul>
 * 
 * @author wvengen
 * 
 */
public class LocalCA implements CA {
    
    static final protected Logger logger = Logger.getLogger(LocalCA.class.getName());
    
    /** temporary CA certificate for different keyalgorithms  used to sign requests (generated at instantiation) */
    protected HashMap<String, X509Certificate> cacerts = new HashMap<String, X509Certificate>();
    /** temporary CA private key for different keyalgorithms used to sign requests (generated at instantiation) */
    protected HashMap<String, PrivateKey> cakeys = new HashMap<String, PrivateKey>();
    /** serial number of last generated certificate */
    static protected int serial = 1;
    /** DN of local CA */
    static protected String caDN = System.getProperty("jgridstart.ca.local.dn");
    /** number of seconds into the future generated certificates are valid */
    static protected int validtime = 60 * 60; 
    
    /**
     * Creates a new LocalCA and generates a self-signed certificate to
     * sign with. It is valid for an hour only.
     * <p>
     * By default, an RSA CA certificate is generated. If other key algorithms (like
     * DSA or ECDSA) are discovered in a CSR, a new CA of that type is generated on
     * the fly.
     */
    public LocalCA() throws CertificateException, KeyException, NoSuchAlgorithmException, IllegalStateException, NoSuchProviderException, SignatureException {
	// set defaults
	if (caDN==null)
	    caDN = "CN=LocalCA Test Certificate";
	String validstr = System.getProperty("jgridstart.ca.local.valid");
	if (validstr!=null) {
	    try { validtime = Integer.parseInt(validstr);
	    } catch(NumberFormatException e) { }
	}
	
	// generate default RSA CA certificate
	generateCaCert("RSA");
    }
    
    /** Generates a new CA key/certificate combination for the given algorithm */
    protected void generateCaCert(String keyalgname) throws CertificateException, KeyException, NoSuchAlgorithmException, IllegalStateException, NoSuchProviderException, SignatureException {
	// create CA certificate
	// find out keysize for algorithm
	int keysize=1024; // RSA, DSA
	if (keyalgname.equals("EC") || keyalgname.equals("ECDSA")) keysize=192;
	logger.fine("Generating self-signed LocalCA certificate ["+keyalgname+" "+Integer.toString(keysize)+"]");
	System.out.println("Generating self-signed LocalCA certificate ["+keyalgname+" "+Integer.toString(keysize)+"]");
	
	KeyPairGenerator keygen = KeyPairGenerator.getInstance(keyalgname);
	keygen.initialize(keysize);
	KeyPair keypair = keygen.generateKeyPair();
	X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
	certGen.setSerialNumber(BigInteger.ONE);
	certGen.setIssuerDN(new X500Principal(caDN));
	certGen.setNotBefore(new Date(System.currentTimeMillis()-50000));
	certGen.setNotAfter(new Date(System.currentTimeMillis()+validtime*1000));
	certGen.setSubjectDN(new X500Principal(caDN));
	certGen.setPublicKey(keypair.getPublic());
	certGen.setSignatureAlgorithm("SHA1With"+keyalgname);
	// CA extensions as defined in http://www.ogf.org/documents/GFD.125.pdf
	certGen.addExtension(X509Extensions.BasicConstraints, true,
		new BasicConstraints(true));
	certGen.addExtension(X509Extensions.KeyUsage, true,
		new KeyUsage(KeyUsage.keyCertSign|KeyUsage.cRLSign));

	PrivateKey cakey = keypair.getPrivate();
	cakeys.put(keyalgname, cakey);
	cacerts.put(keyalgname, certGen.generate(cakey, "BC"));
    }

    /** Return CA certificate for key algorithm.
     * <p>
     * If no CA certificate for that key algorithm name exists, generate
     * one on the fly.
     * 
     * @param keyalgname key algorithm
     * @return CA certificate
     * @throws IOException when CA generation fails
     */
    protected X509Certificate getCaCert(String keyalgname) throws IOException {
	if (!cacerts.containsKey(keyalgname)) {
	    try {
		generateCaCert(keyalgname);
	    } catch (Exception e) {
		throw new IOException(e);
	    }
	}
	return cacerts.get(keyalgname);
    }
    /** Return CA private key for key algorithm.
     * <p>
     * If no CA key for that key algorithm name exists, generate
     * one on the fly.
     * 
     * @param keyalgname key algorithm
     * @return CA certificate
     * @throws IOException when CA generation fails
     */
    protected PrivateKey getCaKey(String keyalgname) throws IOException {
	if (!cakeys.containsKey(keyalgname)) {
	    getCaCert(keyalgname);
	}
	return cakeys.get(keyalgname);
    }
    
    /** Just returns the PEM encoded version of the request. */
    public String encodeCertificationRequest(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	StringWriter out = new StringWriter();
	PEMWriter writer = new PEMWriter(out);
	writer.writeObject(req);
	writer.close();
	return out.toString();
    }
    
    /** LocalCA does no renewals, so this is equal to {@code #encodeCertificationRequest} */
    public String signCertificationRequest(
	    PKCS10CertificationRequest req, Properties info,
	    PrivateKey oldKey, X509Certificate oldCert) throws IOException {
	return encodeCertificationRequest(req, info);
    }
    
    /** Obtain a new serial number for the certificate signing request / certificate */
    public void uploadCertificationRequest(String req, Properties info) throws IOException {
	serial++;
	info.setProperty("request.serial", Integer.toString(serial));
    }

    /** This local CA always processes a certificate on the fly, so it returns always true. */
    public boolean isCertificationRequestProcessed(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	String hold = System.getProperty("jgridstart.ca.local.hold");
	if (hold==null) return true;
	return !Boolean.valueOf(hold);
    }

    /** Creates and returns a certificate for the request. */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	
	if (!isCertificationRequestProcessed(req, info))
	    return null;
	
	X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
	X509Certificate cert = null;
	String reqserial = info.getProperty("request.serial");
	if (reqserial==null)
	    throw new IOException("Request has no serial number!");

	String sigalgname;
	try {
	    sigalgname = req.getPublicKey().getAlgorithm();
	} catch (Exception e) {
	    throw new IOException("Could not get key algorithm from CSR", e);
	}
	X509Certificate cacert = getCaCert(sigalgname);
	PrivateKey cakey = getCaKey(sigalgname);
	
	try {
	    certGen.setSerialNumber(BigInteger.valueOf(Integer.valueOf(reqserial)));
	    certGen.setIssuerDN(cacert.getSubjectX500Principal());
	    certGen.setNotBefore(new Date(System.currentTimeMillis()-50000));
	    certGen.setNotAfter(new Date(System.currentTimeMillis()+validtime*1000));
	    certGen.setSubjectDN(req.getCertificationRequestInfo().getSubject());
	    certGen.setPublicKey(req.getPublicKey());
	    certGen.setSignatureAlgorithm(req.getSignatureAlgorithm().getAlgorithm().getId());
	    certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
		    new AuthorityKeyIdentifierStructure(cacert));
	    certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
		    new SubjectKeyIdentifierStructure(req.getPublicKey("BC")));
	    certGen.addExtension(X509Extensions.BasicConstraints, true,
		    new BasicConstraints(false));
	    certGen.addExtension(X509Extensions.KeyUsage, true,
		    new KeyUsage(KeyUsage.digitalSignature|KeyUsage.keyEncipherment|
			         KeyUsage.dataEncipherment));
	    Vector<KeyPurposeId> extendedKeyUsage = new Vector<KeyPurposeId>();
	    extendedKeyUsage.add(KeyPurposeId.id_kp_clientAuth);
	    extendedKeyUsage.add(KeyPurposeId.id_kp_emailProtection);
	    certGen.addExtension(X509Extensions.ExtendedKeyUsage, false,
		    new ExtendedKeyUsage(extendedKeyUsage));
	    // TODO implement internal CRL for testing and set cRLDistributionPoints
	    cert = certGen.generate(cakey, "BC");
	} catch (GeneralSecurityException e) {
	    throw new IOException("Could not sign certificate:\n"+e.getMessage());
	}
	return cert;
    }
    
    public X509Certificate getCACertificate(String keyalgname) throws IOException {
	return getCaCert(keyalgname);
    }
    /** Return default CA certificate, which is for the RSA algorithm. */
    public X509Certificate getCACertificate() throws IOException {
	return getCACertificate("RSA");
    }

    /** {@inheritDoc} */
    public boolean isIssuer(X509Certificate cert) {
	// TODO cache X500Principal
	String dn = caDN;
	if (dn.trim().startsWith("/"))
	    dn = dn.substring(1).replace('/', ',');
	return cert.getIssuerDN().equals(new X509Principal(dn));
    }
}
