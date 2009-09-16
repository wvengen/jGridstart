package nl.nikhef.jgridstart.ca;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;
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
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

/**
 * A certificate authority that runs locally and signs using a
 * generated certificate. Ideal for testing. 
 * 
 * @author wvengen
 * 
 */
public class LocalCA implements CA {
    
    static final protected Logger logger = Logger.getLogger(LocalCA.class.getName());
    
    /** temporary CA certificate used to sign requests (generated at instantiation) */
    protected X509Certificate cacert = null;
    /** temporary CA private key used to sign requests (generated at instantiation) */
    protected PrivateKey cakey = null;
    /** serial number of last generated certificate */
    static protected int serial = 1;
    /** DN of local CA */
    final protected String caDN = "CN=LocalCA Test Certificate";
    /** number of seconds into the future generated certificates are valid */
    final protected int validtime = 60 * 60; 
    
    /**
     * Creates a new LocalCA and generates a self-signed certificate to
     * sign with. It is valid for an hour only.
     */
    public LocalCA() throws CertificateException, KeyException, NoSuchAlgorithmException, IllegalStateException, NoSuchProviderException, SignatureException {
	logger.fine("Generating self-signed LocalCA certificate");
	KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
	keygen.initialize(1024);
	KeyPair keypair = keygen.generateKeyPair();
	X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
	certGen.setSerialNumber(BigInteger.ONE);
	certGen.setIssuerDN(new X500Principal(caDN));
	certGen.setNotBefore(new Date(System.currentTimeMillis()-50000));
	certGen.setNotAfter(new Date(System.currentTimeMillis()+validtime*1000));
	certGen.setSubjectDN(new X500Principal(caDN));
	certGen.setPublicKey(keypair.getPublic());
	certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
	// CA extensions as defined in http://www.ogf.org/documents/GFD.125.pdf
	certGen.addExtension(X509Extensions.BasicConstraints, true,
		new BasicConstraints(true));
	certGen.addExtension(X509Extensions.KeyUsage, true,
		new KeyUsage(KeyUsage.keyCertSign|KeyUsage.cRLSign));

	cakey = keypair.getPrivate();
	cacert = certGen.generate(cakey, "BC");
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
	return true;
    }

    /** Creates and returns a certificate for the request. */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
	X509Certificate cert = null;
	String reqserial = info.getProperty("request.serial");
	if (reqserial==null)
	    throw new IOException("Request has no serial number!");
	
	try {
	    certGen.setSerialNumber(BigInteger.valueOf(Integer.valueOf(reqserial)));
	    certGen.setIssuerDN(cacert.getSubjectX500Principal());
	    certGen.setNotBefore(new Date(System.currentTimeMillis()-50000));
	    certGen.setNotAfter(new Date(System.currentTimeMillis()+validtime*1000));
	    certGen.setSubjectDN(req.getCertificationRequestInfo().getSubject());
	    certGen.setPublicKey(req.getPublicKey());
	    certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
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
	    throw new IOException("Could not sign certificate: "+e.getMessage());
	}
	return cert;
    }
    
    public X509Certificate getCACertificate() {
	assert(cacert!=null);
	return cacert;
    }
}
