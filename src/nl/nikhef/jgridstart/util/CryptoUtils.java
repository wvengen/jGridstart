package nl.nikhef.jgridstart.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;

/** Cryptographic utilities */
public class CryptoUtils {
    
    /** Return a certificate's subject hash.
     * <p>
     * This is equal to the value computed by {@code openssl x509 -hash -noout -in cert.pem}.
     * @throws NoSuchAlgorithmException 
     * @throws IOException 
     */
    public static String getSubjectHash(X509Certificate cert) throws NoSuchAlgorithmException, IOException {
	return String.format("%08x", getX509NameHash(cert.getSubjectX500Principal()));
    }
    /** Return a certificate signing request's subject hash.
     * <p>
     * Same as {@link #getSubjectHash} but for a CSR. No openssl equivalent.
     * @throws NoSuchAlgorithmException 
     * @throws NoSuchAlgorithmException 
     * @throws IOException 
     */
    public static String getSubjectHash(PKCS10CertificationRequest req) throws NoSuchAlgorithmException, IOException {
	return String.format("%08x", getX509NameHash(req.getCertificationRequestInfo().getSubject()));
    }
    /** Return a certificate's issuer hash.
     * <p>
     * This is equal to the value computed by {@code openssl x509 -issuer_hash -noout -in cert.pem}.
     * @throws NoSuchAlgorithmException 
     * @throws IOException 
     */
    public static String getIssuerHash(X509Certificate cert) throws NoSuchAlgorithmException, IOException {
	return String.format("%08x", getX509NameHash(cert.getIssuerX500Principal()));
    }

    
    /** Return the hash of an {@linkplain X509Name}.
     * <p>
     * Same as openssl's {@code X509_NAME_hash()}.
     * 
     * @throws NoSuchAlgorithmException 
     * @throws IOException 
     */
    public static long getX509NameHash(X509Name p) throws NoSuchAlgorithmException, IOException {
	byte[] hash = MessageDigest.getInstance("MD5").digest(p.getEncoded());
	long a = 0;
	a |=  (long)hash[0]&0xffL;
	a |= ((long)hash[1]&0xffL)<<8L;
	a |= ((long)hash[2]&0xffL)<<16L;
	a |= ((long)hash[3]&0xffL)<<24L;
	return a;
    }
    /** Return the hash of an {@linkplain X509Name}.
     * @see #getX509NameHash
     * @throws NoSuchAlgorithmException
     * @throws IOException 
     */
    public static long getX509NameHash(X500Principal p) throws NoSuchAlgorithmException, IOException {
	return getX509NameHash(new X509Principal(p.getEncoded()));
    }

    /** Return number of millseconds that certificate is still valid */
    public static long getX509MillisecondsValid(X509Certificate cert){
	Calendar now = Calendar.getInstance();
	Calendar end = Calendar.getInstance();
	end.setTime(cert.getNotAfter());
	return end.getTimeInMillis() - now.getTimeInMillis(); 
    }
    /** Return number of days that certificate is still valid */
    public static long getX509DaysValid(X509Certificate cert) {
	return getX509MillisecondsValid(cert) / (1000*60*60*24);
    }

    /**
     * S/MIME sign a string.
     * <p>
     * The output is a MIME message consisting of two parts. The first is the
     * MIME-encoded string supplied as input, the second is the message signed
     * by the key with certificate embedded.
     * <p>
     * Example output:
     * <code>
     * Message-ID: <1234567899.1.1234567890123.JavaMail.user@host>
     * MIME-Version: 1.0
     * Content-Type: multipart/signed; protocol="application/pkcs7-signature"; micalg=sha1; 
     * 	boundary="----=_Part_1_190331520.1253198584335"
     * 
     * ------=_Part_1_190331520.1253198584335
     * Content-Type: text/plain; charset=us-ascii
     * Content-Transfer-Encoding: 7bit
     * 
     * This is my freakingly sensitive piece of text,
     * 
     * ------=_Part_1_190331520.1253198584335
     * Content-Type: application/pkcs7-signature; name=smime.p7s; smime-type=signed-data
     * Content-Transfer-Encoding: base64
     * Content-Disposition: attachment; filename="smime.p7s"
     * Content-Description: S/MIME Cryptographic Signature
     * 
     * OTAzMDMwMDAwMDBaFw0xMDAzMDMxMDQ5MDFaMFAxEjAQBgNVBAoTCWR1dGNoZ3JpZDEOMAwGA1UE
     * ChMFdXNlcnMxDzANBgNVBAoTBm5pa2hlZjEZMBcGA1UEAxMQV2lsbGVtIHZhbiBFb.......etc.
     * yAAAAAAAAA==
     * ------=_Part_1_190331520.1253198584335--
     * </code>
     */
    public static String SignSMIME(String msg, PrivateKey key, X509Certificate cert) throws GeneralSecurityException, SMIMEException, MessagingException {
	// make sure we have the mailcap set right
	CryptoUtils.setDefaultMailcap();
	// create S/MIME message from it
	MimeBodyPart data = new MimeBodyPart();
	data.setText(msg);
	// add signature
	SMIMESignedGenerator gen = new SMIMESignedGenerator();
	gen.addSigner(key, cert, SMIMESignedGenerator.DIGEST_SHA1);
	CertStore certStore = CertStore.getInstance("Collection",
		new CollectionCertStoreParameters(Arrays.asList(cert)), "BC");
	gen.addCertificatesAndCRLs(certStore);
	MimeMultipart multipart = gen.generate(data, "BC");

	// don't use system properties as not to require full access to them
	//   we don't need to access mail servers anyway
	MimeMessage outmsg = new MimeMessage(Session.getDefaultInstance(new Properties()));
	outmsg.setContent(multipart, multipart.getContentType());
	outmsg.saveChanges();

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
	    outmsg.writeTo(out);
	    out.close();
	} catch (IOException e) {
	    // shouldn't happen on {@linkplain ByteArrayOutputStream}
	    throw new MessagingException("Internal output error", e);
	}
	
	return out.toString();
    }
    
    /** Initialize mailcap handlers.
     * <p>
     * In some rare cases, the error "no object DCH for MIME type" can appear, even for
     * {@literal text/plain}. In an attempt to avoid this, this should be called
     * in the application before any MIME messages are processed. To make it possible
     * to run this on-demand, subsequent calls do nothing, since the mailcap needs to
     * be initialised only once.
     */
    public static void setDefaultMailcap() {
	if (mailcapInitDone) return;
	MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
	mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
	mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
	mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");	
	mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
	mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
	mc.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
	mc.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
	mc.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
	mc.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
	mc.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");
	CommandMap.setDefaultCommandMap(mc);
	mailcapInitDone = true;
    }
    /** flag to indicate if mailcap has been initialised or not */
    private static boolean mailcapInitDone = false;

}
