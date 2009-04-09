package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.logging.LogWindowHandler;

public class ActionViewLog extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.view");
    @SuppressWarnings("unused")
    private JFrame parent = null;

    public ActionViewLog(JFrame parent) {
	super();
	this.parent = parent;
	putValue(NAME, "View log...");
	putValue(MNEMONIC_KEY, new Integer('L'));
    }

    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	LogWindowHandler.getInstance().getWindow().setVisible(true);
    }
}
