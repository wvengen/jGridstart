package nl.nikhef.jgridstart.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;

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
}
