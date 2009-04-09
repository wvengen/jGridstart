package nl.nikhef.jgridstart.gui;

import java.util.logging.Logger;
import java.util.logging.LogManager;

import nl.nikhef.jgridstart.logging.LogWindowHandler;

public class Main {

    // setup logging
    static {
	try {
	    LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
	} catch(Exception e) {
	    System.out.println("Warning: logging configuration could not be set");
	}
    }
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart");

    /** graphical user-interface entry point */
    public static void main(String[] args) {
	logger.addHandler(LogWindowHandler.getInstance());
	logger.fine("main starting");
	// Schedule a job for the event-dispatching thread:
	// creating and showing this application's GUI.
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		createAndShowGUI();
	    }
	});
    }

    private static void createAndShowGUI() {
	/*
	try {
	    logger.fine("set look and feel");
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch(Exception e) { }
	*/
	JGSFrame frame = new JGSFrame();
	frame.setDefaultCloseOperation(JGSFrame.EXIT_ON_CLOSE);

	frame.pack();
	frame.setVisible(true);
    }

}
