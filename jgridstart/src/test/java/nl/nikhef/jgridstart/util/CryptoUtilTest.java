package nl.nikhef.jgridstart.osutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.Test;
import junit.framework.TestCase;

public class CryptoUtilsTest extends TestCase {
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.osutils");
    
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
    
    /** Test if line wrapping workaround works as expected */
    @Test
    public void testjavaMailWrappingWorkaround() throws Exception {
	String msg1 =
	    "Message-ID: <1660743788.2.1253198584345.JavaMail.wvengen@toce>\r\n" +
	    "MIME-Version: 1.0\r\n" +
	    "Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha1;\r\n" + 
	    "    boundary=\"----=_Part_1_190331520.1253198584335\"\r\n" +
	    "\r\n" +
	    "------=_Part_1_190331520.1253198584335\r\n" +
	    "Content-Type: text/plain; charset=us-ascii\r\n" +
	    "Content-Transfer-Encoding: 7bit\r\n" +
	    "\r\n" +
	    "(message)\n" +
	    "\r\n" +
	    "------=_Part_1_190331520.1253198584335\r\n" +
	    "Content-Type: application/x-pkcs7-signature; name=smime.p7s; smime-type=signed-data\r\n" +
	    "More: headers\r\n" +
	    "\r\n";
	String msg2 =
	    "YKN6NXXqGXhapZq1JmveXQeXHYvZtBZyNfPARVD/wmtFuODkwra1rYNLqL1XOhq0BN216CjBfN2d\r\n" +
	    "zEbZ5XemdAkzDcPYrM7/GWmdxo92ZQanCuU70WDtHYgmWRsmzeBnURBUasWGrReq6x0Q1RpdfRfvAAAA\r\n" +
	    "AAAA\r\n";
	String msg2wrapped = 
	    "YKN6NXXqGXhapZq1JmveXQeXHYvZtBZyNfPARVD/wmtFuODkwra1rYNLqL1XOhq0BN216CjBfN2d\r\n" +
	    "zEbZ5XemdAkzDcPYrM7/GWmdxo92ZQanCuU70WDtHYgmWRsmzeBnURBUasWGrReq6x0Q1RpdfRfv\r\n" +
	    "AAAAAAAA\r\n";
	String msg3 = "------=_Part_1_190331520.1253198584335--\r\n";
	
	String msg = msg1+msg2+msg3;
	String[] lines = msg.split("\r\n");
	assertEquals(lines[lines.length-2].length(), 4);
	assertEquals(lines[lines.length-3].length(), 80);
	assertEquals(lines[lines.length-4].length(), 76);
	// wrap and check again
	msg = CryptoUtils.javaMailWrappingWorkaround(msg);
	lines = msg.split("\r\n");
	assertEquals(lines[lines.length-2].length(), 8);
	assertEquals(lines[lines.length-3].length(), 76);
	assertEquals(lines[lines.length-4].length(), 76);
	assertEquals((msg1+msg2wrapped+msg3), msg);
    }
}
