package nl.nikhef.xhtmlrenderer.swing;

import javax.swing.JTextField;

import org.junit.Test;

import abbot.finder.matchers.ClassMatcher;

public class TemplatePanelGuitest extends ITemplatePanelTest {
    @Override
    protected ITemplatePanel createPanel() {
	return new TemplatePanel();
    }
    
    /** Test if {@link TemplatePanel#getFormComponent} works */    
    @Test
    public void testFormFinder() throws Exception {
	createPanel(" <input type='text' name='myname' value='hi'/>");
	JTextField field = (JTextField)find(new ClassMatcher(JTextField.class));
	JTextField found = (JTextField)panel.getFormComponent("myname");
	assertEquals(field, found);
    }
    
    public static void main(String[] args) throws Exception {
	ITemplatePanelTest.main(new TemplatePanel(), args);
    }
}
