package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
import nl.nikhef.jgridstart.logging.LogWindowHandler;

/** Show log window */
public class ActionViewLog extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    @SuppressWarnings("unused")
    private JFrame parent = null;

    public ActionViewLog(JFrame parent) {
	super();
	this.parent = parent;
	putValue(NAME, "View log...");
	putValue(MNEMONIC_KEY, new Integer('L'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control L"));
	URLLauncherCertificate.addAction("viewlog", this);
    }

    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	LogWindowHandler.getInstance().getWindow().setVisible(true);
    }
}
