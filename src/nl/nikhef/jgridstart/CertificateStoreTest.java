package nl.nikhef.jgridstart;

import java.io.File;
import java.io.IOException;

import nl.nikhef.jgridstart.util.FileUtils;

import org.junit.Test;
import junit.framework.TestCase;

public class CertificateStoreTest extends TestCase {
    
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
     * @param num number of certificates to put in; must be <=6
     */
    protected File newTestStore(int num) throws IOException {
	File path = FileUtils.createTempDir("test-store", tmpBasePath);
	for (int i=1; i<=num; i++) {
	    addCopyTest("testO-0"+i, path);
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
	    File newfile = new File(path, copyFrom.getName());
	    FileUtils.CopyFile(files[j], newfile);
	    newfile.deleteOnExit();
	}
	return path;
    }
    
    /** Load {@linkplain CertificateStore} from string path */
    @Test
    public void testLoadString() throws Exception {
	File path = newTestStore(1);
	CertificateStore store = new CertificateStore();
	store.load(path.getPath());
	assertEquals(1, store.size());
    }

    /** Load {@linkplain CertificateStore} from path */
    @Test
    public void testLoadFile() throws Exception {
	File path = newTestStore(1);
	CertificateStore store = new CertificateStore();
	store.load(path);
	assertEquals(1, store.size());
	// TODO finish
    }
    
    /** Load {@linkplain CertificateStore} from path in constructor */
    @Test
    public void testConstructWithFile() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	assertEquals(1, store.size());
    }

    /** Test if refresh picks up newly added item */
    @Test
    public void testRefreshAdd() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(0));
	assertEquals(0, store.size());
	addCopyTest("testO-05", store.path);
	store.refresh();
	assertEquals(1, store.size());
    }

    /** Test if refresh picks up removed item */
    @Test
    public void testRefreshRemove() throws Exception {
	File path = newTestStore(0);
	File entry = addCopyTest("testO-01", path);
	CertificateStore store = new CertificateStore(path);
	assertEquals(1, store.size());
	recursiveDelete(entry);
	store.refresh();
	assertEquals(0, store.size());
    }
    
    /** Test removal by index */
    @Test
    public void testDeleteInt() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(2));
	assertEquals(2, store.size());
	store.delete(1);
	assertEquals(1, store.size());
	store.delete(0);
	assertEquals(0, store.size());
    }

    /** Test removal by {@linkplain CertificatePair} */
    @Test
    public void testDeleteCertificatePair() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(3));
	assertEquals(3, store.size());
	store.delete(store.get(2));
	store.delete(store.get(0));
	assertEquals(1, store.size());
    }
    
    @Test
    public void testGetDefault() throws Exception {
	fail("Not yet implemented");
    }

    @Test
    public void testSetDefault() throws Exception {
	fail("Not yet implemented");
    }
}
