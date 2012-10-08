package nl.nikhef;

import java.lang.reflect.Method;

import nl.nikhef.jgridstart.gui.util.ScreenshotsGuitest;
import nl.nikhef.jgridstart.logging.LogHelper;
import junit.framework.Test;
import junit.framework.TestCase;
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
	builder.setFilter(new MyTestFilter("Test", "BaseTest"));
	Test suitecli = builder.suite(myjarpath);
	// then gui tests
	builder.setFilter(new MyTestFilter(
		new String[]{"Guitest"},
		new String[]{"BaseTest", "ScreenshotsGuitest"}));
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
	//lLogHelper.setupLogging(true); // does not seem to do anything
	// TODO enable test logging (fail/error/success)
	Test suite = suite();
	TestResult result = new TestResult();
	suite.run(result);
    }

    // test filter for packages in same package as me (and below)
    // and with given class ending name
    private static class MyTestFilter implements TestFilter {
	private String[] testinc = null;
	private String[] testexc = null;
	public MyTestFilter(String[] include, String[] exclude) {
	    this.testinc = include;
	    this.testexc = exclude;
	}
	public MyTestFilter(String include, String exclude) {
	    this(new String[]{include}, new String[]{exclude});
	}
	public MyTestFilter(String[] include) {
	    this(include, null);
	}
	public boolean include(String f) {
	    String fqcn = f.replace('/','.');
	    return fqcn.startsWith(AllTests.class.getPackage().getName());
	}
	public boolean include(Class c) {
	    boolean found = false;
	    if (testinc!=null)
		for (int i=0; i<testinc.length; i++)
		    if (c.getName().endsWith(testinc[i])) found=true;
	    if (testexc!=null)
		for (int i=0; i<testexc.length; i++)
		    if (c.getName().endsWith(testexc[i])) return false;
	    // class must extend TestCase
	    if (!TestCase.class.isAssignableFrom(c)) return false;
	    // class must have a method matching test name to avoid errors
	    Method[] methods = c.getMethods();
	    for (int i=0; i<methods.length; i++) {
		if (methods[i].getName().startsWith("test")) return true;
	    }
	    // none found
	    return false;
	}
    }
}
