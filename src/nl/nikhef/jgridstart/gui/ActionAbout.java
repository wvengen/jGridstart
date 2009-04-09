package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class ActionAbout extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.view");
    private JFrame parent = null;

    public ActionAbout(JFrame parent) {
	super();
	this.parent = parent;
	putValue(NAME, "About...");
	putValue(MNEMONIC_KEY, new Integer('A'));
    }

    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	JOptionPane.showMessageDialog(parent,
		"jGridstart gives you a hassle-free start with the grid.\n" +
		"At least I hope so!\n" +
		"\n" +
		"Java runtime environment version: "+System.getProperty("java.version"),
		
		"About jGridStart", JOptionPane.INFORMATION_MESSAGE);
    }

}
