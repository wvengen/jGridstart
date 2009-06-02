package nl.nikhef.jgridstart.gui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import nl.nikhef.xhtmlrenderer.swing.TemplatePanel;

import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.SelectionHighlighter;


/** A template pane in a scrolledwindow with buttons below. */
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
	
	// add button pane
	buttonpane = new JPanel();
	buttonpane.setLayout(new BoxLayout(buttonpane, BoxLayout.X_AXIS));
	buttonpane.setBorder(BorderFactory.createEmptyBorder(
		btnBorderWidth, btnBorderWidth, btnBorderWidth, btnBorderWidth));
	add(buttonpane, null);
    }
    
    public TemplateButtonPanel(String src) throws IOException {
	this();
	setDocument(src);
    }
    
    /** remove all buttons currently present */
    public void removeActions() {
	buttonpane.removeAll();
    }
    /** adds an action to the button list
     *
     * @param action Action to add
     * @param isDefault true whether this is the default action (set on only one)
     */
    public void addAction(Action action, boolean isDefault) {
	if (buttonpane.getComponentCount()==0)
	    buttonpane.add(Box.createHorizontalGlue());
	
	buttonpane.add(new JButton(action), null);
	buttonpane.add(Box.createRigidArea(new Dimension(btnBorderWidth, 0)));
	buttonpane.revalidate();
	buttonpane.repaint();
	if (isDefault)
	    contentpane.setSubmitAction(action);
    }
    /** adds an non-default action to the button list
     * 
     * @param action Action to add
     */
    public void addAction(Action action) {
	addAction(action, false);
    }
    
    // plain delegates TODO complete
    public String getDocumentTitle() {
	return contentpane.getDocumentTitle();
    }
    public void refresh() {
	contentpane.refresh();
    }
    public void setData(Properties p) {
	contentpane.setData(p);
    }
    public Properties data() {
	return contentpane.data();
    }
    public void setDocument(String url) {
	contentpane.setDocument(url);
    }
    public void setDocument(File file) throws Exception {
	contentpane.setDocument(file);
    }
    public void setDocument(Document doc, String url) {
	contentpane.setDocument(doc, url);
    }
    public void setDocument(Document doc, String url, NamespaceHandler nsh) {
	contentpane.setDocument(doc, url, nsh);
    }
    public Document getDocument() {
	return contentpane.getDocument();
    }
    public void setBackground(Color c) {
	super.setBackground(c);
	if (contentpane!=null) contentpane.setBackground(c);
    }
    public boolean print() throws PrinterException {
	return contentpane.print();
    }
}