package nl.nikhef.jgridstart.gui.util;

import javax.swing.JButton;

import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanelTest;
import nl.nikhef.xhtmlrenderer.swing.TemplatePanelTest;

public class TemplateButtonPanelTest extends TemplatePanelTest {
    @Override
    protected ITemplatePanel createPanel() {
	return new TemplateButtonPanel();
    }
    
    public static void main(String[] args) throws Exception {
	TemplateButtonPanel panel = new TemplateButtonPanel();
	panel.addButton(new JButton("dummy"), false);
	panel.addButton(new JButton("dfldummy"), true);
	ITemplatePanelTest.main(panel, args);
    }
}
