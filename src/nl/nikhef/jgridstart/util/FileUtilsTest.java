package nl.nikhef.jgridstart.util;

import java.io.File;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Test;

public class FileUtilsTest extends TestCase {
    
    /** Helper method: make sure file is readable by user only.
     * <p>
     * This method only checks it if it is possible to do so on this system.
     */
    protected void assertUserOnly(File f) throws AssertionFailedError {
	// TODO implement
    }

    @Test
    public void testCopyFile() throws Exception  {
	fail("Not yet implemented");
    }

    @Test
    public void testReadFile() throws Exception  {
	fail("Not yet implemented");
    }

    @Test
    public void testChmod() throws Exception  {
	fail("Not yet implemented");
    }

    @Test
    public void testCreateTempDir() throws Exception {
	File tmpDir = FileUtils.createTempDir("foobar");
	assertTrue(tmpDir.exists());
	assertTrue(tmpDir.isDirectory());
	assertTrue(tmpDir.canRead());
	assertTrue(tmpDir.canWrite());
	assertUserOnly(tmpDir);
	tmpDir.delete();
	assertFalse(tmpDir.exists());
    }

    @Test
    public void testExecStringArray() throws Exception  {
	fail("Not yet implemented");
    }

    @Test
    public void testExecStringArrayStringStringBuffer() throws Exception  {
	fail("Not yet implemented");
    }

    @Test
    public void testExecStringArrayString() throws Exception  {
	fail("Not yet implemented");
    }

}
