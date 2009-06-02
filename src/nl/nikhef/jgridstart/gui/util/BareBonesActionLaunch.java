package nl.nikhef.jgridstart.gui.util;

import java.net.URL;
import java.util.HashMap;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Action;

/**
 * Launch a web page in an external browser but modify action: links to run UI
 * Actions instead.
 * 
 * @author wvengen
 */
public class BareBonesActionLaunch extends BareBonesBrowserLaunch {
    
    /** list of registered actions */
    protected static HashMap<String, Action> actions = new HashMap<String, Action>();

    /** @see BareBonesBrowserLaunch#openURL(URL, Component) */
    public static void openURL(URL url, Component parent) {
	if (url.toExternalForm().startsWith("action:"))
	    performAction(url.toExternalForm().substring(7), parent);
	else
	    BareBonesBrowserLaunch.openURL(url.toExternalForm(), parent);
    }

    /** @see BareBonesBrowserLaunch#openURL(String, Component) */
    public static void openURL(String surl, Component parent) {
	if (surl.startsWith("action:"))
	    performAction(surl.substring(7), parent);
	else
	    BareBonesBrowserLaunch.openURL(surl, parent);
    }
    
    /** @see BareBonesBrowserLaunch#openURL(URL) */
    public static void openURL(URL url) {
	openURL(url, null);
    }
    /** @see BareBonesBrowserLaunch#openURL(String) */
    public static void openURL(String surl) {
	openURL(surl, null);
    }
    
    /** Perform a certain action
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
