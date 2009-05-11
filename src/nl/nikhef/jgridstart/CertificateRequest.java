package nl.nikhef.jgridstart;

import java.util.Properties;

/** Helper class for requesting a new certificate. */
public class CertificateRequest {
    /** Set default properties based on an amount of guessing to aid
     * the user in filling in the form.
     * 
     * @param p Properties to set
     */
    static public void preFillData(Properties p) {
	// TODO implement prefillData()
    }
    
    /** Complete data entered before creating a certificate signing
     * request. This should be called before CertificateStore.generateRequest()
     * or CertificatePair.generateRequest() is called.
     * 
     * @param p Properties to update
     */
    static public void postFillData(Properties p) {
	// construct subject
	String subject = "";
	if (p.getProperty("level").equals("tutorial"))
	    subject += "O=edgtutorial";
	else
	    subject += "O=dutchgrid";
	if (p.getProperty("level").equals("demo"))
	    subject += ", O=dutch-demo";
	subject += ", O=users";
	subject += ", CN=" + p.getProperty("givenname").trim() +
	" " + p.getProperty("surname").trim();
	p.setProperty("subject", subject);
    }
}
