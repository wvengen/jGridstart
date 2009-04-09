package nl.nikhef.jgridstart.ca;

import java.io.IOException;
import java.util.Properties;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.PKCS10CertificationRequest;

/**
 * Interface for communicating with an (online) Certification Authority (CA)
 * 
 * @author wvengen
 */
interface CA {

    /**
     * Checks whether the certification request with the specified serial
     * number is processed by the CA.
     * 
     * @param serial the serial number of the request
     * @return true if ready to download, false if not
     */
    public boolean checkStatusOfRequest(String serial) throws IOException;
    
    /**
     * Download a certificate from the CA.
     * 
     * @param req the certificate signing request that was sent
     * @param serial the serial number of the certificate signing request that was returned
     *               by submission of the certificate signing request 
     * @return The X509Certificate signed by the certificate authority
     * @throws IOException
     */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, String reqserial) throws IOException;

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
}
