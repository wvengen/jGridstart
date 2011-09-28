package nl.nikhef.jgridstart.gui.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.nikhef.jgridstart.gui.util.TemplateButtonPanel;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;
import nl.nikhef.xhtmlrenderer.swing.TemplateDocument;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xml.sax.SAXException;

/** HTML-based wizard dialog.
 * <p>
 * This is an {@link ITemplatePanel} with next/previous buttons by which
 * the user can walk through a list of pages. The pages that make up the
 * wizard are defined in the member {@link #pages}.
 * <p>
 * When you want to do something on a page change, use {@link #setHandler} with
 * a {@link PageListener} interface as argument. One can use {@link #setStep} and
 * {@link #setStepRelative} to change the current page.
 * <p>
 * To give the user an overview of the available steps in the wizard, there is
 * a variable {@code wizard.contents.html} which can be inserted in each step
 * using the default {@link TemplateDocument template} substitutions. It can be
 * fixed to the viewport with css like <code>position: fixed</code>.
 * {@link #updateWizardProperties} generates this variable, which can be customized
 * by derived classes by overriding either that method, or {@link #getWizardContentsLine}.
 * <p>
 * The default implementation of {@linkplain #getWizardContentsLine} retrieves the
 * HTML document title for each page to construct the overview of the available
 * steps.
 */
public class TemplateWizard extends JDialog implements ITemplatePanel {

    /** wizard page templates for each step */
    public ArrayList<URL> pages = new ArrayList<URL>();
    /** list of loaded documents */
    private ArrayList<TemplateDocument> docs = new ArrayList<TemplateDocument>();
    /** currently active step (base 0), -1 for not initialized */
    protected int step = -1;
    /** actual dialog content with HTML and buttons */
    protected TemplateButtonPanel pane = null;
    protected JPanel btnRight = null;
    protected JPanel btnLeft = null;
    /** "Next" action */
    protected Action nextAction = null;
    /** "Previous" action */
    protected Action prevAction = null;
    /** "Cancel"/"Close" action */
    protected Action cancelAction = null;
    /** Listener for page changes */
    protected PageListener handler = null;

    // constructors
    public TemplateWizard()               { super(); initialize(); }
    public TemplateWizard(Dialog owner)   { super(owner); initialize(); }
    public TemplateWizard(Frame owner)    { super(owner); initialize(); }
    public TemplateWizard(Properties p)   { this(); setData(p); }
    public TemplateWizard(Dialog owner, Properties p) { this(owner); setData(p); }
    public TemplateWizard(Frame owner, Properties p)  { this(owner); setData(p);  }
    
    /** return the TemplateDocument by index.
     * <p>
     * The document is loaded on demand. Always use this method to access
     * the document, or else it may not be loaded.
     *
     * @return loaded document on success, {@code null} on failure
     */
    protected TemplateDocument getDocument(int i)  {
	// load when needed
	while (docs.size() <= i) docs.add(null);
	if (docs.get(i)==null || !docs.get(i).getDocumentURI().equals(pages.get(i).toExternalForm()) ) {
	    try {
		Document src = retrieveDocument(pages.get(i));
		TemplateDocument doc = new TemplateDocument(src, data());
		doc.setDocumentURI(pages.get(i).toExternalForm());
		docs.set(i, doc);
		return doc;
	    } catch (SAXException e) {
	    } catch (IOException e) {
	    } catch (ParserConfigurationException e) {
	    }
	    return null;
	}
	return docs.get(i);
    }
    /** Returns the title of a step */
    protected String getDocumentTitle(int step) {
	return getSharedContext().getNamespaceHandler().getDocumentTitle(getDocument(step));	
    }
    
    /** set the currently displayed page */
    // TODO describe behaviour, especially what happens in last step
    //      (based on handler being present or not)
    public void setStep(int s) {
	int oldStep = step;
	// call pre-hook first
	if (handler!=null && step>=0 && !handler.pageLeave(this, oldStep, s))
	    return;
	
	// handle final "Close" step which just quits the dialog
	if (s == pages.size()) {
	    if (handler!=null)
		handler.pageEnter(this, oldStep, s);
	    else
		dispose();
	    return;
	}
	step = s;
	// Update the ui
	updateWizardProperties(step);
	setTitle(pane.getDocumentTitle());
	// no "Previous" at start; no "Next" beyond the final "Close"
	if (s == pages.size()-1)
	    cancelAction.putValue(Action.NAME, "Close");
	nextAction.setEnabled(step < (pages.size()-1) );
	prevAction.setEnabled(step > 0);
	// enter handler so it can update properties
	if (handler!=null) handler.pageEnter(this, oldStep, s);
	// set new contents and get title from that as well
	getDocument(s).refresh();
	pane.setDocument(getDocument(s));
	
	// pack here to give child classes a chance to setPreferredSize()
	// in their constructors or in setStep(). This is only called if
	// the window is not yet visible because pack()ing resets the
	// dialog size to its preferred size, which is unwanted when the
	// user resized the window.
	if (!isVisible()) pack();
    }
    /** go to another page by relative distance */
    public void setStepRelative(int delta) {
	if (step < 0)
	    step = 0;
	setStep(step + delta);
    }
    
    /** Update the wizard properties for a step.
     * <p>
     * This generaties the properties {@code wizard.*} which can be
     * referenced in the template. The following variables are defined
     * by default:
     * <ul>
     *   <li>{@code wizard.contents.html} - list of pages (html)</li>
     * </ul>
     * <p>
     * Note that property {@code wizard.contents.html.volatile} are
     * set to {@code true} to avoid it being saved to disk for persistent
     * property stores (like jGridstart's CertificatePair).
     * <p>
     * Derived classes may add and/or override wizard properties. Please
     * make sure to set the {@code wizard.foo.html.volatile} property
     * as well to avoid property clutter.
     * 
     * @param step step for which the contents is displayed; the default
     *             implementation marks the currently displayed step. 
     */
    public void updateWizardProperties(int step) {
	String v = "<ul>\n";
	
	for (int i=0; i<pages.size(); i++)
	    v += getWizardContentsLine(i, step);
	
	v += "</ul>\n";
	data().setProperty("wizard.contents.html", v);
	data().setProperty("wizard.contents.html.volatile", "true");
    }
    /** Returns the contents line for a page.
     * <p>
     * Default implementation returns a li item with an optional class
     * {@code wizard-current} if it is the current page, or
     * {@code wizard-future} if it is a step later than the current one.
     * 
     * @param step step to return contents for; index in {@link #pages}
     * @param current the currently active step
     */
    protected String getWizardContentsLine(int step, int current) {
	String classes = "";
	if (step==current) classes += " wizard-current";
	if (step>current)  classes += " wizard-future";
	return (classes=="" ? "<li>" : "<li class='"+classes+"'>") + getDocumentTitle(step) + "</li>\n";
    }

    /** Shows or hides the dialog.
     * <p>
     * This also sets the current step to the first one if none is selected
     * as of yet (only when there are pages defined).
     */
    @Override
    public void setVisible(boolean visible) {
	// need to setup some stuff if no step selected
	if (visible && step < 0 && pages.size() > 0)
	    setStep(0);
	super.setVisible(visible);
    }
    
    /** Initialize and build the dialog
     * 
     * The background colour is set to the default control background colour as
     * specified by UIManager. It is still possible to override it in CSS/HTML.
     */
    protected void initialize() {
	//setModal(true);
	getContentPane().removeAll();
	pane = new TemplateButtonPanel();
	getContentPane().add(pane);

	// create two button areas: one for prev/next to the right, one for
	// extra buttons at the left.
	btnLeft = new JPanel();
	btnLeft.setLayout(new BoxLayout(btnLeft, BoxLayout.X_AXIS));
	btnRight = new JPanel();
	btnRight.setLayout(new BoxLayout(btnRight, BoxLayout.X_AXIS));
	JPanel bpanel = pane.getButtonPane();
	bpanel.removeAll();
	bpanel.add(btnLeft, null);
	bpanel.add(Box.createHorizontalGlue());
	bpanel.add(btnRight, null);	
	
	// "Previous" button
	prevAction = new AbstractAction("Previous") {
	    public void actionPerformed(ActionEvent e) {
		setStepRelative(-1);
	    }
	};
	prevAction.putValue(Action.MNEMONIC_KEY, new Integer('P'));
	
	JButton prevButton = new JButton(prevAction);
	prevButton.setName("wizard_previous");
	pane.addButton(btnRight, prevButton, false, false);
	// "Next" button; name and mnemonic set in setStep()
	nextAction = new AbstractAction("Next") {
	    public void actionPerformed(ActionEvent e) {
		setStepRelative(1);
	    }
	};
	nextAction.putValue(Action.MNEMONIC_KEY, new Integer('N'));
	JButton nextButton = new JButton(nextAction);
	nextButton.setName("wizard_next");
	pane.addButton(btnRight, nextButton, true, false);
	// close window on escape
	btnRight.add(Box.createRigidArea(new Dimension(pane.btnBorderWidth*8, 0)));
	cancelAction = new AbstractAction("Cancel") {
	    public void actionPerformed(ActionEvent e) {
		TemplateWizard.this.dispose();
	    }
	};
	cancelAction.putValue(Action.MNEMONIC_KEY, new Integer('C'));
	JButton cancelButton = new JButton(cancelAction);
	cancelButton.setName("wizard_close");
	pane.addButton(btnRight, cancelButton, true);
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", cancelAction);
    }
    
    /** set the handler to be called when a page switch is done.
     * The handler is called after the page has been shown and can be
     * used to modify the dialog contents. */
    public void setHandler(PageListener l) {
	handler = l;
    }
    
    public interface PageListener {
	/** Called before a page is to be changed
	 * 
	 * @param w Current TemplateWizard
	 * @param curPage Page number almost leaving (index in pages)
	 * @param newPage Page number going to (index in pages)
	 * @return {@code true} to continue, {@code false} to stay on current page
	 */
	boolean pageLeave(TemplateWizard w, int curPage, int newPage);

	/** Called after page was changed.
	 * <p>
	 * This is called after the page has been shown and can be used to
	 * modify the dialog contents.
	 * 
	 * @param w Current TemplateWizard
	 * @param prevPage Page number previously shown (index in pages)
	 * @param curPage Page number just shown (index in pages)
	 */
	void pageEnter(TemplateWizard w, int prevPage, int curPage);
    }
    
    /** Returns a {@linkplain Document} for a {@linkplain URL}.
     * <p>
     * This version explicitly allows the use of default caches to enable
     * resource caching from URLs obtained with {@link Class#getResource}. 
     */
    protected Document retrieveDocument(URL url) throws SAXException, IOException, ParserConfigurationException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	URLConnection conn = url.openConnection();
	conn.setDefaultUseCaches(true);
	InputStream in = conn.getInputStream();
	try {
	    return factory.newDocumentBuilder().parse(in);
	} finally {
	    in.close();
	}
    }

    @Override
    public void setBackground(Color c) {
	super.setBackground(c);
	if (pane!=null) pane.setBackground(c);
    }
    /*
     * 
     * Delegates
     * 
     */
    public void addMouseTrackingListener(FSMouseListener l) {
	pane.addMouseTrackingListener(l);
    }

    public void componentHidden(ComponentEvent e) {
	pane.componentHidden(e);
    }

    public void componentMoved(ComponentEvent e) {
	pane.componentMoved(e);
    }

    public void componentResized(ComponentEvent e) {
	pane.componentResized(e);
    }

    public void componentShown(ComponentEvent e) {
	pane.componentShown(e);
    }

    public Properties data() {
	return pane.data();
    }

    public Document getDocument() {
	return pane.getDocument();
    }

    public String getDocumentTitle() {
	return pane.getDocumentTitle();
    }

    public List<?> getMouseTrackingListeners() {
	return pane.getMouseTrackingListeners();
    }

    public SharedContext getSharedContext() {
	return pane.getSharedContext();
    }

    public URL getURL() {
	return pane.getURL();
    }

    public boolean isActive(Element e) {
	return pane.isActive(e);
    }

    public boolean isFocus(Element e) {
	return pane.isFocus(e);
    }

    public boolean isHover(Element e) {
	return pane.isHover(e);
    }

    public boolean print() throws PrinterException {
	return pane.print();
    }

    /** {@inheritDoc}
     * <p>
     * Actually calls {@link #setStep} so that the {@link PageListener}
     * and other hooks are properly updated.
     * 
     * @return always returns {@code true}
     */
    public boolean refresh() {
	setStep(step);
	return true;
    }

    public void reloadDocument(Document doc) {
	pane.reloadDocument(doc);
    }

    public void reloadDocument(String URI) {
	pane.reloadDocument(URI);
    }

    public void removeMouseTrackingListener(FSMouseListener l) {
	pane.removeMouseTrackingListener(l);
    }

    public void replaceLinkListener(LinkListener llnew) {
	pane.replaceLinkListener(llnew);
    }

    public void setData(Properties p) {
	// need to setData on all cached instances!
	for (TemplateDocument doc: docs)
	    doc.setData(p);
	// and pane itself
	pane.setData(p);
    }

    public void setDocument(TemplateDocument doc) {
	pane.setDocument(doc);
    }
    
    public void setDocument(TemplateDocument doc, NamespaceHandler nsh) {
	pane.setDocument(doc, nsh);
    }

    public void setDocument(Document doc, String url) {
	pane.setDocument(doc, url);
    }

    public void setDocument(Document doc) {
	pane.setDocument(doc);
    }

    public void setDocument(File file) throws Exception {
	pane.setDocument(file);
    }

    public void setDocument(InputStream stream, String url, NamespaceHandler nsh) {
	pane.setDocument(stream, url, nsh);
    }

    public void setDocument(InputStream stream, String url) throws Exception {
	pane.setDocument(stream, url);
    }

    public void setDocument(String url, NamespaceHandler nsh) {
	pane.setDocument(url, nsh);
    }

    public void setDocument(String uri) {
	pane.setDocument(uri);
    }

    public void setDocumentFromString(String content, String url,
	    NamespaceHandler nsh) {
	pane.setDocumentFromString(content, url, nsh);
    }

    public void setFormSubmissionListener(FormSubmissionListener fsl) {
	pane.setFormSubmissionListener(fsl);
    }

    public void setSharedContext(SharedContext ctx) {
	pane.setSharedContext(ctx);
    }

    public void setSubmitAction(Action e) {
	pane.setSubmitAction(e);
    }

    public void submit(String query) {
	pane.submit(query);
    }
    
    public Component getFormComponent(String name) {
	return pane.getFormComponent(name);
    }

    public Map<String, Component> getFormComponents() {
	return pane.getFormComponents();
    }
}
