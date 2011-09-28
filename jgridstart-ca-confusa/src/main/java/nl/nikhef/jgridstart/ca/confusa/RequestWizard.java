package nl.nikhef.jgridstart.ca.confusa;

import java.awt.Dialog;
import java.awt.Frame;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardCommon;
import nl.nikhef.jgridstart.gui.wizard.RequestWizardPage;

public class RequestWizard extends RequestWizardCommon {
    
    protected RequestWizard(Frame parent) { super(parent); }
    protected RequestWizard(Dialog parent) { super(parent); }

    @Override
    protected void initialize() {
	super.initialize();
	// add the html pages
	try {
	    addPage(new PageLogin());
	    addPage(new PageDetailsUser());
	    addPage(new PageGenerateSubmit());
	    addPage(new PageInstall());
	    addPage(new RequestWizardPage("whatsnext", getClass().getResource("whatsnext_confusa.html")));
	} catch (Exception e) {
	    ErrorMessage.internal(this, e);
	}
    }
}