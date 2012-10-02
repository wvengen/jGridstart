package nl.nikhef;

import nl.nikhef.jgridstart.gui.util.ScreenshotsGuitest;
import nl.nikhef.jgridstart.logging.LogHelper;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junitx.util.ArchiveSuiteBuilder;
import junitx.util.TestFilter;

public class AllTests {
    public static void main(String[] args) throws Exception {
	// TODO enable full logging
	//LogHelper.setupLogging(true); // does not seem to do anything
	// TODO enable test logging (fail/error/success)

	// add jar this class runs in as source
	String myjarpath = AllTests.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	ArchiveSuiteBuilder builder = new ArchiveSuiteBuilder();
	// first normal tests
	System.out.println("**** RUNNING NON-GUI TESTS (1/3)");
	builder.setFilter(new MyTestFilter("Test", null));
	Test suite = builder.suite(myjarpath);
	TestResult result = new TestResult();
	suite.run(result);
	// then GUI tests
	System.out.println("**** RUNNING GUI TESTS (2/3)");
	builder.setFilter(new MyTestFilter("Guitest", "ScreenshotsGuitest"));
	TestResult resultgui = new TestResult();
	Test suitegui = builder.suite(myjarpath);
	suitegui.run(resultgui);
	// finally run automation GUI test going through all of the steps
	System.out.println("**** RUNNING GUI WALKTHROUGH TEST (3/3)");
	TestSuite suitewk = new TestSuite();
	TestResult resultwk = new TestResult();
	suitewk.addTestSuite(ScreenshotsGuitest.class);
	suitewk.run(resultwk);
    }

    // test filter for packages in same package as me (and below)
    // and with given class ending name
    private static class MyTestFilter implements TestFilter {
	private String testinc = null;
	private String testexc = null;
	public MyTestFilter(String include, String exclude) {
	    this.testinc = include;
	    this.testexc = exclude;
	}
	public boolean include(String f) {
	    String fqcn = f.replace('/','.');
	    return fqcn.startsWith(AllTests.class.getPackage().getName());
	}
	public boolean include(Class c) {
	    return (testinc==null ||  c.getName().endsWith(testinc)) &&
		    (testexc==null || !c.getName().endsWith(testexc));
	}
    }
}
