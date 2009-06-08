package nl.nikhef.jgridstart.gui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import nl.nikhef.xhtmlrenderer.swing.TemplatePanel;

import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.SelectionHighlighter;


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
 * TODO Also selecting and copying the text is possible.
 */
public class TemplateButtonPanel extends JPanel {
    
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
	
	// allow selecting and copying using Ctrl-C
	/*
	SelectionHighlighter highlighter = new SelectionHighlighter();
	highlighter.install(contentpane);
	SelectionHighlighter.CopyAction copyAction = new SelectionHighlighter.CopyAction();
	copyAction.install(highlighter);
	copyAction.putValue(Action.ACCELERATOR_KEY,
		KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
	getActionMap().put(copyAction.getValue(Action.NAME), copyAction);
	getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
		(KeyStroke)copyAction.getValue(Action.ACCELERATOR_KEY),
		copyAction.getValue(Action.NAME));
	*/
	
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
    
    // plain delegates TODO complete
    /** @see TemplatePanel#getDocumentTitle */
    public String getDocumentTitle() {
	return contentpane.getDocumentTitle();
    }
    /** @see TemplatePanel#refresh */
    public void refresh() {
	contentpane.refresh();
    }
    /** @see TemplatePanel#setData */
    public void setData(Properties p) {
	contentpane.setData(p);
    }
    /** @see TemplatePanel#data */
    public Properties data() {
	return contentpane.data();
    }
    /** @see TemplatePanel#setSubmitAction */
    public void setSubmitAction(Action e) {
	contentpane.setSubmitAction(e);
    }
    /** @see TemplatePanel#setDocument(String) */
    public void setDocument(String url) {
	contentpane.setDocument(url);
    }
    /** @see TemplatePanel#setDocument(File) */
    public void setDocument(File file) throws Exception {
	contentpane.setDocument(file);
    }
    /** @see TemplatePanel#setDocument(Document, String) */
    public void setDocument(Document doc, String url) {
	contentpane.setDocument(doc, url);
    }
    /** @see TemplatePanel#setDocument(Document, String, NamespaceHandler) */
    public void setDocument(Document doc, String url, NamespaceHandler nsh) {
	contentpane.setDocument(doc, url, nsh);
    }
    /** @see TemplatePanel#getDocument */
    public Document getDocument() {
	return contentpane.getDocument();
    }
    /** Sets the background colour, also of the HTML area */
    public void setBackground(Color c) {
	super.setBackground(c);
	if (contentpane!=null) contentpane.setBackground(c);
    }
    /** @see TemplatePanel#print */
    public boolean print() throws PrinterException {
	return contentpane.print();
    }
}