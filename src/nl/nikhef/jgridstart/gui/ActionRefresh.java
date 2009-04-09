package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import nl.nikhef.jgridstart.CertificateStore;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class ActionRefresh extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.view");
    protected JFrame parent = null;
    protected CertificateStore store = null;

    public ActionRefresh(JFrame parent, CertificateStore store) {
	super();
	this.parent = parent;
	this.store = store;
	putValue(NAME, "Refresh");
	putValue(MNEMONIC_KEY, new Integer('R'));
    }

    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	store.refresh();
    }

}
