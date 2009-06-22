package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import nl.nikhef.jgridstart.CertificateRequest;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.Organisation;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.gui.util.TemplateWizard;

public class ActionRequest extends AbstractAction {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    protected JFrame parent = null;
    protected CertificateStore store = null;
    protected CertificateSelection selection = null;
    
    public ActionRequest(JFrame parent, CertificateStore store, CertificateSelection selection) {
	super();
	this.parent = parent;
	this.store = store;
	this.selection = selection;
	putValue(NAME, "Request new...");
	putValue(MNEMONIC_KEY, new Integer('R'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
	BareBonesActionLaunch.addAction("request", this);
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	TemplateWizard dlg = new RequestWizard(parent, store, selection);
	Properties p = new Properties();
	// properties required for the forms
	p.setProperty("organisations.html.options", Organisation.getAllOptionsHTML());
	p.setProperty("organisations.html.options.volatile", "true");
	// help the user by prefilling some elements
	CertificateRequest.preFillData(p);
	// and go
	dlg.setData(p);
	dlg.setVisible(true);
    }
}
