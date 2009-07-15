package nl.nikhef.jgridstart.gui.util;

import java.net.URL;
import java.util.HashMap;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import nl.nikhef.jgridstart.install.BrowserFactory;

/** URL Handler to external webbrowser and {@linkplain Action} invocation.
 * <p>
 * This method is used to handle opening of URLs. All URLs are
 * opened with an external web browser via {@link BrowserFactory}, but
 * {@code action:} URLs are handled specially.
 * <p>
 * An {@code action:} URL activates an {@link Action}s {@linkplain Action#actionPerformed}
 * method, so it is equivalent to selecting a menu item or pressing a button.
 * Before an action can be called as URL, it must be made known, for example:
 * <code><pre>
 *   class CoolAction extends AbstractAction {
 *     public CoolAction() {
 *       super();
 *       putValue(NAME, "Cool thing...");
 *       URLLauncher.addAction("cool", this);
 *     }
 *     public void actionPerformed(ActionEvent e) {
 *       System.out.println("Cool down "+e.getActionCommand());
 *     }
 *   }
 * <pre></code>
 * It is then possible to launch the action by invoking
 * {@code URLLauncher.openURL("action:cool")}.
 * <p>
 * Actions can have an argument specified in brackets, which sets the
 * {@link ActionEvent}'s {@link ActionEvent#getActionCommand command}. So when
 * {@code URLLauncher.openURL("action:cool(please)")} would be invoked, 
 * {@code CoolAction}'s {@code actionPerformed} method would print
 * <tt>Cool down please</tt>.
 * <p>
 * The API is compatible with
 * <a href="http://www.nikhef.nl/pub/projects/grid/gridwiki/index.php/User:WvEngen/BareBonesBrowserLaunch">BareBonesBrowserLaunch</a>
 * (<a href="http://www.centerkey.com/java/browser/">original version</a>), so this could be
 * used as a drop-in replacement to add action-handling.
 * 
 * @author wvengen
 */
public class URLLauncher {
    
    /** list of registered actions */
    protected static HashMap<String, Action> actions = new HashMap<String, Action>();

    /** Open a URL
     * 
     * @param url url to open
     * @param parent parent component for error dialog
     */
    public static void openURL(URL url, Component parent) {
	if (url.toExternalForm().startsWith("action:"))
	    performAction(url.toExternalForm().substring(7), parent);
	else try {
	    BrowserFactory.getInstance().openUrl(url.toExternalForm());
	    //BareBonesBrowserLaunch.openURL(url.toExternalForm(), parent);
	} catch (Exception e) {
	    ErrorMessage.error(parent, "Could not launch URL", e);
	}
    }

    /** Open a string URL
     * 
     * @param surl url to open (as String)
     * @param parent parent component for error dialog
     */
    public static void openURL(String surl, Component parent) {
	if (surl.startsWith("action:"))
	    performAction(surl.substring(7), parent);
	else try {
	    BrowserFactory.getInstance().openUrl(surl);
	    //BareBonesBrowserLaunch.openURL(surl, parent);
	} catch (Exception e) {
	    ErrorMessage.error(parent, "Could not launch URL", e);
	}
    }
    
    /** Open an URL
     * <p>
     * This is equal to {@link #openURL(URL, Component) openURL(url, null)}
     * so any error dialog will have no parent.
     * 
     * @param url url to open
     */
    public static void openURL(URL url) {
	openURL(url, null);
    }
    /** Open a string URL
     * <p>
     * This is equal to {@link #openURL(String, Component) openURL(url, null)}
     * so any error dialog will have no parent.
     * 
     * @param surl url to open (as String)
     */
    public static void openURL(String surl) {
	openURL(surl, null);
    }
    
    /** Perform an action.
     * <p>
     * This method is called for each {@code action:} URL.
     * 
     * @param action name of action to perform
     * @param src source object for event
     */
    public static void performAction(String action, Object src) {
	ActionEvent e = new ActionEvent(src, 0, "");
	actions.get(action).actionPerformed(e);
    }
    
    /** add an Action class to the list of recognised actions.
     * It is suggested to run this in an Action's constructor like this:
     * 
     * <code>
     *   class MyAction extends AbstractAction {
     *     
     *     public MyAction() {
     *       super("MyAction");
     *       ActionHandler.addAction("myaction", this);
     *     }
     *     
     *     public void actionPerformed(ActionEvent e) {
     *       // ...
     *     }
     *   }
     * </code>
     *  
     * @param name name in url, e.g. "myaction" to respond to url "action:myaction"
     * @param a Action to perform when url is opened
     */
    public static void addAction(String name, Action a) {
	actions.put(name, a);
    }
    
    /** Return a previously added action */
    public static Action getAction(String name) {
	return actions.get(name);
    }
}
