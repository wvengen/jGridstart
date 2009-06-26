package nl.nikhef.jgridstart.gui.util;

import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanelTest;

public class TemplateButtonPanelTest extends ITemplatePanelTest {
    @Override
    protected ITemplatePanel createPanel() {
	return new TemplateButtonPanel();
    }
}
