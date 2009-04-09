package nl.nikhef.jgridstart;

import nl.nikhef.jgridstart.util.PasswordCacheTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite("Test for nl.nikhef.jgridstart");
	//$JUnit-BEGIN$
	suite.addTestSuite(PasswordCacheTest.class);
	//$JUnit-END$
	return suite;
    }

}
