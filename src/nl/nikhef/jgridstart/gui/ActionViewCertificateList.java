package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;

public class ActionViewCertificateList extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    private JComponent c = null;

    /** Create a new ActionViewCertificateList
     * 
     * @param c Component to view/hide
     * @param isSelected Default value of this action
     */
    public ActionViewCertificateList(JComponent c, boolean isSelected) {
	super();
	this.c = c;
	putValue(NAME, "Show certificate list");
	putValue(MNEMONIC_KEY, new Integer('L'));
	putValue("SwingSelectedKey", new Boolean(isSelected));
	BareBonesActionLaunch.addAction("viewlist", this);
	actionPerformed(null);
    }
    public ActionViewCertificateList(JComponent c) {
	this(c, false);
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	c.setVisible((Boolean)getValue("SwingSelectedKey"));
    }
}