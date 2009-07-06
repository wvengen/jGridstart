package nl.nikhef.jgridstart.gui.util;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;
import nl.nikhef.xhtmlrenderer.swing.TemplateDocument;
import nl.nikhef.xhtmlrenderer.swing.TemplatePanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;


/** A {@link TemplatePanel} in a scrolled window with buttons below.
 * <p>
 * This is a thin layer over {@linkplain TemplatePanel} that adds optional buttons
 * below the document view. It may be viewed as a kind of {@linkplain JOptionPane}
 * with a {@linkplain TemplatePanel} as dialog contents, but the buttons here are
 * to be supplied explicitely. {@link TemplateWizard} is an example that adds
 * previous/next buttons.
 * <p>
 * Additionally, links are opened in an external web browser using
 * {@link BareBonesActionLaunch}.
 */
public class TemplateButtonPanel extends JPanel implements ITemplatePanel {
    
    /** the actual html template pane */
    protected TemplatePanel contentpane = null;
    /** pane containing the buttons */
    protected JPanel buttonpane = null;
    /** empty space around buttons */
    protected final int btnBorderWidth = 2;
    
    public TemplateButtonPanel() {
	super();
	
	// add TemplatePanel in scrollview
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	contentpane = new TemplatePanel();
	JScrollPane scroll = new JScrollPane();
    	scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    	scroll.setViewportView(contentpane);
	add(scroll, null);
	
	// open links with an external web browser
	contentpane.replaceLinkListener(new LinkListener() {
	    @Override
            public void linkClicked(BasicPanel panel, String uri) {
		BareBonesActionLaunch.openURL(uri, panel);
	    }
	});
	
	// add button pane, by default with right aligned components
	buttonpane = new JPanel();
	buttonpane.setLayout(new BoxLayout(buttonpane, BoxLayout.X_AXIS));
	buttonpane.setBorder(BorderFactory.createEmptyBorder(
		btnBorderWidth, btnBorderWidth, btnBorderWidth, btnBorderWidth));
	buttonpane.add(Box.createHorizontalGlue());
	add(buttonpane, null);
    }
    
    public TemplateButtonPanel(String src) throws IOException {
	this();
	setDocument(src);
    }

    /** returns the bottom {@linkplain JPanel} that can contain the buttons.
     * <p>
     * By default, horizontal glue is added as the first element, so that
     * when you use {@link JPanel#add}, elements are right-aligned. If you
     * really want to have this differently, call {@link JPanel#removeAll}
     * on the returned {@linkplain JPanel} after construction of this
     * {@linkplain TemplateButtonPanel}. */
    public JPanel getButtonPane() {
	return buttonpane;
    }
    
    /** adds an action to the button list
     *
     * @param panel Panel to add the button to; useful when creating sub-panels
     * @param btn Button to add
     * @param isDefault true whether this is the default action (set on only one)
     * @param isLast whether this is the final button to add, for refreshing
     */
    public void addButton(JPanel panel, JButton btn, boolean isDefault, boolean isLast) {
	panel.add(btn, null);
	panel.add(Box.createRigidArea(new Dimension(btnBorderWidth, 0)));
	if (isDefault && btn.getAction()!=null)
	    contentpane.setSubmitAction(btn.getAction());
	if (isLast) {
	    panel.revalidate();
	    panel.repaint();
	}
    }
    /** adds an action to the button list
    *
    * @param panel Panel to add the button to; useful when creating sub-panels
    * @param btn Button to add
    * @param isDefault true whether this is the default action (set on only one)
    */
    public void addButton(JPanel panel, JButton btn, boolean isDefault) {
	addButton(panel, btn, isDefault, true);
    }
    /** adds an action to the button list
     * <p>
     * The button needs to have an Action specified at this moment.
     *
     * @param btn Button to add
     * @param isDefault true whether this is the default action (set on only one)
     * @param isLast whether this is the final button to add, for refreshing
     */
    public void addButton(JButton btn, boolean isDefault, boolean isLast) {
	addButton(buttonpane, btn, isDefault, isLast);
    }
    /** adds an action to the button list
     * <p>
     * The button needs to have an Action specified at this moment.
     *
     * @param btn Button to add
     * @param isDefault true whether this is the default action (set on only one)
    */
    public void addButton(JButton btn, boolean isDefault) {
	addButton(buttonpane, btn, isDefault);
    }

    /*
     * 
     * Delegates
     * 
     */
    public void addMouseTrackingListener(FSMouseListener l) {
	contentpane.addMouseTrackingListener(l);
    }

    public void componentHidden(ComponentEvent e) {
	contentpane.componentHidden(e);
    }

    public void componentMoved(ComponentEvent e) {
	contentpane.componentMoved(e);
    }

    public void componentResized(ComponentEvent e) {
	contentpane.componentResized(e);
    }

    public void componentShown(ComponentEvent e) {
	contentpane.componentShown(e);
    }

    public Properties data() {
	return contentpane.data();
    }

    public Document getDocument() {
	return contentpane.getDocument();
    }

    public String getDocumentTitle() {
	return contentpane.getDocumentTitle();
    }

    public List<?> getMouseTrackingListeners() {
	return contentpane.getMouseTrackingListeners();
    }

    public SharedContext getSharedContext() {
	return contentpane.getSharedContext();
    }

    public URL getURL() {
	return contentpane.getURL();
    }

    public boolean isActive(Element e) {
	return contentpane.isActive(e);
    }

    public boolean isFocus(Element e) {
	return contentpane.isFocus(e);
    }

    public boolean isHover(Element e) {
	return contentpane.isHover(e);
    }

    public boolean print() throws PrinterException {
	return contentpane.print();
    }

    public boolean refresh() {
	return contentpane.refresh();
    }

    public void reloadDocument(Document doc) {
	contentpane.reloadDocument(doc);
    }

    public void reloadDocument(String URI) {
	contentpane.reloadDocument(URI);
    }

    public void removeMouseTrackingListener(FSMouseListener l) {
	contentpane.removeMouseTrackingListener(l);
    }

    public void replaceLinkListener(LinkListener llnew) {
	contentpane.replaceLinkListener(llnew);
    }

    public void setData(Properties p) {
	contentpane.setData(p);
    }
    
    public void setDocument(TemplateDocument doc) {
	contentpane.setDocument(doc);
    }
    
    public void setDocument(TemplateDocument doc, NamespaceHandler nsh) {
	contentpane.setDocument(doc, nsh);
    }

    public void setDocument(Document doc, String url) {
	contentpane.setDocument(doc, url);
    }

    public void setDocument(Document doc) {
	contentpane.setDocument(doc);
    }

    public void setDocument(File file) throws Exception {
	contentpane.setDocument(file);
    }

    public void setDocument(InputStream stream, String url, NamespaceHandler nsh) {
	contentpane.setDocument(stream, url, nsh);
    }

    public void setDocument(InputStream stream, String url) throws Exception {
	contentpane.setDocument(stream, url);
    }

    public void setDocument(String url, NamespaceHandler nsh) {
	contentpane.setDocument(url, nsh);
    }

    public void setDocument(String uri) {
	contentpane.setDocument(uri);
    }

    public void setDocumentFromString(String content, String url,
	    NamespaceHandler nsh) {
	contentpane.setDocumentFromString(content, url, nsh);
    }

    public void setFormSubmissionListener(FormSubmissionListener fsl) {
	contentpane.setFormSubmissionListener(fsl);
    }

    public void setSharedContext(SharedContext ctx) {
	contentpane.setSharedContext(ctx);
    }

    public void setSubmitAction(Action e) {
	contentpane.setSubmitAction(e);
    }

    public void submit(String query) {
	contentpane.submit(query);
    }   
}