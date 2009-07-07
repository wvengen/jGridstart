package nl.nikhef.jgridstart.ca;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import nl.nikhef.jgridstart.util.ConnectionUtils;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

/**
 * This class is used to download a certificate from a Nikhef CA
 * 
 * @author wvengen
 * 
 */
public class NikhefCA implements CA {
    
    static final protected Logger logger = Logger.getLogger(NikhefCA.class.getName());
    
    /** Base URL of certificate authority */
    protected String base = "http://www.nikhef.nl/~wvengen/testca/";

    /** Create new OpenCA plugin; initializes SSL configuration 
     * 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException */
    public NikhefCA() throws NoSuchAlgorithmException, KeyManagementException {
	// TODO try better way to create https connection. maybe using hostname verifier
	//      or supply host certificate with application and require that. 

	// Create a trust manager that does not validate certificate chains
	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	    public X509Certificate[] getAcceptedIssuers() {
		return null;
	    }
	    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
	    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
	} };

	// Install the all-trusting trust manager
	SSLContext sc = SSLContext.getInstance("SSL");
	sc.init(null, trustAllCerts, new java.security.SecureRandom());
	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /** Uploads a user certificate signing request onto a Nikhef CA
     * 
     * @param req {@inheritDoc}
     * @param info {@inheritDoc}; only "email" is used here.
     * @return {@inheritDoc}
     */
    public String uploadCertificationRequest(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	
	String name = req.getCertificationRequestInfo().getSubject().getValues(X509Principal.CN).get(0).toString();
	
	StringWriter reqWriter = new StringWriter();
	PEMWriter w = new PEMWriter(reqWriter);
	w.writeObject(req);
	w.close();
	
	String[] postdata = {
		"action", "submit",
		"fullname", name,
		"email", info.getProperty("email"),
		"request", reqWriter.toString(),
		"submit", "Submit request"
	};

	URL url = new URL(base);
	String answer = ConnectionUtils.pageContents(url, postdata, true);

	String serial = null;

	// TODO dodgy but I don't know how else to do it.
	try {
	    String matchstr = "Saving request as";
	    int index = answer.indexOf(matchstr);
	    if (index == -1 || answer.indexOf("error:") != -1 || answer.indexOf("Error:") != -1) {
		// means: not successful
		logger.severe("Could not upload certification request.");
		throw new /*UnableToUploadCertificationRequestException*/IOException(answer);
	    }
	    int index_end = answer.indexOf(".", index);
	    serial = answer.substring(index+matchstr.length(), index_end).trim();
	} catch (RuntimeException e) {
	    logger.severe(e.getMessage());
	    throw e;
	}

	logger.info("Uploaded certificate signing request with serial "+serial);

	return serial;
    }
    
    public boolean isCertificationRequestProcessed(
	    PKCS10CertificationRequest req, String reqserial) throws IOException {
	return downloadCertificate(req, reqserial) != null;
    }

    /** Download a certificate from the Nikhef CA
     * 
     * @param req {@inheritDoc} (not used by NikhefCA)
     * @param reqserial {@inheritDoc} 
     * @return {@inheritDoc}
     */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, String reqserial) throws IOException {
	
	if (reqserial==null || reqserial.equals(""))
	    throw new IOException("Cannot download certificate without request serial number");

	// return certificate by serial
	URL url = new URL(base);
	String[] pre = new String[] {
		"action", "retrieve_cert",
		"serial", reqserial
	};
	String scert = ConnectionUtils.pageContents(url, pre, false);
	StringReader reader = new StringReader(scert);
	PEMReader r = new PEMReader(reader);
	X509Certificate cert = (X509Certificate)r.readObject();
	r.close();
	if (cert==null)
	    throw new IOException("Certificate could not be retrieved: "+scert);
	return cert;
    }
}
