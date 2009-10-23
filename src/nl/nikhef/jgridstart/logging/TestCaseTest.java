package nl.nikhef.jgridstart.logging;

import org.junit.runner.RunWith;

import junit.framework.TestCase;

/** Extended JUnit {@linkplain TestCase} that logs each test as it runs.
 * <p>
 * Is called {@linkplain TestCaseTest} to make sure that it is filtered by
 * the build script to be included in the deployed version.
 * 
 * @author wvengen
 */
@RunWith(PrintRunner.class)
public class TestCaseTest extends TestCase {
    
}
