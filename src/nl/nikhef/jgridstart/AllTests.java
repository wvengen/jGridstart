package nl.nikhef.jgridstart;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import nl.nikhef.jgridstart.gui.util.TemplateButtonPanelTest;
import nl.nikhef.jgridstart.install.BrowsersMacOSXTest;
import nl.nikhef.jgridstart.util.FileUtilsTest;
import nl.nikhef.jgridstart.util.PasswordCacheTest;
import nl.nikhef.xhtmlrenderer.swing.TemplateDocumentTest;
import nl.nikhef.xhtmlrenderer.swing.TemplatePanelTest;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite("Test for nl.nikhef.jgridstart");
	//$JUnit-BEGIN$
	suite.addTestSuite(FileUtilsTest.class);
	suite.addTestSuite(PasswordCacheTest.class);
	suite.addTestSuite(CertificateCheckTest.class);
	suite.addTestSuite(CertificateStoreTest.class);
	suite.addTestSuite(CertificateStoreTest2.class);
	suite.addTestSuite(BrowsersMacOSXTest.class);
	suite.addTestSuite(TemplateDocumentTest.class);
	suite.addTestSuite(TemplatePanelTest.class);
	suite.addTestSuite(TemplateButtonPanelTest.class);
	//$JUnit-END$
	return suite;
    }
    
    /** GUI test program that runs unit tests */
    public static void main(String[] args) {
	// initial user message
	int ret = JOptionPane.showConfirmDialog(null,
		"Thank you for running the jGridstart testing program. Your cooperation enables us to\n" +
		"improve the software. After you press Ok, please wait while the tests are running,\n" +
		"until a message appears about the tests being done.",
		"jGridStart Testing Program", JOptionPane.OK_CANCEL_OPTION);
	if (ret!=JOptionPane.OK_OPTION) return;
	
	// TODO redirect messages to output
	
	// run tests
	JFrame frame = new JFrame("jGridstart Testing Program");
	frame.add(new JLabel("     Please don't touch your computer until testing is complete...     "));
	frame.setSize(400, 250);
	frame.setVisible(true);
	TestResult result = junit.textui.TestRunner.run(suite());
	frame.setVisible(false);
	
	// generate report, ask user confirmation
	// TODO generate report and submit information
	JOptionPane.showMessageDialog(null,
		"The tests have been run, resulting in " + result.errorCount() + " failures.\n" +
		"\n" +
		"Thank you for your help.",
		"jGridStart Testing Program", JOptionPane.OK_OPTION|JOptionPane.INFORMATION_MESSAGE);
	System.exit(0);
    }

    /*
	"jGridstart version: "+System.getProperty("jgridstart.version")+ 
	" (rev "+System.getProperty("jgridstart.revision")+")\n" +
	"Java runtime environment version: "+System.getProperty("java.version"),
     */

}
