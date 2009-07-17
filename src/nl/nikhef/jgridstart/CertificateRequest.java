package nl.nikhef.jgridstart;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import nl.nikhef.xhtmlrenderer.swing.TemplateDocument;

/** Helper class for requesting a new certificate. */
public class CertificateRequest {

    /** System properties prefix for prefill defaults */
    public static final String defaultsPrefix = "jgridstart.defaults.";
    
    /** Set default properties based on an amount of guessing to aid
     * the user in filling in the form.
     * <p>
     * First properties are copied from its parent, if any. If the parent
     * has no such property, it looks at system properties
     * {@literal jgridstart.defaults.*} and sets defaults from these.
     * If a property already exists, it is not overwritten.
     * <p>
     * The parent is meant for renewing certificates, where most properties
     * need to be copied from the parent certificate, but not all.
     * 
     * @param p Properties to set
     * @param parent Parent Properties to copy from 
     */
    static public void preFillData(Properties p, Properties parent) {
	// copy relevant values from parent, if any
	if (parent!=null) {
	    for (Iterator<Entry<Object, Object>> it = parent.entrySet().iterator(); it.hasNext(); ) {
		Entry<Object, Object> entry = it.next();
		String name = (String)entry.getKey();
		String value = (String)entry.getValue();
		// filter out state properties that shouldn't be copied
		if (name.equals("request.submitted")) continue;
		if (name.equals("request.processed")) continue;
		if (name.equals("install.done")) continue;
		// copy if unset
		if (!p.containsKey(name))
		    p.setProperty(name, value);
	    }
	}
	// read defaults from system properties
	for (Iterator<Entry<Object, Object>> it = System.getProperties().entrySet().iterator(); it.hasNext(); ) {
	    Entry<Object, Object> entry = it.next();
	    String name = (String)entry.getKey();
	    String value = (String)entry.getValue();
	    if (name.startsWith(defaultsPrefix) ) {
		String localName = name.substring(defaultsPrefix.length()+1);
		if (!p.containsKey(localName))
		    p.setProperty(name.substring(defaultsPrefix.length()), value);
	    }
	}
    }
    /** Set default properties to aid user in filling in the form.
     * <p>
     * This is equal to {@link #preFillData(Properties, Properties) preFillData(Properties, null)}.
     * 
     * @see #preFillData(Properties, Properties)
     * @param p Properties to set
     */
    static public void preFillData(Properties p) {
	preFillData(p);
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
	subject += ", O=" + p.getProperty("org");
	subject += ", CN=" + p.getProperty("givenname").trim() +
			" " + p.getProperty("surname").trim();
	// simulate x-full propery from certificate to generate request
	p.setProperty("subject", subject);
	p.setProperty("subject.volatile", "true");
    }
    
    /** Lock fields on which the request is dependent.
     * <p>
     * When the CSR is generated, some fields should not be changed 
     * anymore since these have become part of the request. This
     * method should set all {@code foo.locked} variables so that
     * the fields cannot be edited in {@linkplain TemplateDocument}s. 
     */
    static public void postFillDataLock(Properties p) {
	p.setProperty("givenname.lock", Boolean.toString(true));
	p.setProperty("surname.lock", Boolean.toString(true));
	p.setProperty("subject.lock", Boolean.toString(true));
	p.setProperty("level.lock", Boolean.toString(true));
	p.setProperty("org.lock", Boolean.toString(true));
    }
}
