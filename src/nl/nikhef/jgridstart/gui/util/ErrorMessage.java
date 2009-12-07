package nl.nikhef.jgridstart.gui.util;

import java.awt.Component;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Uniform way to handle error messages.
 * <p>
 * This shows an error dialog to the user, and logs the exception, if any.
 */
public class ErrorMessage {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui.util");
    
    /** show an error to the user. This method is for errors that are
     * meaningful to the user, such as an IOException.
     * 
     * @param parent Parent window
     * @param title Title of the dialog
     * @param e Exception to get information from
     * @param extramsg Additional error message details to display
     */
    public static void error(Component parent, String title, Throwable e, String extramsg) {
	logException(e);
	String msg = e.getLocalizedMessage();
	if (msg==null || msg=="") msg = "Unknown error";
	if (extramsg!=null) msg += "\n" + extramsg;
	showErrorDialog(parent, title, msg);
    }
    /** @see #error(Component, String, Throwable, String) */
    public static void error(Component parent, String title, Throwable e) {
	error(parent, title, e, null);
    }
    /** @see #error(Component, String, Throwable, String) */
    public static void error(Component parent, String title, Exception e, String extramsg) {
	error(parent, title, (Throwable)e, extramsg);
    }
    /** @see #error(Component, String, Throwable, String) */
    public static void error(Component parent, String title, Exception e) {
	error(parent, title, (Throwable)e, null);
    }
    
    /** show an error to the user. This method is for errors that should
     * not occur; the dialog indicates that the user can contact
     * the developers.
     * 
     * @param parent Parent window
     * @param e Exception to get information from
     */
    public static void internal(Component parent, Throwable e) {
	logException(e);
	internal(parent, e.getLocalizedMessage());
    }
    /** @see #internal(Component, Throwable) */
    public static void internal(Component parent, Exception e) {
	internal(parent, (Throwable)e);
    }

    /** show an error to the user. This method is for errors that should
     * not occur; the dialog indicates that the user can contact
     * the developers.
     * 
     * @param parent Parent window
     * @param msg Message to describe to error
     */
    public static void internal(Component parent, String msg) {
	String s = "I'm sorry to report that an unexpected internal error occured.\n"
	          +"Please contact technical support for help.\n";
	// TODO include contact details for technical support
	showErrorDialog(parent, "Internal problem", s+msg);
    }
    
    /** logs an exception */
    public static void logException(Throwable e) {
	logger.warning("[Exception] "+e.getMessage());
	StackTraceElement[] trace = e.getStackTrace();
	for (int i=0; i<trace.length; i++)
	    logger.fine("  "+trace[i].toString());
    }
    
    /** show dialog with error message to user */
    private static void showErrorDialog(Component parent, String title, String msg) {
	// message
	JTextArea area = new JTextArea(msg);
	area.setEditable(false);
	JLabel dummylbl = new JLabel();
	area.setBackground(dummylbl.getBackground()); // use JLabel layout 
	area.setForeground(dummylbl.getForeground());
	area.setFont(dummylbl.getFont());
	JScrollPane pane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	pane.setBorder(null);
	JOptionPane.showMessageDialog(parent, new Object[] { pane }, title, JOptionPane.ERROR_MESSAGE);
    }
}
