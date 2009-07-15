package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.jdesktop.swingworker.SwingWorker;

import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.URLLauncher;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class ActionRefresh extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    protected JFrame parent = null;
    protected CertificateStore store = null;

    public ActionRefresh(JFrame parent, CertificateStore store) {
	super();
	this.parent = parent;
	this.store = store;
	putValue(NAME, "Refresh");
	putValue(MNEMONIC_KEY, new Integer('R'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F5"));
	URLLauncher.addAction("refresh", this);
    }

    @SuppressWarnings("deprecation") // for JFrame.setCursor()
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	setEnabled(false);
	parent.setCursor(Cursor.WAIT_CURSOR);
	new SwingWorker<Void, Void>() {
	    @Override
	    protected Void doInBackground() throws Exception {
		store.refresh();
		return null;
	    }
	    @Override
	    protected void done() {
		parent.setCursor(null);
		setEnabled(true);
	    }
	}.execute();
    }
}
