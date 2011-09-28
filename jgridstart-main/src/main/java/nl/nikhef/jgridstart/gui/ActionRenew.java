package nl.nikhef.jgridstart.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JFrame;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.TemplateWizard;
import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
import nl.nikhef.jgridstart.gui.wizard.IRequestWizard;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardCommon;

/** Renew a certificate
 * 
 * @author wvengen
 */
public class ActionRenew extends CertificateAction {
    
    protected CertificateStore store = null;
    
    public ActionRenew(JFrame parent, CertificateStore store, CertificateSelection s) {
	super(parent, s);
	this.store = store;
	putValue(NAME, "Renew...");
	putValue(MNEMONIC_KEY, new Integer('W'));
	URLLauncherCertificate.addAction("renew", this);
    }

    @Override
    public boolean wantsEnabled() {
	try {
	    // need valid certificate 
	    if (getCertificatePair()==null) return false;
	    if (getCertificatePair().getCertificate()==null) return false;
	    if (!Boolean.valueOf(getCertificatePair().getProperty("valid"))) return false;
	    // demo certificates are not eligible for renewal
	    if (getCertificatePair().getProperty("level") == "demo") return false;
	    // ok!
	    return true;
	} catch (IOException e) {
	    return false;
	}
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	
	// show request wizard for renewal
	logger.finer("Action: "+getValue(NAME));
	IRequestWizard dlg = null;

	try {
	    Window w = findWindow(e.getSource());
	    if (w instanceof Frame)
		dlg = RequestWizardCommon.createInstance((Frame)w, store, selection, null, getCertificatePair());
	    else if (w instanceof Dialog)
		dlg = RequestWizardCommon.createInstance((Dialog)w, store, selection, null, getCertificatePair());
	    else
		ErrorMessage.internal(w, "Expected Frame or Dialog as owner");

	    dlg.setVisible(true);
	} catch (Exception e1) {
	    ErrorMessage.internal(parent, e1);
	}
    }
}
