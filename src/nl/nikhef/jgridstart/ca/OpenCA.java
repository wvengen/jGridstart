/*
 * Copyright 2006 VPAC
 * Copyright 2009 Willem van Engen <wvengen@nikhef.nl>
 * 
 * You can redistribute this file and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or any later version.
 * 
 * Grix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * the source; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * This file is based on org.vpac.grix.plugins.openca.OpenCA from Grix
 *   http://projects.arcs.org.au/
 */

// TODO this package is unfinished and doesn't work

package nl.nikhef.jgridstart.ca;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import nl.nikhef.jgridstart.util.ConnectionUtils;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

/**
 * This class is used to download a certificate from an OpenCA server via http(s).
 * 
 * TODO Suffers heavily from bit-rot, please review thoroughly before use.
 * 
 * @author Markus Binsteiner
 * @author wvengen
 *
 */
public class OpenCA implements CA {
    
    static final protected Logger logger = Logger.getLogger(OpenCA.class.getName());

    /** Create new OpenCA plugin; initializes SSL configuration 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException */
    public OpenCA() throws NoSuchAlgorithmException, KeyManagementException {
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

    /** Return the URL of the next page in the list.
     * 
     * Note that this only works on OpenCA pages. This method may need updating when
     * the OpenCA web page changes :/
     * 
     * @param page_body Page to search on for reference to next page
     * @return URL of the next page
     * @throws MalformedURLException
     */
    protected static URL containsNextPage(String page_body) throws MalformedURLException {
	final Pattern p = Pattern.compile("<a href=([\"'])(.*?viewFrom=.*?)\1>");
	Matcher m = p.matcher(page_body);
	if (!m.matches()) return null;
	return new URL(GrixProperty.getString("openca.base.url") + m.group(2));
    }

    /**
     * Download a certificate from the OpenCA server.
     * 
     * @param req the certificate signing request that was sent
     * @param reqserial the serial number of the certificate signing request that was returned
     *                  by submission of the certificate signing request (not used by OpenCA) 
     * @return The X509Certificate signed by the certificate authority
     * @throws IOException
     */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, String reqserial) throws IOException {
	// find serial number first
	URL url_pre = new URL(GrixProperty.getString("openca.base.url"));
	String[] post_pre = new String[] {
		"cmd=search",
		"dataType=CERTIFICATE",
		// TODO probably not working since X509Name.toString() doesn't do the right thing
		"value_1="+req.getCertificationRequestInfo().getSubject().toString(),
		"value_2=",
		"value_3=",
		"value_4=",
		"value_5=",
		"name_1=DN",
		"name_2=EMAILADDRESS",
		"name_3=CN",
		"name_4=ROLE",
		"name_5=KEY",
		"pcounter=5",
	};
	String serial = null;
	String page_body = ConnectionUtils.pageContents(url_pre, post_pre, true);
	final Pattern p = Pattern.compile("<a href=([\"'])(.*?cmd=viewCert.*?)\1>");
	Matcher m = p.matcher(page_body);
	if (!m.matches()) {
	    logger.warning("Certificate not found on OpenCA site: "+req);
	    return null;
	}
	// TODO handle multiple matches, now takes the first one ...
	serial = m.group(1);

	if (serial == null) {
	    logger.fine("Serial not found. Can't download certificate.");
	    return null;
	}
	logger.fine("Serial found on OpenCA site: " + serial);

	// return certificate by serial
	URL url = new URL(GrixProperty.getString("openca.base.url"));
	String[] post = new String[] { 
	    "format_sendcert=pem",
	    "Submit=Download",
	    "GET_PARAMS_CMD=",
	    "cmd=sendcert",
	    "dataType=VALID_CERTIFICATE",
	    "name=PUBLIC",
	    "key="+serial,
	    "HIDDEN_key="+serial,
	    "passwd=",
	    "signature=",
	    "format=",
	    "text=",
	    "new_dn=",
	    "dn=",
	};
	Reader reader = ConnectionUtils.pageReader(url, post, true);
	PEMReader r = new PEMReader(reader);
	X509Certificate cert = (X509Certificate)r.readObject();
	r.close();
	return cert;
    }

    /**
     * Uploads a user certificate signing request onto an OpenCA server via http(s)
     * post.
     * 
     * @param req certification signing request
     * @param info extra information that may be sent with the request
     *        for OpenCA, only ra, department, phone and email are used.
     * @return serial of certificate signing request returned by CA
     * @throws IOException 
     */
    public String uploadCertificationRequest(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	
	// TODO check this is indeed a correct CN
	String cn = req.getCertificationRequestInfo().getSubject().toString();
	
	StringWriter reqWriter = new StringWriter();
	PEMWriter w = new PEMWriter(reqWriter);
	w.writeObject(req);
	w.close();
	
	String[] postdata = {
		"cmd=pkcs10_req",
		"operation=server-confirmed-form",
		"role=User",
		"ra="+info.getProperty("ra"),
		"passwd1=",
		"passwd2=",
		"loa=",
		"request="+reqWriter.toString(),
		"ADDITIONAL_ATTRIBUTE_DEPARTMENT="+info.getProperty("depertment"),
		"ADDITIONAL_ATTRIBUTE_TELEPHONE="+info.getProperty("phone"),
		"ADDITIONAL_ATTRIBUTE_EMAIL="+info.getProperty("email"),
		"ADDITIONAL_ATTRIBUTE_REQUESTERCN="+cn,
	};

	URL url = new URL(GrixProperty.getString("openca.base.url"));
	String answer = ConnectionUtils.pageContents(url, postdata, true);

	String serial = null;

	// TODO dodgy but I don't know how else to do it.
	try {
	    int index = answer.indexOf("serial") + 7;
	    if (index == -1 || answer.indexOf("error") != -1
		    || answer.indexOf("Error") != -1) {
		// means: not successful
		logger.severe("Could not upload certification request.");
		throw new /*UnableToUploadCertificationRequestException*/IOException(answer);
	    }
	    int index_end = answer.indexOf(" ", index);
	    serial = answer.substring(index, index_end);
	} catch (RuntimeException e) {
	    logger.severe(e.getMessage());
	    throw e;
	}

	logger.finest("Answer from server: \n\n" + answer);

	return serial;

    }
    
    public boolean isCertificationRequestProcessed(
	    PKCS10CertificationRequest req, String reqserial) throws IOException {
	return downloadCertificate(req, reqserial) != null;
    }
    
    // temporary property classes
    static class GrixProperty {
	static private Properties p = null;
	
	static private void checkSetup() {
	    if (p!=null) return;
	    p.setProperty("openca.base.url", "http://mm.cs.dartmouth.edu/cgi-bin/pki/pub/pki");
	}
	
	static public void setProperty(String key, String value) {
	    checkSetup();
	    p.setProperty(key, value);
	}
	static public String getString(String key) {
	    checkSetup();
	    return p.getProperty(key);
	}
    }
    static class UserProperty extends GrixProperty { }

}
