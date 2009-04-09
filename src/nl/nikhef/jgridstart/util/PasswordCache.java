package nl.nikhef.jgridstart.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.bouncycastle.openssl.PasswordFinder;

/** Class that caches passwords for a limited time so that the user doesn't
 * need to type in the password everytime.
 * 
 * TODO move swing stuff into gui, implement cli as well, auto-select cli/gui
 *      this can be done by implementing this class with callbacks for asking
 *      the user for passwords. Different UIs can then use their own callbacks,
 *      and even the tests can use this to test easily without user intervention.
 * 
 * @author wvengen
 */
public class PasswordCache {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
    
    /** UI: no questions asked, returns null if password isn't in cache */
    public static final int UI_NONE = 0;
    /** UI: graphical, pops up a dialog when password isn't in cache */
    public static final int UI_GUI = 1;
    /** TODO UI: console, asks for a password on the console */
    public static final int UI_CLI = 2;
    /** user interface backend, one of UI_* */
    protected int ui = UI_GUI;

    
    /** the actual passwords */
    protected HashMap<String, char[]> passwords = null;
    /** list of forget-timers for each password */
    protected HashMap<String, Timer> timers = null;
    /** parent frame for dialogs */
    protected JFrame parent = null;
    /** number of seconds after which passwords are forgotten */
    protected int timeout = 5 * 60;
    
    
    protected PasswordCache() {
	passwords = new HashMap<String, char[]>();
	timers = new HashMap<String, Timer>(); 
    }
    
    /** Reference to singleton instance */
    protected static PasswordCache instance = null;
    /** Retrieve singleton object */
    static public PasswordCache getInstance() {
	if (instance==null) {
	    instance = new PasswordCache();
	}
	return instance;
    }
    
    /** Set the parent for dialogs */
    public void setParent(JFrame parent) {
	this.parent = parent;
    }
    /** Set the timeout after which passwords are forgotten again.
     * Also updates timeout of currently stored passwords.
     * 
     * @param timeout timeout in number of seconds
     */
    public void setTimeout(int s) {
	this.timeout = s;
	// update timeout of current passwords
	for (Iterator<String> it = timers.keySet().iterator(); it.hasNext(); ) {
	    touch(it.next());
	}
    }
    /** Set the user-interface backend.
     * 
     * @param ui One of UI_NONE, UI_GUI and UI_CLI
     */
    public void setUI(int ui) {
	this.ui = ui;
    }
    
    /** Invalidate a cache entry */
    protected void invalidate(String loc) {
	// overwrite password for a little security
	if (passwords.containsKey(loc)) {
	    Arrays.fill(passwords.get(loc), '\0');
	    passwords.remove(loc);
	    logger.finest("Password removed on timeout for "+loc);
	}
	// remove timer stuff
	if (timers.containsKey(loc)) {
	    timers.get(loc).cancel();
	    timers.remove(loc);
	}
    }
    
    /** Return the password for the specified location to decrypt files
     * 
     * Each item has a unique reference to a required password consisting of
     * a location string.
     * 
     * @param msg Message to present to the user when asking for a password.
     *            Actual message is "Enter password to unlock "+msg
     * @param loc Location string
     * @return password
     */
    public char[] getForDecrypt(String msg, String loc) {
	// try if entry exists
	if (passwords.containsKey(loc)) {
	    touch(loc);
	    return passwords.get(loc);
	}
	// else ask from user
	char[] pw = null;
	switch(ui) {
	case UI_GUI:
	    // ask for password
	    // TODO focus is moved from password entry to ok button!!!
	    JOptionPane pane = new JOptionPane();
	    pane.setMessageType(JOptionPane.PLAIN_MESSAGE);
	    pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
	    JLabel lbl = new JLabel("Enter password to unlock "+msg);
	    final JPasswordField pass = new JPasswordField(25);
	    pane.setMessage(new Object[] {lbl, pass});
	    JDialog dialog = pane.createDialog(parent, "Enter password");
	    logger.fine("Requesting decryption password for "+loc);
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    pass.requestFocusInWindow();
		}
	    });
	    dialog.setVisible(true);
	    if (((Integer)pane.getValue()) == JOptionPane.OK_OPTION) {
		// store password and zero out for a little security
		pw = pass.getPassword();
	    }
	    break;
	case UI_CLI:
	    logger.severe("UI_CLI not implemented");
	    return null;
	case UI_NONE:
	    logger.fine("Decryption password not present for "+loc);
	    return null;
	}
	if (pw!=null) {    
	    set(loc, pw);
	    logger.fine("Decryption password entered for "+loc);
	    return pw;
	}
	logger.fine("Decryption password request cancelled for "+loc);
	return null;
    }
    
    /** Return a new password for the specified location. Also ask for a password
     * and require to enter twice for verification. This is for encrypting data.
     * The password is remembered for the case it is read again.
     *
     * @param msg Description presented to the user when asking for a password.
     * @param loc Location string
     * @return password
     */
    public char[] getForEncrypt(String msg, String loc) {
	// always ask for password when writing
	// TODO focus is moved from password entry to ok button!!!
	JOptionPane pane = new JOptionPane();
	pane.setMessageType(JOptionPane.PLAIN_MESSAGE);
	pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
	JLabel lbl1 = new JLabel("Enter password for saving "+msg+".");
	JLabel lbl2 = new JLabel("Please enter the same password and avoid mistakes.");
	final JPasswordField pass1 = new JPasswordField(25);
	final JPasswordField pass2 = new JPasswordField(25);
	pane.setMessage(new Object[] {lbl1, lbl2, pass1, pass2});
	JDialog dialog = pane.createDialog(parent, "Enter password");
	logger.fine("Requesting encryption password for "+loc);
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		pass1.requestFocusInWindow();
	    }
	});
	dialog.setVisible(true);
	if (((Integer)pane.getValue()) == JOptionPane.OK_OPTION) {
	    // store password and zero out for a little security
	    char[] pw1 = pass1.getPassword();
	    char[] pw2 = pass2.getPassword();
	    if (!Arrays.equals(pw1, pw2)) {
		// TODO ask again
		Arrays.fill(pw1, '\0');
		Arrays.fill(pw2, '\0');
		logger.severe("Passwords don't match.");
		return null;
	    }
	    set(loc, pw1);
	    Arrays.fill(pw2, '\0');
	    return pw1;
	}
	logger.fine("Encryption password request cancelled for "+loc);
	return null;
    }

    /** Set a password for a location. Should not be used normally because the
     * user is the one to give the password. For testing this is convenient. 
     * 
     * @param loc Location string
     * @param index index in location
     * @param pw password to set
     */
    public void set(String loc, char[] pw) {
	passwords.put(loc, pw);
	touch(loc);
	logger.fine("Password set for "+loc);
    }
    
    /** Completely clear the cache so that no passwords are present. */
    public void clear() {
	Set<String> locations = new HashSet<String>(passwords.keySet());
	for (Iterator<String> it=locations.iterator(); it.hasNext(); ) {
	    invalidate(it.next());
	}
    }
    
    /** Reset the forget timeout for a location. Password mustexist already. */
    protected void touch(String loc) {
	if (!passwords.containsKey(loc)) return;
	if (timers.containsKey(loc)) {
	    timers.get(loc).cancel();
	    timers.remove(loc);
	}
	new ForgetTask(loc);
    }
    
    /** Return a PasswordFinder that asks the user for a password when encrypting
     * a file. The password is retrieved using getForEncrypt(). 
     * 
     * @param msg Description on what this key is about
     * @param loc Location string
     * @param index index in location
     * @return a new PasswordFinder
     */
    public PasswordFinder getEncryptPasswordFinder(String msg, String loc) {
	return new CachePasswordFinder(true, msg, loc);
    }
    /** Return a PasswordFinder that returns the cached password, or else
     * asks the user to provide ine. The password is retrieved using getForDecrypt(). 
     * 
     * @param msg Description on what this key is about
     * @param loc Location string
     * @param index index in location
     * @return a new PasswordFinder
     */
    public PasswordFinder getDecryptPasswordFinder(String msg, String loc) {
	return new CachePasswordFinder(false, msg, loc);
    }
    
    /** A PasswordFinder interface for a password */
    private class CachePasswordFinder implements PasswordFinder {
	private String msg, loc;
	private boolean write;
	// TODO implement guess mode (all previous passwords) when no hardware device
	public CachePasswordFinder(boolean write, String msg, String loc) {
	    this.write = write;
	    this.msg = msg;
	    this.loc = loc;
	}
        public char[] getPassword() {
            if (write)
        	return getForEncrypt(msg, loc);
            else
        	return getForDecrypt(msg, loc);
        }
    }

    /** Timer task that removes the cached password after timeout */
    protected class ForgetTask extends TimerTask {
	protected String loc;
	public ForgetTask(String loc) {
	    this.loc = loc;
	    update();
	}
	/** update or create new timer task to forget password
	 * after the default timeout */
	public void update() {
	    if (timers.containsKey(loc))
		timers.get(loc).cancel();
	    Timer t = new Timer();
	    t.schedule(this, timeout*1000);
	    timers.put(loc, t);
	    logger.finest("Password timeout reset for "+loc);
	}
	public void run() {
	    invalidate(loc);
	}
    }
}
