package nl.nikhef.jgridstart.gui.util;

import java.net.URL;
import java.io.ByteArrayInputStream;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import org.junit.Test;
import abbot.finder.matchers.NameMatcher;

import nl.nikhef.jgridstart.gui.util.TemplateWizard.PageListener;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanelTest;
import nl.nikhef.xhtmlrenderer.swing.TemplateDocument;

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
	final String testStyle = "<style type='text/css'><!--\n" +
	    "body { padding-left: 7em; }\n" +
	    ".wizard-contents { position: fixed; left: 0; top: 0; background: #eee; width: 4.5em; }\n" +
	    ".wizard-contents .wizard-current { color: green; }\n" +
	    "//--></style>";
	final String[] testPages = {
		"<html><head>"+testStyle+"<title>Page 1</title></head><body>" +
			"<div c='${wizard.contents.html}'/>" +
			testBody +
			"</body></html>",
		"<html><head>"+testStyle+"<title>Page 2</title></head><body>" +
			"<div c='${wizard.contents.html}'/>" +
			"<h1>Page two</h1><p>Hi there</p>" +
			"</body></html>",
		"<html><head>"+testStyle+"<title>Page 3</title></head><body>" +
			"<div c='${wizard.contents.html}'/>" +
			"<h1>Page three</h1><p>Hi there</p>" +
			"</body></html>",
	};
	
	TemplateWizard wiz = new TemplateWizard() {
	    // use embedded document as source instead of external pages
	    @Override
	    public TemplateDocument getDocument(int step) {
		try {
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    Document src = builder.parse(new ByteArrayInputStream(testPages[step].getBytes()));
		    return new TemplateDocument(src, data());
		} catch(Exception e) {
		    return null;
		}
	    }
	};
	// create dummy list so it knows the right number of pages
	for (int i=0; i<testPages.length; i++) wiz.pages.add(new URL("http://localhost/"));
	// and go!
	setTestPane(null, wiz);
	wiz.setPreferredSize(new Dimension(500,400));
	wiz.setVisible(true);
    }
}
