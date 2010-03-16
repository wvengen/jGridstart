package nl.nikhef.jgridstart;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.gui.util.CertificateFileChooserTest;
import nl.nikhef.jgridstart.gui.util.GUIScreenshotsTest;
import nl.nikhef.jgridstart.gui.util.TemplateButtonPanelTest;
import nl.nikhef.jgridstart.install.BrowsersMacOSXTest;
import nl.nikhef.jgridstart.logging.LogHelper;
import nl.nikhef.jgridstart.util.CryptoUtilsTest;
import nl.nikhef.jgridstart.util.FileUtilsTest;
import nl.nikhef.jgridstart.util.GeneralUtils;
import nl.nikhef.jgridstart.util.PasswordCacheTest;
import nl.nikhef.xhtmlrenderer.swing.TemplateDocumentTest;
import nl.nikhef.xhtmlrenderer.swing.TemplatePanelTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart");
    
    public static Test suite() {
	// setup logging and emit jGridstart version
	LogHelper.setupLogging(true);
	try {
	    Properties p = GeneralUtils.getConfig();
	    logger.info("jGridstart version "+p.getProperty("jgridstart.version")+" (r"+p.getProperty("jgridstart.revision")+")");
	} catch (IOException e) { }
	TestSuite suite = new TestSuite("Test for nl.nikhef.jgridstart");
	//$JUnit-BEGIN$
	suite.addTestSuite(FileUtilsTest.class);
	suite.addTestSuite(CryptoUtilsTest.class);
	suite.addTestSuite(PasswordCacheTest.class);
	suite.addTestSuite(CertificateCheckTest.class);
	suite.addTestSuite(CertificateStore1Test.class);
	suite.addTestSuite(CertificateStore2Test.class);
	suite.addTestSuite(CertificateStoreWithDefaultTest.class);
	suite.addTestSuite(BrowsersMacOSXTest.class);
	suite.addTestSuite(CertificateFileChooserTest.class);
	suite.addTestSuite(TemplateDocumentTest.class);
	suite.addTestSuite(TemplatePanelTest.class);
	suite.addTestSuite(TemplateButtonPanelTest.class);
	suite.addTestSuite(GUIScreenshotsTest.class);
	//$JUnit-END$
	return suite;
    }
}
