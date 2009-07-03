package nl.nikhef.jgridstart;

import nl.nikhef.jgridstart.gui.util.TemplateButtonPanelTest;
import nl.nikhef.jgridstart.install.BrowsersMacOSXTest;
import nl.nikhef.jgridstart.util.PasswordCacheTest;
import nl.nikhef.xhtmlrenderer.swing.TemplateDocumentTest;
import nl.nikhef.xhtmlrenderer.swing.TemplatePanelTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite("Test for nl.nikhef.jgridstart");
	//$JUnit-BEGIN$
	suite.addTestSuite(PasswordCacheTest.class);
	suite.addTestSuite(CertificateCheckTest.class);
	suite.addTestSuite(BrowsersMacOSXTest.class);
	suite.addTestSuite(TemplateDocumentTest.class);
	suite.addTestSuite(TemplatePanelTest.class);
	suite.addTestSuite(TemplateButtonPanelTest.class);
	//$JUnit-END$
	return suite;
    }

}
