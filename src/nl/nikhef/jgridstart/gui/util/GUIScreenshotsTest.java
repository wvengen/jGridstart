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
import nl.nikhef.jgridstart.util.PasswordCache;

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
	String prefix = "jgridstart-screenshot-";
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
	    saveScreenshot(new File(shotdir, prefix+"newrequest01.png"));
	    // new request wizard
	    guiSleep(); tester.key(new Integer('N'), InputEvent.CTRL_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest02.png"));
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
	    saveScreenshot(new File(shotdir, prefix+"newrequest03.png"));
	    Thread.sleep(6000);
	    // verification form
	    System.setProperty("wizard.show.help1", "true"); // simulate help btn1 pressed
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    guiSleep();
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest04.png"));
	    // form display
	    JButton btn = (JButton) new BasicFinder().find(new Matcher() {
		public boolean matches(Component c) {
		    return c instanceof JButton && ((JButton)c).getText().equals("display form");
		}
	    });
	    btn.doClick();
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest05.png"));
	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
	    // close wizard
	    guiSleep();
	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest06.png"));
	    // enable certificate in LocalCA and refresh pane
	    System.setProperty("jgridstart.ca.local.hold", "false");
	    tester.key(KeyEvent.VK_F5);
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest07.png"));
	    // show request wizard again
	    tester.key(new Integer('A'), InputEvent.ALT_MASK);
	    tester.key('R');
	    Thread.sleep(2500);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest08.png"));
	    // install step
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest09.png"));
	    // show final screen
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest10.png"));
	    // exit wizard
	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
	    // save final screenshot
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"newrequest11.png"));
	    guiSleep();
	    
	    /*
	     * Renewal
	     */
	    System.setProperty("jgridstart.ca.local.hold", "true");
	    // forget password so we certainly get the password dialog
	    PasswordCache.getInstance().clear();
	    // start screen
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew01.png"));
	    // personal details
	    tester.key(new Integer('A'), InputEvent.ALT_MASK);
	    tester.key('W');
	    Thread.sleep(2500);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew02.png"));
	    tester.keyString("\t");
	    tester.keyString(password+"\t");
	    tester.keyString(password+"\t");
	    tester.keyString(password+"\t");
	    // wait for submission screen
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    // renew03.png used to be a password dialog, which was removed
	    // submit page
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew04.png"));
	    Thread.sleep(3000);
	    // wait for approval page
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew05.png"));
	    // close wizard
	    guiSleep();
	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew06.png"));
	    // enable certificate in LocalCA and refresh pane
	    System.setProperty("jgridstart.ca.local.hold", "false");
	    tester.key(KeyEvent.VK_F5);
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew07.png"));
	    // show request wizard again
	    tester.key(new Integer('A'), InputEvent.ALT_MASK);
	    tester.key('R');
	    Thread.sleep(2500);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew08.png"));
	    // install step
	    tester.key(new Integer('N'), InputEvent.ALT_MASK);
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew09.png"));
	    // exit wizard
	    tester.key(new Integer('C'), InputEvent.ALT_MASK);
	    // save final screenshot
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"renew10.png"));
	    guiSleep();
	    
	    /*
	     * Import/export
	     */
	    // forget password so we certainly get the password dialog
	    PasswordCache.getInstance().clear();
	    // starting screenshot (multiple certificates)
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"importexport01.png"));
	    // export dialog
	    tester.key(new Integer('E'), InputEvent.CTRL_MASK);
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"importexport02.png"));
	    // enter name and do export
	    tester.keyString("my_certificate.p12\n");
	    Thread.sleep(1000);
	    saveScreenshot(new File(shotdir, prefix+"importexport03.png"));
	    tester.keyString("\t\t"); // update when passwordcache dialog focuses proper field
	    tester.keyString(password+"\n");
	    guiSleep();
	    
	    // forget password so we certainly get the password dialog
	    PasswordCache.getInstance().clear();
	    
	    // import dialog
	    tester.key(new Integer('I'), InputEvent.CTRL_MASK);
	    Thread.sleep(1000);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"importexport04.png"));
	    guiSleep();
	    // enter name and do import
	    tester.keyString("my_certificate.p12\n");
	    Thread.sleep(1000);
	    saveScreenshot(new File(shotdir, prefix+"importexport05.png"));
	    tester.keyString("\t\t"); // update when passwordcache dialog focuses proper field
	    tester.keyString(password+"\n");
	    guiSleep();

	    /*
	     * Certificate details
	     */
	    // certificate details view
	    Window jgwnd = AWT.getActiveWindow();
	    jgwnd.setSize(750, 420);
	    System.setProperty("view.showdetails", "true");
	    URLLauncherCertificate.performAction("viewlist(false)", tester.findFocusOwner());
	    tester.key(KeyEvent.VK_F5);
	    Thread.sleep(500);
	    guiSleep();
	    saveScreenshot(new File(shotdir, prefix+"viewdetails01.png"));
	    
	} finally {
	    guiSleep(); Thread.sleep(500); // for screenshot to complete ...
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
		    img.flush();
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
	    tester.waitForIdle();
	    /*
	    java.awt.EventQueue.invokeAndWait(new Runnable() {
	        public void run() { }
	    });
	    */
	} catch (Exception e) { throw new AWTException(e.toString()); }
    }
}
