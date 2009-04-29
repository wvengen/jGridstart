package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class ActionQuit extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    @SuppressWarnings("unused")
    private JFrame parent = null;

    public ActionQuit(JFrame parent) {
	super();
	this.parent = parent;
	putValue(NAME, "Quit");
	putValue(MNEMONIC_KEY, new Integer('Q'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
	BareBonesActionLaunch.addAction("quit", this);
    }

    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	System.exit(0);
    }
}
