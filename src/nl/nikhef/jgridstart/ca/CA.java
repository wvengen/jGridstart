package nl.nikhef.jgridstart.ca;

import java.io.IOException;
import java.util.Properties;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.PKCS10CertificationRequest;

/** Interface for communicating with an (online) Certification Authority (CA)
 * <p>
 * This interface provides an abstraction layer to access a certification authority.
 * It is up to each implementation to use the arguments available. A certificate
 * signing request serial number can be returned by an implementation's
 * {@link #uploadCertificationRequest} which is passed to {@link #downloadCertificate}
 * later. When an implementation doesn't have or need a certificate signing request
 * serial number, it can just return [@code null} from {@link #uploadCertificationRequest}.
 * <p>
 * If you are creating a new implementation and find that the current interface
 * provides insufficient information, please contact the developers, and we'll
 * see if an API update is sensible.
 * 
 * @author wvengen
 */
public interface CA {
    /**
     * Uploads a user certificate signing request onto an CA.
     * 
     * @param req certification signing request
     * @param info extra information that may be sent with the request (implementation-dependant)
     * @return certificate signing request serial number
     * @throws IOException 
     */
    public String uploadCertificationRequest(
	    PKCS10CertificationRequest req, Properties info) throws IOException;

    /** Checks to see if a certificate signing request was processed by a CA.
     * <p>
     * When true, the certificate can be downloaded using {@link #downloadCertificate}.
     * <p>
     * Implementers of this CA interface could, for example, just return if
     * {@link #downloadCertificate} would complete without errors, optionally caching the
     * fetched certificate.
     * 
     * @param req the certificate signing request that was sent
     * @param reqserial the serial number of the certificate signing request that was
     *               returned by submission of the certificate signing request 
     * @return whether the certificate is available at the CA or not
     * @throws IOException
     */
    public boolean isCertificationRequestProcessed(
	    PKCS10CertificationRequest req, String reqserial) throws IOException;

    /** Download a certificate from the CA.
     * 
     * @param req the certificate signing request that was sent
     * @param reqserial the serial number of the certificate signing request that was
     *               returned by submission of the certificate signing request 
     * @return certificate signed by the certificate authority
     * @throws IOException
     */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, String reqserial) throws IOException;
    
    /** Return the CA certificate */
    public X509Certificate getCACertificate() throws IOException;
}
