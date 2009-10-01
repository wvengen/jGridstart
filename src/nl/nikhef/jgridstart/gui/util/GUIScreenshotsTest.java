package nl.nikhef.jgridstart.gui.util;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;

import nl.nikhef.jgridstart.util.FileUtils;

import abbot.finder.BasicFinder;
import abbot.finder.Matcher;
import abbot.tester.ComponentTester;
import abbot.util.AWT;

/** Generate screenshots the for documentation of jGridstart */
public class GUIScreenshotsTest {
    
    protected static ComponentTester tester = new ComponentTester();
    
    /** password used for test certificate */
    protected static String password = "test123_pass#";
    
    public static void main(String[] args) throws Exception {
	// screenshot output directory
	if (args.length!=1) {
	    System.err.println("please give screenshot dir as argument");
	    System.exit(1);
	}
	File shotdir = new File(args[0]);
	shotdir.mkdirs();
	// setup temporary environment
	File tmphome = FileUtils.createTempDir("jgridstart-home");
	try {
	    System.setProperty("jgridstart.ca.provider", "LocalCA");
	    System.setProperty("jgridstart.ca.local.hold", "true");
	    System.setProperty("user.home", tmphome.getCanonicalPath());
	    // create standard gui
	    nl.nikhef.jgridstart.gui.Main.main(new String[]{});
	    // move mouse here since closing window may give up focus later
	    Thread.sleep(1000); guiSleep();
	    tester.mouseMove(AWT.getActiveWindow().getComponents()[0]);

	    /*
	     * Request new
	     */
	    // start screen
	    saveScreenshot(new File(shotdir, "newrequest01.png"));
	    // new request wizard
	    guiSleep(); tester.key(new Integer('N'), InputEvent.CTRL_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest02.png"));
	    // enter details
	    guiSleep();
	    tester.keyString("John\t");
	    tester.keyString("Doe\t");
	    tester.keyString("john.doe@example.com\t");
	    tester.keyString("N\t");
	    tester.keyString(" \t\t");
	    tester.keyString(password+"\t");
	    tester.keyString(password+"\t");
	    // wait for submission
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest03.png"));
	    Thread.sleep(6000);
	    // verification form
	    System.setProperty("wizard.show.help1", "true"); // simulate help btn1 pressed
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    guiSleep();
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest04.png"));
	    // form display
	    JButton btn = (JButton) new BasicFinder().find(new Matcher() {
		public boolean matches(Component c) {
		    return c instanceof JButton && ((JButton)c).getText().equals("display form");
		}
	    });
	    btn.doClick();
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest05.png"));
	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
	    // close wizard
	    guiSleep();
	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest06.png"));
	    // enable certificate in LocalCA and refresh pane
	    System.setProperty("jgridstart.ca.local.hold", "false");
	    tester.key(KeyEvent.VK_F5);
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest07.png"));
	    // show request wizard again
	    tester.key(new Integer('A'), InputEvent.ALT_MASK);
	    tester.key('R');
	    Thread.sleep(2500);
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest08.png"));
	    // install step
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest09.png"));
	    // show final screen
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, "newrequest10.png"));
	    // exit wizard
	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
	} finally {
	    FileUtils.recursiveDelete(tmphome);
	}
	// exit!
	System.exit(0);
    }
    
    /** Write screenshot of current screen to specified file as png.
     * <p>
     * Assumes a single screen. */
    protected static void saveScreenshot(final File dst) throws AWTException, IOException {
	final Robot robot = new Robot();
	guiSleep(); guiSleep(); guiSleep();
	// capture screen
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		try {
		    // find active window area, or full desktop if that fails
		    Window w = AWT.getActiveWindow();
		    Rectangle captureSize = w != null ? w.getBounds() :
			new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		    // take image
		    BufferedImage img = robot.createScreenCapture(captureSize);
		    ImageIO.write(img, "png", dst);
		} catch(IOException e) {
		    System.err.println(e);
		    System.exit(2);
		}
	    }
	});
    }
    
    protected static void guiSleep() throws AWTException {
	// process gui events
	try {
	    java.awt.EventQueue.invokeAndWait(new Runnable() {
	        public void run() { }
	    });
	} catch (Exception e) { throw new AWTException(e.toString()); }
    }
}
