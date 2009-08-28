package nl.nikhef.jgridstart.gui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.install.BrowserFactory;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/** Open an external web page showing help */
public class ActionOpenURL extends AbstractAction {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    private String url = null;

    /** Create new action that opens an external web page.
     * 
     * @param parent
     * @param name Action name
     * @param mnemonic Mnemonic for action
     * @param url URL to open on activation
     */
    public ActionOpenURL(JFrame parent, String name, char mnemonic, String url) {
	super();
	this.url = url;
	putValue(NAME, name);
	putValue(MNEMONIC_KEY, new Integer(mnemonic));
    }

    public void actionPerformed(ActionEvent e) {
	logger.finer("Action OpenURL: "+getValue(NAME)+": "+url);
	try {
	    BrowserFactory.getInstance().openUrl(url);
	} catch (Exception e1) {
	    ErrorMessage.error(CertificateAction.findWindow(e.getSource()),
		    "Could not open external web page:\n"+url, e1);
	}
    }
}
