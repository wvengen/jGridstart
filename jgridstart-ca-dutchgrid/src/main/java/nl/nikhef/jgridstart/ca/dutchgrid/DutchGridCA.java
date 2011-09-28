package nl.nikhef.jgridstart.ca.dutchgrid;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import nl.nikhef.jgridstart.ca.CA;
import nl.nikhef.jgridstart.osutils.ConnectionUtils;
import nl.nikhef.jgridstart.util.CryptoUtils;

import org.apache.commons.lang.WordUtils;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.mail.smime.SMIMEException;
import nl.nikhef.jgridstart.util.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.encoders.Base64;

/**
 * This class is used interface with the DutchGrid production CA
 * 
 * @author wvengen
 */
public class DutchGridCA implements CA {
    
    static final protected Logger logger = Logger.getLogger(DutchGridCA.class.getName());
    
    /** CA entry point: submission */
    protected String baseSubmit = System.getProperty("jgridstart.ca.base.submit");
    /** CA entry point: query */
    protected String baseQuery = System.getProperty("jgridstart.ca.base.query");
    /** CA entry point: CA cert */
    protected String baseCaCert = System.getProperty("jgridstart.ca.base.cacert");
    
    /** CA certificate (cached) */
    protected static X509Certificate cacert = null;

    /** Create new DutchGridCA 
     * 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException */
    public DutchGridCA() throws NoSuchAlgorithmException, KeyManagementException {
	if (baseSubmit==null) {
	    baseSubmit = "http://ra.dutchgrid.nl/ra/public/submit";
	    System.setProperty("jgridstart.ca.base.submit", baseSubmit);
	}
	if (baseQuery==null) {
	    baseQuery = "http://ca.dutchgrid.nl/medium/query/";
	    System.setProperty("jgridstart.ca.base.query", baseQuery);
	}
	if (baseCaCert==null) {
	    baseCaCert = "https://ca.dutchgrid.nl/cgi-bin/nikhef-ms?certder";
	    System.setProperty("jgridstart.ca.base.cacert", baseCaCert);
	}
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
    
    /** PEM-encodes request and puts it in a S/MIME signed form.
     * <p>
     * Sets the property {@code renewal} to {@code true}.
     */
    public String signCertificationRequest(
	    PKCS10CertificationRequest req, Properties info,
	    PrivateKey oldKey, X509Certificate oldCert) throws IOException {
	
	String reqstring = encodeCertificationRequest(req, info);
	
	try {
	    String out = CryptoUtils.SignSMIME(reqstring, oldKey, oldCert);
	    info.setProperty("renewal", Boolean.toString(true));
	    return out.toString().replace("Content-Type: application/pkcs7-signature", "Content-Type: application/x-pkcs7-signature");
	    
	} catch (GeneralSecurityException e) {
	    throw new IOException("Could not sign request:\n"+e.getMessage());
	} catch (SMIMEException e) {
	    throw new IOException("Could not sign request:\n"+e.getMessage());
	}
    }

    /** Uploads a user certificate signing request onto the DutchGrid CA.
     * <p>
     * The field {@code email} is used to supply the user's email address to the CA
     * for notifying the user when the request is processed, and the field {@code agreecps}
     * must be {@code true} to succeed; the latter corresponds to the "I agree to the
     * privacy policy" checkbox on the website. The field {@code fullname} should contain
     * the user's full name. The optional field {@code comments} is used for additional
     * comments.
     * 
     * @param req {@inheritDoc}
     * @param info {@inheritDoc}; {@code email}, {@code fullname} and {@code agreecps} are used here.
     */
    public void uploadCertificationRequest(String req, Properties info) throws IOException {
	
	// renewal needs to be packed in a base64 encoded renewal blob
	String sendreq = req;
	if (Boolean.valueOf(info.getProperty("renewal"))) {
	    sendreq =
		"===== BEGIN DUTCHGRID RENEWAL BLOB =====\n" +
		WordUtils.wrap(new String(Base64.encode(req.getBytes())), 64, "\n", true) + "\n" +
		"===== END DUTCHGRID RENEWAL BLOB =====\n";
		logger.finest("Submitting DutchGrid renewal blob:\n" + sendreq);
	} else {
	    logger.finest("Submitting request:\n" + req);
	}
	
	String[] postdata = {
		"action", "submit",
		"fullname", info.getProperty("fullname"),
		"email_1", info.getProperty("email"),
		"email_2", info.getProperty("email"),
		"requesttext", sendreq,
		"comments", info.getProperty("comments", ""),
		"Publish", "Upload Publishing Data",
		"robot", "yes",
		"dummy", "dummy"
	};
	if (Boolean.valueOf(info.getProperty("agreecps"))) {
	    postdata[postdata.length-2] = "confirm-submit";
	    postdata[postdata.length-1] = "ja";
	}

	URL url = new URL(baseSubmit);
	String answer = ConnectionUtils.pageContents(url, postdata, true);

	// returns "200 ..." on success
	// TODO CAException instead of IOException
	logger.info("Answer: "+answer);
	if (!answer.startsWith("200")) {
	    logger.warning("Certificate signing request upload failed: "+answer);
	    throw new IOException("Certificate signing request upload failed:\n"+answer);
	}

	logger.info("Uploaded certificate signing request");
    }
    
    public boolean isCertificationRequestProcessed(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	return downloadCertificate(req, info) != null;
    }

    /** Download a certificate from the DutchGrid CA
     * 
     * @param req {@inheritDoc}
     * @param info {@inheritDoc} (not used by DutchGridCA)
     * @return {@inheritDoc}
     */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	
	// return certificate by serial
	URL url = new URL(baseQuery);
	String[] pre;
	try {
	    pre = new String[] {
	    	"id", ((RSAPublicKey)req.getPublicKey()).getModulus().toString(16),
	    	"fmt", "single"
	    };
	} catch (Exception e) {
	    throw new IOException(e.getLocalizedMessage());
	}
	String scert = ConnectionUtils.pageContents(url, pre, false);
	StringReader reader = new StringReader(scert);
	PEMReader r = new PEMReader(reader);
	X509Certificate cert = (X509Certificate)r.readObject();
	r.close();
	if (cert==null)
	    throw new IOException("Certificate could not be retrieved: "+scert);
	return cert;
    }
    
    /** {@inheritDoc}
     * <p>
     * CA certificate is downloaded once each program run.
     */
    public X509Certificate getCACertificate() throws IOException {
	if (cacert==null) {
	    // download when not present
	    String scert = ConnectionUtils.pageContents(new URL(baseCaCert));
	    StringReader reader = new StringReader(scert);
	    PEMReader r = new PEMReader(reader);
	    cacert = (X509Certificate)r.readObject();
	    r.close();
	    if (cacert==null)
		throw new IOException("CA certificate could not be retrieved: "+scert);
	}
	return cacert;
    }
}
