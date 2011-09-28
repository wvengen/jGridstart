package nl.nikhef.jgridstart.gui.wizard;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.LinkListener;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;

/** Wizard base class for guiding the user through the certificate request process.
 * <p>
 * The request wizard that is a key piece of jGridstart is implemented as a child
 * of this class. Each certificate authority usually has its own request process
 * and instructions. The implementation of the wizard is an {@link IRequestWizard},
 * specified by the system property <tt>jgridstart.requestwizard.provider</tt>
 * containing a fully qualified class name. Typically such an implementation is
 * a child of {@linkplain RequestWizardCommon}, which adds an interface for
 * accessing {@link CertificatePair (parent) certificate},
 * {@link CertificateStore store} and {@link CertificateSelection selection}.
 * Also a title is set.
 * <p>
 * The wizard is based on {@link TemplateWizard}, which uses html files for
 * page contents. 
 */
public class RequestWizardCommon extends TemplateWizard implements IRequestWizard {

    /** CertificateStore to operate on */
    protected CertificateStore store = null;
    /** CertificateSelection to select newly requested certificate*/
    protected CertificateSelection selection = null;
    /** the resulting CertificatePair, or null if not yet set */
    protected CertificatePair cert = null;
    /** the parent CertificatePair in case of a renewal */
    protected CertificatePair certParent = null;
    
    /** Create a request wizard.
     * <p>
     * This is the only publicly accessible method to create a new
     * request wizard. The global property {@literal jgridstart.requestwizard.provider}
     * is used to lookup the actual implementation to use, that's why no
     * ordinary constructor can be used.
     * <p>
     * Be sure to create a constructor for each implementation with both
     * a Frame as Dialog as argument.
     */
    static public RequestWizardCommon createInstance(Frame parent, CertificateStore store, CertificateSelection sel, CertificatePair cert, CertificatePair certParent) throws ClassNotFoundException, RuntimeException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException  {
	Class<?> clazz = Class.forName(System.getProperty("jgridstart.requestwizard.provider"));
	Constructor<?> constr = clazz.getDeclaredConstructor(new Class[]{Frame.class});
	constr.setAccessible(true); // to be able to call protected methods -- why needed?!?!?
	RequestWizardCommon wizard = (RequestWizardCommon)constr.newInstance(new Object[]{parent});
	wizard.construct(store, sel, cert, certParent);
	return wizard;
    }
    static public RequestWizardCommon createInstance(Frame parent, CertificateStore store, CertificateSelection sel, CertificatePair cert) throws ClassNotFoundException, RuntimeException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
	return createInstance(parent, store, sel, cert, null);
    }
    static public RequestWizardCommon createInstance(Frame parent, CertificateStore store, CertificateSelection sel) throws ClassNotFoundException, RuntimeException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
	return createInstance(parent, store, sel, null);
    }
    static public RequestWizardCommon createInstance(Dialog parent, CertificateStore store, CertificateSelection sel, CertificatePair cert, CertificatePair certParent) throws ClassNotFoundException, RuntimeException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException  {
	Class<?> clazz = Class.forName(System.getProperty("jgridstart.requestwizard.provider"));
	Constructor<?> constr = clazz.getDeclaredConstructor(new Class[]{Dialog.class});
	RequestWizardCommon wizard = (RequestWizardCommon)constr.newInstance(new Object[]{parent});
	wizard.construct(store, sel, cert, certParent);
	return wizard;
    }
    static public RequestWizardCommon createInstance(Dialog parent, CertificateStore store, CertificateSelection sel, CertificatePair cert) throws ClassNotFoundException, RuntimeException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
	return createInstance(parent, store, sel, cert, null);
    }
    static public RequestWizardCommon createInstance(Dialog parent, CertificateStore store, CertificateSelection sel) throws ClassNotFoundException, RuntimeException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
	return createInstance(parent, store, sel, null);
    }
    /** initialize from parameters @see #createInstance(Frame, CertificateStore, CertificateSelection, CertificatePair, CertificatePair) */
    private void construct(CertificateStore store, CertificateSelection sel, CertificatePair cert, CertificatePair certParent) {
	this.store = store;
	this.selection = sel;
	if (cert!=null) {		// view existing request
	    this.cert = cert;
	    setData(cert);
	} else if (certParent!=null) {	// renewal request
	    this.certParent = certParent;
	    setData(new Properties());
	    data().setProperty("renewal", Boolean.toString(true));
	} else {			// new request
	    setData(new Properties());
	}
    }
    
    protected RequestWizardCommon(Frame parent) { super(parent); }
    protected RequestWizardCommon(Dialog parent) { super(parent); }

    @Override
    protected void initialize() {
	super.initialize();
	setPreferredSize(new Dimension(800, 550));
	// extra special handling of "action:" links
	replaceLinkListener(new LinkListener() {
	    @Override
            public void linkClicked(BasicPanel panel, String uri) {
		// select certificate so that any "action:" links are executed
		// on the correct certificate.
		if (uri.startsWith("action:"))
		    selection.setSelection(cert);
		// go!
		URLLauncherCertificate.openURL(uri, panel);
		// refresh document after action because properties may be updated
		if (uri.startsWith("action:"))
		    refresh();
	    }	    
	});
    }
    
    @Override
    public void setData(Properties p) {
	super.setData(p);
	// also set static properties for the forms
	// initialize properties when new request / renewal
	if (cert==null) {
	    // help the user by prefilling some elements
	    if (!Boolean.valueOf(data().getProperty("renewal"))) {
	    	data().setProperty("wizard.title", "Request a new certificate");
	    } else {
    	    	data().setProperty("wizard.title", "Renew a certificate");
	    }
	} else {
	    if (!data().containsKey("wizard.title"))
		data().setProperty("wizard.title", "Certificate Request");
	}
	data().setProperty("wizard.title.volatile", "true");
	data().setProperty("wizard.title.html", data().getProperty("wizard.title"));
	data().setProperty("wizard.title.html.volatile", "true");
	// workaround for checkboxes without a name; even with checked="checked" they
	// would sometimes not be shown as checked (irregular behaviour though)
	data().setProperty("true", "true");
	data().setProperty("true.volatile", "true");
    }
    
    public CertificatePair getCertificate() {
	return cert;
    }
    public void setCertificate(CertificatePair cert) {
	assert(this.cert==null);
	this.cert = cert;
    }
    public CertificatePair getParentCertificate() {
	return certParent;
    }
    public CertificateStore getStore() {
	return store;
    }
    public CertificateSelection getSelection() {
	return selection;
    }
    
    public IRequestWizard getWizard() {
	return this;
    }

    /** Only add {@linkplain IRequestWizardPage}s */
    @Override
    public void addPage(ITemplateWizardPage page) {
	super.addPage(page);
    }
}