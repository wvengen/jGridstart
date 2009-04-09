package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class ActionQuit extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.view");
    @SuppressWarnings("unused")
    private JFrame parent = null;

    public ActionQuit(JFrame parent) {
	super();
	this.parent = parent;
	putValue(NAME, "Quit");
	putValue(MNEMONIC_KEY, new Integer('Q'));
    }

    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	System.exit(0);
    }
}
