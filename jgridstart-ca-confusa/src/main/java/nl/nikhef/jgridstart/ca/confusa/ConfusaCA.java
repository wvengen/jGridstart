package nl.nikhef.jgridstart.ca.confusa;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.nikhef.jgridstart.ca.CA;
import nl.nikhef.jgridstart.osutils.ConnectionUtils;
import nl.nikhef.jgridstart.util.GeneralUtils;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.OAuthProviderListener;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpRequest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is used interface with the Confusa online CA
 * <p>
 * TODO finish details
 * 
 * @author wvengen
 */
public class ConfusaCA implements CA {
    
    static final protected Logger logger = Logger.getLogger(ConfusaCA.class.getName());
    
    /** CA entry point: CA cert (no OAuth required for this) */
    protected String baseCaCert = null;
    /** CA entry point: submission&query (requires OAuth access token) */
    protected String baseCert = null;
    /** CA entry point: user info (requires OAuth access token) */
    protected String baseInfo = null;
    
    /** CA certificate (cached) */
    protected static X509Certificate cacert = null;
    /** OAuth consumer */
    protected OAuthConsumer consumer = null;
    /** OAuth provider */
    protected OAuthProvider provider = null;
    /** http client */
    protected HttpClient client = null;
    /** if a valid OAuth access token is present */
    protected boolean oauthDone = false;
    /** cached user info properties */
    protected Properties userInfo = null;

    /** Create new ConfusaCA */
    public ConfusaCA() throws NoSuchAlgorithmException, KeyManagementException {
	String base = System.getProperty("jgridstart.ca.base");
	base = dflProp("jgridstart.ca.base", "https://beta.confusa.org/confusa/");
	baseCert = dflProp("jgridstart.ca.base.cert", base + "api/certificates.php");
	baseInfo = dflProp("jgridstart.ca.base.info", base + "api/infopoint.php");
	baseCaCert = dflProp("jgridstart.ca.base.cacert", base + "root_cert.php?link=cacert");
	
	// client can be re-used between OAuth and requests (pipelining)
	//   and we want to set a custom http user-agent string
	client = new DefaultHttpClient();
	String httpua = GeneralUtils.getUserAgentString((String)client.getParams().getParameter("http.useragent"));
	logger.fine("ConfusaCA user-agent: "+httpua);
	client.getParams().setParameter("http.useragent", httpua);
	
	consumer = new CommonsHttpOAuthConsumer(
		dflProp("jgridstart.ca.oauth.key", "key"),
		dflProp("jgridstart.ca.oauth.secret", "secret"));
	
	provider = new CommonsHttpOAuthProvider(
		dflProp("jgridstart.ca.oauth.requesturl", base + "api/oauth.php/request"),
		dflProp("jgridstart.ca.oauth.accessurl", base + "api/oauth.php/access"),
		dflProp("jgridstart.ca.oauth.authorizeurl", base + "api/oauth.php/authorize"),
		client);
	
	// workaround for simplesamlphp returning 200 with error in html body
	provider.setListener(new OAuthProviderListener() {
	    public void prepareSubmission(HttpRequest request) throws Exception { }
	    public void prepareRequest(HttpRequest request) throws Exception { }
	    public boolean onResponseReceived(HttpRequest request,
		    oauth.signpost.http.HttpResponse response) throws Exception {
		HttpResponse cresponse = (HttpResponse)response.unwrap();
		StringBuilder sb = new StringBuilder(); 
		
		// TODO use apache commons IOUtils#toString
		InputStream is = response.getContent();
		String line;
		try {
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		    while ((line = reader.readLine()) != null)
			sb.append(line).append("\n");
		} finally {
		    is.close();
		}
		if (sb.toString().contains("Fatal error")) {
		    String txt = sb.toString();
		    txt = txt.replaceAll("<.*?>", "");
		    throw new IOException("Could not obtain OAuth access token:\n"+txt);
		}
		
		if (!cresponse.getEntity().isRepeatable()) {
		    // create a repeatable entity to avoid signpost error
		    cresponse.setEntity(new StringEntity(sb.toString()));
		}
		
		return false;
	    }
	});
    }
    
    private String dflProp(String prop, String newval) {
	String val = System.getProperty(prop);
	if (val==null) {
	    val = newval;
	    System.setProperty(prop, newval);
	}
	return val;
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
    
    /** PEM-encodes a renewal (not supported).
     * <p>
     * Renewals do not really make sense for this CA, so they just encode
     * the request and not sign it. */
    public String signCertificationRequest(
	    PKCS10CertificationRequest req, Properties info,
	    PrivateKey oldKey, X509Certificate oldCert) throws IOException {
	return encodeCertificationRequest(req, info);
    }

    /** Uploads a user certificate signing request onto the Confusa CA */
    public void uploadCertificationRequest(String req, Properties info) throws IOException {

	// create xml to encapsulate request
	Document xmlreq;
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    xmlreq = factory.newDocumentBuilder().newDocument();
	} catch(ParserConfigurationException e) {
	    throw new IOException("Could not create request xml document", e);
	}
	Element elreq = xmlreq.createElement("signingRequest");
	xmlreq.appendChild(elreq);
	elreq.appendChild(xmlreq.createTextNode(req));
	Element emails = xmlreq.createElement("emails");
	elreq.appendChild(emails);
	if (info.getProperty("email")!=null) {
	    Element email = xmlreq.createElement("email");
	    email.appendChild(xmlreq.createTextNode(info.getProperty("email")));
	    emails.appendChild(email);
	}
	
	// output document to string
	StreamResult xmlresult = null;
	try {
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	     xmlresult = new StreamResult(new StringWriter());
	    transformer.transform(new DOMSource(xmlreq), xmlresult);
	} catch (TransformerException e) {
	    throw new IOException("Could not encode request to xml", e);	    
	}

	// and post request
	String[] postdata = { "request", xmlresult.getWriter().toString() };
	HttpResponse result = oauthPost(baseCert, postdata);
	try {
	    // must contain location url for certificate now
	    Header[] locations = result.getHeaders("Location");
	    if (locations.length<1)
		throw new IOException("Missing certificate location, although submission seemed succesful.");
	    if (locations.length>1)
		logger.warning("Multiple ("+Integer.toString(locations.length)+") certificate locations, using first one.");
	    info.setProperty("request.certurl", result.getHeaders("Location")[0].getValue());
	    // XXX for debugging
	    logger.info(readEntity(result.getEntity()));
	} finally {
	    // to release connection so it can be reused later
	    result.getEntity().consumeContent();
	}
	logger.info("Uploaded certificate signing request");
    }
    
    public boolean isCertificationRequestProcessed(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	return downloadCertificate(req, info) != null;
    }

    /** Download a certificate from the DutchGrid CA
     * <p>
     * Request must have been uploaded before using {@link #uploadCertificationRequest}
     * so that the property <tt>request.certurl</tt> is set. The certificate is
     * downloaded from this location.
     * 
     * @param req {@inheritDoc}
     * @param info {@inheritDoc} (not used by DutchGridCA)
     * @return {@inheritDoc}
     */
    public X509Certificate downloadCertificate(
	    PKCS10CertificationRequest req, Properties info) throws IOException {
	
	String certurl = info.getProperty("request.certurl");
	if (certurl==null)
	    throw new IOException("Cannot retrieve certificate: no certificate url specified.");
	
	// download with OAuth authentication
	HttpResponse result = oauthPost(certurl, null);
	try {
	    String scert = null;

	    for (NameValuePair p: URLEncodedUtils.parse(result.getEntity())) {
		if ("cert".equals(p.getName()))
		    scert = p.getValue();
	    }

	    if (scert==null)
		throw new IOException("Certificate authority returned success but no certificate, weird.");

	    StringReader reader = new StringReader(scert);
	    PEMReader r = new PEMReader(reader);
	    X509Certificate cert = (X509Certificate)r.readObject();
	    r.close();
	    if (cert==null)
		throw new IOException("Certificate could not be parsed: "+scert);

	    return cert;

	} finally {
	    // to release connection so it can be reused later
	    result.getEntity().consumeContent();
	}
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
    
    /** return page contents with OAuth authentication.
     * <p>
     * Assumes access token has been acquired already.
     */
    protected HttpResponse oauthPost(String url, String[] data) throws IOException {	
	// Now do authenticated post
	HttpPost req = new HttpPost(url);
	ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
	for (int i=0; data!=null && i<data.length; i+=2) {
	    params.add(new BasicNameValuePair(data[i], data[i+1]));
	}
	req.setEntity(/*new UrlEncodedFormEntity*/ new StringEntity(params.toString()));
	try {
	    consumer.sign(req);
	} catch (OAuthException e) {
	    throw new IOException(e);
	}

	HttpResponse result = client.execute(req);
	
	// now handle error
	if (result.getStatusLine().getStatusCode()!=200) {
	    String errmsg = "Interaction with certificate authority failed";
	    String trace = "";
	    errmsg += " (code "+result.getStatusLine().getStatusCode()+
	    		": "+result.getStatusLine().getReasonPhrase()+")";
	    for (NameValuePair p: URLEncodedUtils.parse(result.getEntity())) {
		if ("message".equals(p.getName()))
		    errmsg += ": " + p.getValue();
		if ("exception".equals(p.getName()))
		    trace += p.getValue();
	    }
	    // consume content for detailed error message
	    errmsg += ":\n" + readEntity(result.getEntity()).replaceAll("<.*?>", "");

	    if (trace!="") logger.warning("  trace:"+trace);
	    else logger.warning(errmsg);
	    throw new IOException(errmsg);
	}
	
	return result;
    }
    
    /** Return http entity as string */
    static private String readEntity(HttpEntity entity) throws IOException {
	// TODO use apache commons IOUtils#toString
	InputStream is = entity.getContent();
	StringBuffer sb = new StringBuffer();
	String line;
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    while ((line = reader.readLine()) != null)
		sb.append(line).append("\n");
	} finally {
	    is.close();
	}
	return sb.toString();
    }
    
    /** Return whether already logged in or not. */
    public boolean loginIsDone() {
	// TODO doesn't catch login timeout ...
	return oauthDone;
    }
    
    /** Prepare login.
     * <p>
     * Returns url that should be visited by user to obtain access.
     * Make sure that you don't call this when an access token is present
     * already (and {@link #loginIsDone} returns {@literal true}),
     * or the user will need to login again.
     * 
     * <p>TODO use IOException & CAPasswordWrongException oid
     */
    public URL loginPrepare() throws OAuthException, MalformedURLException {
	logger.fine("Requesting OAuth request token");
	return new URL(provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND));
    }
    
    /** Process login.
     * <p>
     * Processes the login. Out-of-band authentication with verifier not supported. 
     *
     * <p>TODO use IOException & CAPasswordWrongException oid
     * 
     * @param notused1 should be {@literal null}
     * @param notused2 should be {@literal null}
     */
    public void loginProcess(String notused1, String notused2) throws OAuthException {
	logger.fine("Retrieving OAuth access token");
	provider.retrieveAccessToken(consumer, null);
	logger.fine("OAuth access token retrieved");
	oauthDone = true;
    }
    
    /** Retrieve user info.
     * <p>
     * This only works after a successful login. The result it cached.
     */
    public Properties getUserInfo() throws IOException {
	if (userInfo==null) {
	    // get info
	    HttpResponse response = oauthPost(baseInfo + "/dn/openssl", null);
	    String dn = readEntity(response.getEntity());
	    if (!dn.startsWith("DN="))
		throw new IOException("Could not retrieve user DN.\n "+dn);
	    dn = dn.substring(4);
	    response = oauthPost(baseInfo + "/user", null);
	    String uinf = readEntity(response.getEntity());
	    Document xuinf = null;
	    
	    // parse xml
	    try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		xuinf = factory.newDocumentBuilder().parse(new ByteArrayInputStream(uinf.getBytes()));
	    } catch (ParserConfigurationException e) {
		throw new IOException("Could not parse user info xml", e);
	    } catch (SAXException e) {
		throw new IOException("Could not parse user info xml", e);
	    }
	    
	    // set as properties
	    Properties p = new Properties();
	    p.setProperty("subject", dn.trim());
	    p.setProperty("uid", xuinf.getElementsByTagName("uid").item(0).getTextContent().trim());
	    p.setProperty("fullname", xuinf.getElementsByTagName("name").item(0).getTextContent().trim());
	    p.setProperty("org", xuinf.getElementsByTagName("orgDN").item(0).getTextContent().trim());
	    p.setProperty("country", xuinf.getElementsByTagName("country").item(0).getTextContent().trim());
	    NodeList emails = xuinf.getElementsByTagName("emails");
	    if (emails.getLength()>1)
		logger.warning("Found "+emails.getLength()+" email addresses, using only first one.");
	    if (emails.getLength()==1)
		p.setProperty("email", emails.item(0).getTextContent().trim());
	    else
		logger.info("No email address available for user");
	    userInfo = p;
	}
	return userInfo;
    }
}
