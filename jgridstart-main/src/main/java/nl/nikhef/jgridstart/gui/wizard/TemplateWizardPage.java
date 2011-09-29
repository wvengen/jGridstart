package nl.nikhef.jgridstart.gui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nl.nikhef.xhtmlrenderer.swing.TemplateDocument;

/** Page of a HTML-based wizard dialog.
 * <p>
 * This is a {@link TemplateDocument} embedded in a {@link TemplateWizard}.
 */
public class TemplateWizardPage extends TemplateDocument implements ITemplateWizardPage {
    
    /** Internal identifier of this page */
    protected String id = null;
    /** Source, only used when constructed with a {@linkplain URL} */
    protected URL url = null;
    /** Containing wizard, if any */
    protected ITemplateWizard wizard = null;
    /** Containing window */
    protected Component parent = null;

    /** Create new page from {@linkplain URL}, which is loaded immediately.
     * 
     * @param id internal identifier
     * @todo load in background
     */
    public TemplateWizardPage(String id, URL url) throws ParserConfigurationException, SAXException, IOException {
	super(retrieveDocument(url));
	this.id = id;
    }
    
    /** Create new page from {@linkplain Document} */
    public TemplateWizardPage(String id, Document template) {
	super(template);
	this.id = id;
    }
    
    /** Return the identifier of this page.
     * <p>
     * The identifier is used internally as a way to reference
     * this page, e.g. to select a certain wizard page.
     * <p>
     * The default implementation returns the id as specified in the
     * constructor.
     */
    public String getId() {
	return id;
    }

    /** Return title of this page.
     * <p>
     * This assumes that only one {@literal title} tag is present in the whole xml document.
     * If no {@literal title} tag is present at all,  {@code null} is returned. 
     */
    public String getTitle() {
	NodeList titles = getElementsByTagName("title");
	if (titles.getLength()<1)
	    return null;
	return titles.item(0).getTextContent();
    }
    
    /** Return title of this page used in the contents.
     * <p>
     * This is generally used in the list of steps in a wizard. Defaults to
     * {@linkplain #getTitle}, but can be customized in child classes.
     * 
     * @see #getTitle
     */
    public String getContentsTitle() {
	return getTitle();
    }
    
    /** Return whether this page is considered done or not.
     * <p>
     * If a certain page was already processed before, it is a good
     * idea to add a checkmark in front of the contents list. This 
     * method should return {@literal true} if this step needs to
     * action from the user anymore.
     * <p>
     * Also {@link ITemplateWizard#setPageDetect} uses this to display 
     * first page that returns {@literal false} here.
     * <p>
     * You can safely ignore this if you don't want the functionality.
     */
    public boolean isDone() {
	return false;
    }

    /**
     * Callback when leaving a page.
     * <p>
     * Default implementation returns {@link #validate} when user pressed "Next".
     * If that throws a {@link ValidationException} which a non-{@literal null}
     * message, an error dialog is shown and {@literal false} is returned.
     * 
     * @see #validate
     * @param newPage new page that will be shown
     * @param isNext is this is a result of a "Next" action
     * @return {@literal false} to stay on the current page
     */
    public boolean pageLeave(ITemplateWizardPage newPage, boolean isNext) {
	if (!isNext)
	    return true;
	// validate
	try {
	    validate();
	} catch(ValidationException e) {
	    if (e.getLocalizedMessage()!=null) {
		JOptionPane.showMessageDialog(getParent(),
			e.getLocalizedMessage(),
			"Form incomplete", JOptionPane.ERROR_MESSAGE);
	    }
	    return false;
	}
	return true;
    }
    
    /** Callback when entering a page.
     * <p>
     * Default implementation does nothing.
     * 
     * @param oldPage old page that was shown, or {@literal null} if this
     *          is the first page shown.
     */
    public void pageEnter(ITemplateWizardPage oldPage) {
    }
    
    /** Validate page contents.
     * <p>
     * This is called by default when a page is left. This method should
     * throw a {@linkplain ValidationException} if the contents of this
     * wizard page cannot be validation (this would generally be user input).
     * If the exception has a message that is not {@literal null}, it will
     * be shown to the user in a dialog box. If the exception message has
     * been set explicitely to {@literal null}, no message is shown but
     * {@linkplain #pageLeave} still returns {@literal false} so that the
     * user remains on the current page; this is to handle error handling
     * in the page itself.
     *
     * @throws ValidationException if validation did not succeed
     */
    public void validate() throws ValidationException {
    }
    
    public ITemplateWizard getWizard() {
	assert(wizard!=null);
	return wizard;
    }
    public Component getParent() {
	return parent;
    }
    public void associate(ITemplateWizard wizard, Component parent) {
	assert(wizard!=null);
	this.wizard = wizard;
	this.parent = parent;
    }
    
    /** Returns a {@linkplain Document} for a {@linkplain URL}.
     * <p>
     * This version explicitly allows the use of default caches to enable
     * resource caching from URLs obtained with {@link Class#getResource}. 
     */
    static protected Document retrieveDocument(URL url) throws SAXException, IOException, ParserConfigurationException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	URLConnection conn = url.openConnection();
	conn.setDefaultUseCaches(true);
	InputStream in = conn.getInputStream();
	try {
	    Document doc = factory.newDocumentBuilder().parse(in);
	    doc.setDocumentURI(url.toExternalForm());
	    return doc;
	} finally {
	    in.close();
	}
    }
}
