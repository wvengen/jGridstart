package nl.nikhef.jgridstart.gui.util;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.UIManager;
import org.w3c.dom.Document;

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
    protected TemplateButtonPane pane = null;
    /** "Next" action */
    protected Action nextAction = null;
    /** "Previous" action */
    protected Action prevAction = null;
    /** Listener for page changes */
    protected PageListener handler = null;

    // constructors
    public TemplateWizard()               { super(); initialize(); }
    public TemplateWizard(Dialog owner)   { super(owner); initialize(); }
    public TemplateWizard(Frame owner)    { super(owner); initialize(); }
    public TemplateWizard(Properties p)   { this(); setData(p); }
    public TemplateWizard(Dialog owner, Properties p) { this(owner); setData(p); }
    public TemplateWizard(Frame owner, Properties p)  { this(owner); setData(p);  }

    /** set the template's properties */
    public void setData(Properties p) {
	pane.setData(p);
    }
    /** return the template's properties */
    public Properties data() {
	return pane.data();
    }
    /** refresh the currently shown document */
    public void refresh() {
	pane.refresh();
    }

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
	try {
	    pane.setPage(pages.get(step));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	setTitle(pane.getTitle());
	// no "Previous" at start; no "Next" beyond the final "Close"
	if (s == pages.size()-1) {
	    nextAction.putValue(Action.NAME, "Close");
	    nextAction.putValue(Action.MNEMONIC_KEY, new Integer('C'));
	} else {
	    nextAction.putValue(Action.NAME, "Next");
	    nextAction.putValue(Action.MNEMONIC_KEY, new Integer('N'));
	}
	nextAction.setEnabled(step < pages.size());
	prevAction.setEnabled(step > 0);
	if (handler!=null) handler.pageChanged(this, s);
	// pack here to give child classes a chance to setPreferredSize()
	// in their constructors or in setStep().
	pack();
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
	pane = new TemplateButtonPane();
	getContentPane().add(pane);
	pane.setBackground(UIManager.getColor("control"));
	// "Previous" button
	prevAction = new AbstractAction("Previous") {
	    public void actionPerformed(ActionEvent e) {
		setStepRelative(-1);
	    }
	};
	prevAction.putValue(Action.MNEMONIC_KEY, new Integer('P'));
	pane.addAction(prevAction);
	// "Next" button; name and mnemonic set in setStep()
	nextAction = new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
		setStepRelative(1);
	    }
	};
	pane.addAction(nextAction, true);
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
    
    // delegates to TemplatePane
    public Document getHTMLDocument() { return pane.getDocument(); }
    public URL getPage() { return pane.getPage(); }
    public boolean print() throws PrinterException  { return pane.print(); }
}
