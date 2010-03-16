package nl.nikhef.jgridstart.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnvelopedParser;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.Test;
import junit.framework.TestCase;

public class CryptoUtilsTest extends TestCase {
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
    
    /** Local test private key */
    PrivateKey key = null;
    X509Certificate cert = null;

    @Override
    public void setUp() throws Exception {
	if (Security.getProvider("BC") == null)
	    Security.addProvider(new BouncyCastleProvider());

	generateTestCertificate();
    }
   
    /** Helper method: generate test key and self-signed certificate */
    protected void generateTestCertificate() throws Exception {
	KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
	keygen.initialize(1024);
	KeyPair keypair = keygen.generateKeyPair();
	X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
	certGen.setSerialNumber(BigInteger.ONE);
	certGen.setIssuerDN(new X500Principal("CN=CryptoUtils Test"));
	certGen.setNotBefore(new Date(System.currentTimeMillis()-50000));
	certGen.setNotAfter(new Date(System.currentTimeMillis()+60*60*1000));
	certGen.setSubjectDN(new X500Principal("CN=CryptoUtils Test"));
	certGen.setPublicKey(keypair.getPublic());
	certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

	key = keypair.getPrivate();
	cert = certGen.generate(key, "BC");
    }
    
    /** Helper method: verify S/MIME signed message */
    @SuppressWarnings("unchecked") // for SignerInformationStore#getSigners
    protected void verifySMIMEMessage(String str) throws MessagingException, CMSException, SMIMEException, GeneralSecurityException, IOException {
	// create mime message from smime again
	MimeMessage outmsg = new MimeMessage(
		Session.getDefaultInstance(new Properties()),
		new ByteArrayInputStream(str.getBytes()));
	SMIMESigned smime = new SMIMESigned((MimeMultipart)outmsg.getContent());
	// verify!
	CertStore certs = smime.getCertificatesAndCRLs("Collection", "BC");
	SignerInformationStore sis = smime.getSignerInfos();
	for (Iterator<SignerInformation> it = sis.getSigners().iterator(); it.hasNext(); ) {
	    SignerInformation signer = it.next();
	    X509Certificate cert = (X509Certificate)certs.getCertificates(signer.getSID()).iterator().next();
	    signer.verify(cert, "BC");
	}
    }
    
    /** Test if signing works to catch nasty "no object DCH" bug */
    @Test
    public void testSMIMESign() throws Exception {
	// if mailcapinit not done, try without first to test
	if (!CryptoUtils.mailcapInitDone) {
	    CryptoUtils.mailcapInitDone = true;
	    try {
		String msg = CryptoUtils.SignSMIME("this is a test message", key, cert);
		if (msg!=null)
		    logger.info("S/MIME signing without mailcap init succeeded");
		else
		    logger.info("S/MIME signing without mailcap init returned null");
	    } catch(Exception e) {
		logger.warning("S/MIME signing without mailcap init failed:" +e);
	    }
	    CryptoUtils.mailcapInitDone = false;
	} else {
	    logger.warning("Mailcap init already done!");
	}
	// then do it for real
	String msg = CryptoUtils.SignSMIME("this is a test message", key, cert);
	assertNotNull(msg);
	verifySMIMEMessage(msg);
    }
}
