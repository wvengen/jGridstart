package nl.nikhef.jgridstart.gui.util;

import javax.swing.JButton;
import org.junit.Test;
import abbot.finder.matchers.NameMatcher;

import nl.nikhef.jgridstart.gui.util.TemplateWizard.PageListener;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanelTest;

public class TemplateWizardTest extends ITemplatePanelTest {
    @Override
    protected ITemplatePanel createPanel() {
	return new TemplateWizard();
    }

    /** page as returned by handler, if used */
    private int handlerPage;
    
    /** Test simple wizard with page */
    @Test
    public void testWizard1() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.pages.add(getClass().getResource("testWizard1.html"));
	wiz.setStep(0);
	wiz.data().setProperty("foo", "foocontents!");
	showWindow(wiz);
	bodyEquals(wiz, "foocontents!");
    }
    /** Test next button */
    @Test
    public void testWizardNext() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.pages.add(getClass().getResource("testWizard1.html"));
	wiz.pages.add(getClass().getResource("testWizard2.html"));
	wiz.setStep(0);
	showWindow(wiz);
	JButton next = (JButton)find(new NameMatcher("wizard_next"));
	next.doClick();
	bodyEquals(wiz, "testWizard2body");
    }
    /** Test prev button */
    @Test
    public void testWizardPrevious() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.pages.add(getClass().getResource("testWizard1.html"));
	wiz.pages.add(getClass().getResource("testWizard2.html"));
	wiz.setStep(0);
	wiz.data().setProperty("foo", "foocontents!");
	showWindow(wiz);
	JButton next = (JButton)find(new NameMatcher("wizard_next"));
	next.doClick();
	JButton prev = (JButton)find(new NameMatcher("wizard_previous"));
	prev.doClick();
	bodyEquals(wiz, "foocontents!");
    }
    /** Test close button */
    @Test
    public void testWizardCancel() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.pages.add(getClass().getResource("testWizard1.html"));
	wiz.setStep(0);
	showWindow(wiz);
	assertTrue(wiz.isVisible());
	JButton close = (JButton)find(new NameMatcher("wizard_close"));
	close.doClick();
	assertFalse(wiz.isVisible());
    }
    /** Test page switch handler */
    @Test
    public void testWizardHandler() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.pages.add(getClass().getResource("testWizard1.html"));
	wiz.pages.add(getClass().getResource("testWizard2.html"));
	handlerPage = -1;
	wiz.setHandler(new PageListener() {
	    public void pageChanged(TemplateWizard w, int page) {
		handlerPage = page;
	    }
	});
	showWindow(wiz);
	wiz.setStep(0);
	assertEquals(0, handlerPage);
	wiz.setStep(1);
	assertEquals(1, handlerPage);
	JButton prev = (JButton)find(new NameMatcher("wizard_previous"));
	prev.doClick();
	assertEquals(0, handlerPage);
    }
    /** Make sure buttons are not enabled when no prev/next page */
    @Test
    public void testWizardEmpty() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.pages.add(getClass().getResource("testWizard1.html"));
	showWindow(wiz);
	bodyEquals(wiz, "");
	assertFalse(((JButton)find(new NameMatcher("wizard_previous"))).isEnabled());
	assertFalse(((JButton)find(new NameMatcher("wizard_next"))).isEnabled());
    }
    
    
    public static void main(String[] args) throws Exception {
	TemplateWizard panel = new TemplateWizard();
	ITemplatePanelTest.main(panel, args);
    }
}
