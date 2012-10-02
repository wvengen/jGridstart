package nl.nikhef;

import nl.nikhef.jgridstart.gui.util.ScreenshotsGuitest;
import nl.nikhef.jgridstart.logging.LogHelper;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junitx.util.ArchiveSuiteBuilder;
import junitx.util.TestFilter;

public class AllTests {
    
    public static Test suite() throws Exception {
	// add jar this class runs in as source
	String myjarpath = AllTests.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	ArchiveSuiteBuilder builder = new ArchiveSuiteBuilder();
	// non-gui tests first
	builder.setFilter(new MyTestFilter("Test", null));
	Test suitecli = builder.suite(myjarpath);
	// then gui tests
	builder.setFilter(new MyTestFilter("Guitest", "ScreenshotsGuitest"));
	Test suitegui = builder.suite(myjarpath);
	// finally walkthrough gui test
	TestSuite suitewk = new TestSuite();
	suitewk.addTestSuite(ScreenshotsGuitest.class);
	// return combination of all
	TestSuite suitecombined = new TestSuite();
	suitecombined.addTest(suitecli);
	suitecombined.addTest(suitegui);
	suitecombined.addTest(suitewk);
	return suitecombined;
    }
    
    public static void main(String[] args) throws Exception {
	// TODO enable full logging
	//LogHelper.setupLogging(true); // does not seem to do anything
	// TODO enable test logging (fail/error/success)
	Test suite = suite();
	TestResult result = new TestResult();
	suite.run(result);
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
