package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;

public class ActionViewCertificateList extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    protected JFrame parent = null;
    private JComponent c = null;
    
    /** Enabled/disabled state */
    private boolean isSelected = false;

    /** Create a new ActionViewCertificateList.
     * <p>
     * This is an enable/disable action. Java 1.6 has support for this using
     * {@link javax.swing.ButtonGroup#setSelected}, but we want to support
     * Java 1.5 as well, so have to code this ourselves. Because of this, you
     * need to have the initial status of components that use this element
     * equal to the {@code isSelected} parameter.
     * 
     * @param parent Frame component is in
     * @param c Component to view/hide
     * @param isSelected Default value of this action
     */
    public ActionViewCertificateList(JFrame parent, JComponent c, boolean isSelected) {
	super();
	this.c = c;
	this.parent = parent;
	this.isSelected = isSelected;
	putValue(NAME, "Show certificate list");
	putValue(MNEMONIC_KEY, new Integer('L'));
	URLLauncherCertificate.addAction("viewlist", this);
	c.setVisible(isSelected);
    }
    public ActionViewCertificateList(JFrame parent, JComponent c) {
	this(parent, c, false);
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	boolean localIsSelected = !isSelected;
	// parse argument, if any
	if (e.getActionCommand()!=null) {
	    String[] args = e.getActionCommand().split(",\\s*");
	    for (int i=0; i<args.length; i++) {
		if (args[i].equals("true"))
		    localIsSelected = true;
		if (args[i].equals("false"))
		    localIsSelected = false;
	    }
	}
	isSelected = localIsSelected;
	c.setVisible(isSelected);
	// need to relayout
	parent.validate();
    }
}
