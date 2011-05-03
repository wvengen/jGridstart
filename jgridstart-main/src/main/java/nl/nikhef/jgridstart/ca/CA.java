package nl.nikhef.jgridstart.ca;

import java.io.IOException;
import java.util.Properties;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.PKCS10CertificationRequest;

/** Interface for communicating with an (online) Certificate Authority (CA)
 * <p>
 * This interface provides an abstraction layer to access a certificate authority.
 * It is up to each implementation to use the arguments available.
 * <p>
 * A certificate signing request is generated by the caller, and then passed
 * to either {@link #encodeCertificationRequest} or {@link #signCertificationRequest}.
 * The return value must be stored by the caller, and is to be supplied to
 * {@link #uploadCertificationRequest}. Then the caller can poll to see if the
 * CA has finished the request using {@link #isCertificationRequestProcessed},
 * and if that is the case the certificate can be retrieved using
 * {@link #downloadCertificate}.
 * <p>
 * The reason for the split between encoding/signing and uploading is to allow for
 * certificate renewals that are signed by the existing key. The key must be decoded
 * when the renewal request is generated (for signing it), and that may require
 * user interaction. To minimise user interaction, the request is generated separately
 * from the uploading.
 * <p>
 * To each method is supplied a {@linkplain Properties} object, which contains
 * information that can be used by the implementation to create and submit a
 * request. Additionally, the implementation can use it to store properties.
 * When, for example, a certain part of the subject DN is needed for submission,
 * {@linkplain #encodeCertificationRequest} could set a property based on the
 * request object, so that {@linkplain #uploadCertificationRequest} can easily
 * retrieve it.
 * <p>
 * CA parameters (like URLs and settings) should be retrieved from the
 * {@link System#getProperty system properties} in the {@code jgridstart.ca} namespace.
 * Any URLs should be put in the {@code jgridstart.ca.base} namespace. Please look at
 * existing implementations for reuse of property names. When a property is not defined,
 * each implementation must set a sensible default and also update the system property
 * to reflect that.
 * <p>
 * If you are creating a new implementation and find that the current interface
 * provides insufficient information, please contact the developers, and we'll
 * see if an API update is sensible.
 * 
 * @author wvengen
 */
public interface CA {
    /**
     * Convert a certificate signing request to a form suitable for uploading.
     * <p>
     * When a certificate signing request is generated, it will need to be
     * uploaded to the certificate authority later using {@link #uploadCertificationRequest}.
     * A new request should be encoded first by this method.
     * <p>
     * It is currently assumed that this string at least contains a PEM encoded version
     * of the certificate request.
     * 
     * @param req certificate signing request
     * @param info extra information that may be sent with the request (implementation-dependent)
     * @return string with encoded certificate signing request
     */
    public String encodeCertificationRequest(
	    PKCS10CertificationRequest req, Properties info) throws IOException;

    /**
     * Post-processes (sign) a certificate signing request for renewal.
     * <p>
     * Renewals may be implemented by different methods, one of which is
     * signing the text of the request with the original key. When the request
     * is stored for reference later (and uploading later in case of a
     * connection problem!), the signed request needs to be stored. That is
     * why a string is returned, which is the signed certificate request.
     * This is passed later to {@link #uploadCertificationRequest}.
     * <p>
     * Note that it is only called in case of a certificate renewal, otherwise
     * it should be omitted.
     * <p>
     * It is currently assumed that this string at least contains a PEM encoded version
     * of the certificate request.
     * 
     * @param req certificate signing request
     * @param info extra information that may be sent with the request (implementation-dependent)
     * @param oldKey key to sign request with
     * @param oldCert certificate to sign request with
     * @return string with encoded and signed certificate signing request
     */
    public String signCertificationRequest(
	    PKCS10CertificationRequest req, Properties info,
	    PrivateKey oldKey, X509Certificate oldCert) throws IOException;

    /**
     * Uploads a user certificate signing request onto a CA
     * <p>
     * The request passed in as the {@literal req} paremeter must be the return
     * value of either {@link #encodeCertificationRequest} or {@link #signCertificationRequest}.
     * 
     * @param req certification signing request
     * @param info extra information that may be sent with the request (implementation-dependant)
     * @return certificate signing request serial number
     * @throws IOException 
     */
    public void uploadCertificationRequest(String req, Properties info) throws IOException;
    
    /** Checks to see if a certificate signing request was processed by a CA.
     * <p>
     * When true, the certificate can be downloaded using {@link #downloadCertificate}.
     * <p>
     * Implementers of this CA interface could, for example, just return if
     * {@link #downloadCertificate} would complete without errors, optionally caching the
     * fetched certificate.
     * 
     * @param req the certificate signing request that was sent
     * @param info properties supplied to previous methods as well 
     * @return whether the certificate is available at the CA or not
     * @throws IOException
     */
    public boolean isCertificationRequestProcessed(
	    PKCS10CertificationRequest req, Properties info) throws IOException;

    /** Download a certificate from the CA.
     * 
     * @param req the certificate signing request that was sent
     * @param info properties supplied to previous methods as well 
     * @return certificate signed by the certificate authority
     * @throws IOException
     */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, Properties info) throws IOException;
    
    /** Return the CA certificate */
    public X509Certificate getCACertificate() throws IOException;
}