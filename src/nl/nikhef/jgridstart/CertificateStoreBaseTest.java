package nl.nikhef.jgridstart;
import java.io.File;
import java.io.IOException;

import nl.nikhef.jgridstart.util.FileUtils;
import junit.framework.TestCase;

/** Base class for {@link CertificateStore} tests */
public abstract class CertificateStoreBaseTest extends TestCase {
    
    protected File tmpBasePath = null;
    
    @Override
    public void setUp() throws Exception {
	// create temp dir to work in
	tmpBasePath = FileUtils.createTempDir("test-certificatestore");
    }
    
    @Override
    public void tearDown() throws Exception {
	// remove temp dir
	recursiveDelete(tmpBasePath);
    }
    
    /** Helper method: remove directory recursively.
     * <p>
     * Beware, symlinks might traverse into unwanted territory!
     */
    protected static void recursiveDelete(File what) {
	File[] children = what.listFiles();
	for (int i=0; i<children.length; i++) {
	    File c = children[i];
	    // make sure we can read/write it so traversal and deletion are possible
	    FileUtils.chmod(c, true, true, c.isDirectory(), true);
	    if (c.isDirectory())
		recursiveDelete(c);
	    c.delete();
	}
	what.delete();
    }
    
    /** Helper method: create new certificate store directory in temp space.
     * <p>
     * This is cleaned up automatically by {@link #tearDown}.
     * 
     * @param num number of certificates to put in
     */
    protected File newTestStore(int num) throws IOException {
	File path = FileUtils.createTempDir("test-store", tmpBasePath);
	for (int i=1; i<=num; i++) {
	    addCopyTest("testO-01", path);
	}
	return path;
    }
    
    /** Helper method: create new entry in {@linkplain CertificateStore} copied from tests.
     * 
     * @param name Test name in {@code CertificateCheck-tests/}
     * @param store Path of {@linkplain CertificateStore} to make new entry in
     * @return path of newly created entry
     * @throws IOException
     */
    protected static File addCopyTest(String name, File store) throws IOException {
	File path = FileUtils.createTempDir("user-cert-", store);
	File copyFrom = CertificateCheckTest.getResourceFile(name);
	File[] files = FileUtils.listFilesOnly(copyFrom);
	for (int j=0; j<files.length; j++) {
	    File newfile = new File(path, files[j].getName());
	    FileUtils.CopyFile(files[j], newfile);
	    newfile.deleteOnExit();
	}
	return path;
    }
}
