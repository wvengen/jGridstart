package nl.nikhef.jgridstart.gui.util;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javax.swing.UIManager;
import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.NamespaceHandler;

/**
 * HTML-based wizard dialog. One supplies it with a list of html templates and a
 * property list.
 * 
 */
public class TemplateWizard extends JDialog {

    /** wizard page templates for each step */
    public ArrayList<URL> pages = new ArrayList<URL>();
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

    /** set the currently displayed page */
    // TODO describe behaviour, especially what happens in last step
    //      (based on handler being present or not)
    public void setStep(int s) {
	// handle final "Close" step which just quits the dialog
	if (s == pages.size()) {
	    if (handler!=null)
		handler.pageChanged(this, s);
	    else
		dispose();
	    return;
	}
	// set new contents and get title from that as well
	step = s;
	pane.setDocument(pages.get(step).toString());
	setTitle(pane.getDocumentTitle());
	// no "Previous" at start; no "Next" beyond the final "Close"
	if (s == pages.size()-1)
	    cancelAction.putValue(Action.NAME, "Close");
	nextAction.setEnabled(step < (pages.size()-1) );
	prevAction.setEnabled(step > 0);
	if (handler!=null) handler.pageChanged(this, s);
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

    /** shows or hides the dialog */
    public void setVisible(boolean visible) {
	// need to setup some stuff if no step selected
	if (visible && step < 0)
	    setStep(0);
	super.setVisible(visible);
    }
    
    /** Initialize and build the dialog
     * 
     * The background colour is set to the default control background colour as
     * specified by UIManager. It is still possible to override it in CSS/HTML.
     */
    protected void initialize() {
	setModal(true);
	getContentPane().removeAll();
	pane = new TemplateButtonPanel();
	getContentPane().add(pane);
	pane.setBackground(UIManager.getColor("control"));
	// "Previous" button
	prevAction = new AbstractAction("Previous") {
	    public void actionPerformed(ActionEvent e) {
		setStepRelative(-1);
	    }
	};
	prevAction.putValue(Action.MNEMONIC_KEY, new Integer('P'));
	
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
	
	pane.addButton(btnRight, new JButton(prevAction), false, false);
	// "Next" button; name and mnemonic set in setStep()
	nextAction = new AbstractAction("Next") {
	    public void actionPerformed(ActionEvent e) {
		setStepRelative(1);
	    }
	};
	nextAction.putValue(Action.MNEMONIC_KEY, new Integer('N'));
	pane.addButton(btnRight, new JButton(nextAction), true, false);
	// close window on escape
	btnRight.add(Box.createRigidArea(new Dimension(pane.btnBorderWidth*8, 0)));
	cancelAction = new AbstractAction("Cancel") {
	    public void actionPerformed(ActionEvent e) {
		TemplateWizard.this.dispose();
	    }
	};
	cancelAction.putValue(Action.MNEMONIC_KEY, new Integer('C'));
	pane.addButton(btnRight, new JButton(cancelAction), true);
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
	/** called when page was changed. This is called after the page has
	 * been shown and can be used to modify the dialog contents.
	 * 
	 * @param w Current TemplateWizard
	 * @param page Page number currently shown (index in pages)
	 */
	void pageChanged(TemplateWizard w, int page);
    }
    
    // delegates to TemplatePanel
    public String getDocumentTitle() {
	return pane.getDocumentTitle();
    }
    public void refresh() {
	pane.refresh();
    }
    public void setData(Properties p) {
	pane.setData(p);
    }
    public Properties data() {
	return pane.data();
    }
    public void setDocument(String url) {
	pane.setDocument(url);
    }
    public void setDocument(File file) throws Exception {
	pane.setDocument(file);
    }
    public void setDocument(Document doc, String url) {
	pane.setDocument(doc, url);
    }
    public void setDocument(Document doc, String url, NamespaceHandler nsh) {
	pane.setDocument(doc, url, nsh);
    }
    public Document getDocument() {
	return pane.getDocument();
    }
    public void setBackground(Color c) {
	super.setBackground(c);
	if (pane!=null) pane.setBackground(c);
    }
    public boolean print() throws PrinterException {
	return pane.print();
    }
}
