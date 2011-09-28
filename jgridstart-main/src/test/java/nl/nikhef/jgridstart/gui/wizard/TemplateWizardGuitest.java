package nl.nikhef.jgridstart.gui.wizard;

import java.io.ByteArrayInputStream;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import org.junit.Test;
import abbot.finder.matchers.NameMatcher;

import nl.nikhef.jgridstart.gui.util.TemplateButtonPanelGuitest;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;

public class TemplateWizardGuitest extends TemplateButtonPanelGuitest {
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
	wiz.addPage(new TemplateWizardPage("start", getClass().getResource("testWizard1.html")));
	wiz.setPage("start");
	wiz.data().setProperty("foo", "foocontents!");
	showWindow(wiz);
	bodyEquals(wiz, "foocontents!");
    }
    /** Test next button */
    @Test
    public void testWizardNext() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.addPage(new TemplateWizardPage("start", getClass().getResource("testWizard1.html")));
	wiz.addPage(new TemplateWizardPage("end", getClass().getResource("testWizard2.html")));
	wiz.setPage("start");
	showWindow(wiz);
	JButton next = (JButton)find(new NameMatcher("wizard_next"));
	next.doClick();
	bodyEquals(wiz, "testWizard2body");
    }
    /** Test prev button */
    @Test
    public void testWizardPrevious() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.addPage(new TemplateWizardPage("1", getClass().getResource("testWizard1.html")));
	wiz.addPage(new TemplateWizardPage("2", getClass().getResource("testWizard2.html")));
	wiz.setPage("2");
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
	wiz.addPage(new TemplateWizardPage("0", getClass().getResource("testWizard1.html")));
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
	wiz.addPage(new TemplateWizardPage("1", getClass().getResource("testWizard1.html")) {
	    @Override
	    public void pageEnter(ITemplateWizardPage oldPage) { handlerPage=0; }
	});
	wiz.addPage(new TemplateWizardPage("new2", getClass().getResource("testWizard2.html")) {
	    @Override
	    public void pageEnter(ITemplateWizardPage oldPage) { handlerPage=1; }
	});
	handlerPage = -1;
	showWindow(wiz);
	wiz.setPage("1");
	assertEquals(0, handlerPage);
	wiz.setPage("new2");
	assertEquals(1, handlerPage);
	JButton prev = (JButton)find(new NameMatcher("wizard_previous"));
	prev.doClick();
	assertEquals(0, handlerPage);
    }
    /** Make sure buttons are not enabled when no prev/next page */
    @Test
    public void testWizardEmpty() throws Exception {
	TemplateWizard wiz = new TemplateWizard();
	wiz.addPage(new TemplateWizardPage("id", getClass().getResource("testWizard1.html")));
	showWindow(wiz);
	bodyEquals(wiz, "");
	assertFalse(((JButton)find(new NameMatcher("wizard_previous"))).isEnabled());
	assertFalse(((JButton)find(new NameMatcher("wizard_next"))).isEnabled());
    }
    
    
    public static void main(String[] args) throws Exception {
	final String testStyle = "<style type='text/css'><!--\n" +
	    "body { margin: 0; padding: 0; background: #eee; color: #000; }\n" +
	    ".wizard-title { position:fixed; top: 0; left: 0; height: 25pt; background: inherit; width: 100%; margin: 0; padding: 10pt; }\n" +
	    ".wizard-title > * { margin-top: 0; padding-top: 0; }\n" +
	    ".wizard-title h1 { font-weight: bold; font-size: 150%; }\n" +
	    ".wizard-contents { position: fixed; left: 0; top: 45pt; background: inherit; width: 5em; margin: 0; padding: 1ex 0 1ex 2.5em; height: 100%; }\n" +
	    ".wizard-contents > ul { margin: 0; padding: 0; }\n" +
	    ".wizard-contents .wizard-current { font-weight: bold; }\n" +
	    ".wizard-page { background: white; margin-left: 8em; margin-top: 45pt; padding: 1ex; }\n" +
	    "//--></style>";
	final String[] testPages = {
		"<html><head>"+testStyle+"<title>Page 1</title></head><body>" +
			"<div class='wizard-title'><h1>TemplateWizard test</h1></div>" +
			"<div class='wizard-contents' c='${wizard.contents.html}'/>" +
			"<div class='wizard-page'>"+ testBody + "</div>" +
			"</body></html>",
		"<html><head>"+testStyle+"<title>Page 2</title></head><body>" +
		"<div class='wizard-title'><h1>TemplateWizard test</h1></div>" +
			"<div class='wizard-contents' c='${wizard.contents.html}'/>" +
			"<div class='wizard-page'>Hi there</div>" +
			"</body></html>",
		"<html><head>"+testStyle+"<title>Page 3</title></head><body>" +
		"<div class='wizard-title'><h1>TemplateWizard test</h1></div>" +
			"<div class='wizard-contents' c='${wizard.contents.html}'/>" +
			"<div class='wizard-page'>Hello there then</div>" +
			"</body></html>",
	};

	// create wizard,
	TemplateWizard wiz = new TemplateWizard();
	// add pages
	for (int i=0; i<testPages.length; i++) {
	    Document doc = null;
	    try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		doc = builder.parse(new ByteArrayInputStream(testPages[i].getBytes()));
	    } catch(Exception e) { }
	    TemplateWizardPage mkpage = new TemplateWizardPage(String.valueOf(i), doc);
	    wiz.addPage(mkpage);
	}
	// and go!
	setTestPane(null, wiz);
	wiz.setPreferredSize(new Dimension(500,400));
	wiz.setVisible(true);
    }
}
