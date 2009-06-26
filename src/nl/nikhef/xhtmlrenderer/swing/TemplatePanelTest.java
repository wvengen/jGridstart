package nl.nikhef.xhtmlrenderer.swing;

public class TemplatePanelTest extends ITemplatePanelTest {
    @Override
    protected ITemplatePanel createPanel() {
	return new TemplatePanel();
    }
    
    public static void main(String[] args) throws Exception {
	ITemplatePanelTest.main(new TemplatePanel(), args);
    }
}
