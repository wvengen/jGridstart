package nl.nikhef.jgridstart.gui.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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
public class TemplateWizard extends JDialog implements ITemplateWizard {

    /** list of loaded documents */
    protected ArrayList<ITemplateWizardPage> pages = new ArrayList<ITemplateWizardPage>();
    
    /** currently active step (base 0), -1 for not initialized */
    protected int page = -1;
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

    // constructors
    public TemplateWizard()               { super(); initialize(); }
    public TemplateWizard(Dialog owner)   { super(owner); initialize(); }
    public TemplateWizard(Frame owner)    { super(owner); initialize(); }
    public TemplateWizard(Properties p)   { this(); setData(p); }
    public TemplateWizard(Dialog owner, Properties p) { this(owner); setData(p); }
    public TemplateWizard(Frame owner, Properties p)  { this(owner); setData(p);  }
    
    /** Change wizard page
     * <p>
     * This fails if the exitpage or enterpage handlers veto this
     * (by returning {@literal false}), or if the page is not
     * present. In these cases, {@literal false} is returned.
     * <p>
     * If the argument is {@literal null}, the first page is shown.
     *
     * @param id identifier of step to show, or {@literal null} for first page
     * @return whether the step was shown or no
     */
    public boolean setPage(String id) {
	for (int i=0; i<pages.size(); i++) {
	    if (pages.get(i).getId().equals(id))
		return setPage(i);
	}
	return false;
    }
    
    /** Sets the page to the first that is not done.
     * <p>
     * Each page is queried, starting from the first one, and if
     * {@link TemplateWizardPage#isDone isDone} returns {@literal false},
     * that page is shown.
     * <p>
     * If all pages are done, the last one is displayed.
     */
    public boolean setPageDetect() {
	for (int i=0; i<pages.size(); i++) {
	    if (!pages.get(i).isDone())
		return setPage(i);
	}
	return setPage(pages.size()-1);
    }

    /** Change wizard page by number
     * <p>
     * This fails if the exitpage or enterpage handlers veto this
     * (by returning {@literal false}), or if the page is not
     * present. In these cases, {@literal false} is returned.
     * <p>
     * When newPage is negative, the index is relative to the end.
     *
     * @param newPage page number to show
     * @return whether the step was shown or no
     */
    protected boolean setPage(int newPage) {
	// set wait cursor during page change
	Cursor oldCursor = getCursor();
	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	try {
	    // negative is relative from end
	    if (newPage<0) newPage = pages.size() + newPage;
	    
	    // validate page number
	    if (newPage<0 || newPage>=pages.size())
		return false;

	    // call exit handler, if any
	    if (page>=0 &&
		    !pages.get(page).pageLeave(pages.get(newPage), newPage>page))
		return false;
	    
	    // clear error if going to another page
	    if (page != newPage) data().remove("wizard.error");

	    ITemplateWizardPage oldPage = null;
	    if (page>=0) oldPage = pages.get(page);
	    page = newPage;
	    // Update the ui
	    updateWizardProperties(newPage);
	    // no "Previous" at start; no "Next" beyond the final "Close"
	    if (newPage == pages.size()-1)
		setSystemAffected(true);
	    setButtonEnabled(BUTTON_NEXT, page < (pages.size()-1) );
	    setButtonEnabled(BUTTON_PREV, page > 0);
	    // call enter handler
	    pages.get(newPage).pageEnter(oldPage);

	    // set new contents and get title from that as well
	    pages.get(newPage).setData(data());
	    pane.setDocument(pages.get(newPage));
	    pane.refresh();
	    
	    if (data().contains("wizard.title"))
		setTitle(data().getProperty("wizard.title") + " - " + pages.get(newPage).getTitle());
	    else
		setTitle(pages.get(newPage).getTitle());
	    
	    setName("jgridstart-wizard-"+pages.get(newPage).getId());

	    // pack here to give child classes a chance to setPreferredSize()
	    // in their constructors or in setStep(). This is only called if
	    // the window is not yet visible because pack()ing resets the
	    // dialog size to its preferred size, which is unwanted when the
	    // user resized the window.
	    if (!isVisible()) pack();

	    // success!
	    return true;
	    
	} finally {
	    // always restore old cursor
	    setCursor(oldCursor);
	}
    }
    /** go to another page by relative distance */
    public boolean setPageRelative(int delta) {
	if (page < 0)
	    page = 0;
	return setPage(page + delta);
    }
    
    /** Update the wizard properties for a step.
     * <p>
     * This generates the properties {@code wizard.*} which can be
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
    protected void updateWizardProperties(int step) {
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
     * <p>
     * For each page, the method {@link TemplateWizardPage#isDone isDone} is
     * called. If it returns {@literal true}, the class {@code wizard-done}
     * is added. The stylesheet can add a checkmark, for example, for
     * this class to show the user that that step doesn't need to be done
     * (anymore).
     * 
     * @param step step to return contents for; index in {@link #pages}
     * @param current the currently active step
     */
    protected String getWizardContentsLine(int step, int current) {
	String classes = "";
	if (step==current) classes += " wizard-current";
	if (step>current)  classes += " wizard-future";
	if (pages.get(step).isDone()) classes += " wizard-done";
	return (classes=="" ? "<li>" : "<li class='"+classes+"'>") + pages.get(step).getContentsTitle() + "</li>\n";
    }

    /** Shows or hides the dialog.
     * <p>
     * This also sets the current step to the first one if none is selected
     * as of yet (only when there are pages defined).
     */
    @Override
    public void setVisible(boolean visible) {
	// need to setup some stuff if no step selected
	if (visible && page < 0 && pages.size() > 0)
	    setPage(0);
	super.setVisible(visible);
    }
    
    /** Button: next @see #setButtonEnabled */
    static public final int BUTTON_PREV = 1;
    static public final int BUTTON_NEXT = 2;
    static public final int BUTTON_CLOSE = 3;
    
    /** Make button enabled or disabled
     * 
     * @param button one of {@linkplain #BUTTON_PREV}, {@linkplain #BUTTON_NEXT}
     *          or {@linkplain #BUTTON_CLOSE}.
     * @param enabled whether to enable the button or no
     */
    public void setButtonEnabled(int button, boolean enabled) {
	if (button==BUTTON_PREV)
	    prevAction.setEnabled(enabled);
	else if (button==BUTTON_NEXT)
	    nextAction.setEnabled(enabled);
	else if (button==BUTTON_CLOSE)
	    cancelAction.setEnabled(enabled);
	else
	    assert(false);
    }
    
    public boolean getButtonEnabled(int button) {
	if (button==BUTTON_PREV)
	    return prevAction.isEnabled();
	if (button==BUTTON_NEXT)
	    return nextAction.isEnabled();
	if (button==BUTTON_CLOSE)
	    return cancelAction.isEnabled();
	else
	    assert(false);
	return false;
    }
    
    /** Indicate whether closing the wizard leaves system changed or not.
     * <p>
     * This affects the close/cancel button. If closing the wizard leaves
     * the user unaffected, the button should be named "Cancel". If the
     * wizard has changed anything on the system, it should be named "Close".
     * <p>
     * For example, when closing the wizard reverts back all changes that
     * were done, it should definitely be called "Cancel". 
     */
    public void setSystemAffected(boolean affected) {
	if (affected)
	    cancelAction.putValue(Action.NAME, "Close");
	else
	    cancelAction.putValue(Action.NAME, "Cancel");
    }
    
    public ITemplateWizard getWizard() {
	return this;
    }
    
    public Component getWindow() {
	return this;
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
		setPageRelative(-1);
	    }
	};
	prevAction.putValue(Action.MNEMONIC_KEY, new Integer('P'));
	
	JButton prevButton = new JButton(prevAction);
	prevButton.setName("wizard_previous");
	pane.addButton(btnRight, prevButton, false, false);
	// "Next" button; name and mnemonic set in setStep()
	nextAction = new AbstractAction("Next") {
	    public void actionPerformed(ActionEvent e) {
		setPageRelative(1);
	    }
	};
	nextAction.putValue(Action.MNEMONIC_KEY, new Integer('N'));
	JButton nextButton = new JButton(nextAction);
	nextButton.setName("wizard_next");
	pane.addButton(btnRight, nextButton, true, false);
	// close window on escape
	btnRight.add(Box.createRigidArea(new Dimension(pane.BUTTON_BORDER*8, 0)));
	cancelAction = new AbstractAction("Cancel") {
	    public void actionPerformed(ActionEvent e) {
		TemplateWizard.this.dispose();
	    }
	};
	cancelAction.putValue(Action.MNEMONIC_KEY, new Integer('C'));
	setSystemAffected(false);
	JButton cancelButton = new JButton(cancelAction);
	cancelButton.setName("wizard_close");
	pane.addButton(btnRight, cancelButton, true);
	getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
	getRootPane().getActionMap().put("ESCAPE", cancelAction);
    }
    
    /** Add a page to the wizard. */
    public void addPage(ITemplateWizardPage page) {
	page.associate(this, this);
	pages.add(page);
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
     * Actually invokes {@link #setPage} so that the
     * hooks are properly called.
     */
    public boolean refresh() {
	return setPage(page);
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
	for (Iterator<ITemplateWizardPage> it = pages.iterator(); it.hasNext(); )
	    it.next().setData(p);
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
