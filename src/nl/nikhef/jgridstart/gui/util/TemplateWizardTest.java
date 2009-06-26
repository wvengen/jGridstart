package nl.nikhef.jgridstart.gui.util;

import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanelTest;

public class TemplateWizardTest extends ITemplatePanelTest {
    @Override
    protected ITemplatePanel createPanel() {
	return new TemplateWizard();
    }
    
    public static void main(String[] args) throws Exception {
	TemplateWizard panel = new TemplateWizard();
	ITemplatePanelTest.main(panel, args);
    }
}
