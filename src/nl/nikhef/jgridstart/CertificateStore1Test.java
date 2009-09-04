package nl.nikhef.jgridstart;

import java.io.File;
import org.junit.Test;

/** Test basic operations of a {@link CertificateStore} */
public class CertificateStore1Test extends CertificateStoreBaseTest {
    
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
    
    /** Make sure user-stuff is not confusing the store */
    @Test
    public void testLitterIsOk() throws Exception {
	File path = newTestStore(1);
	// add litter
	new File(path, "some-cert-xxx").mkdir();
	new File(path, "foobar.pem").createNewFile();
	new File(path, "grix.properties").createNewFile();
	new File(path, "certificates").mkdir();
	File f = addCopyTest("testO-01", path);
	f.renameTo(new File(path, "cool"));
	// refresh and make sure it's still ok
	CertificateStore store = new CertificateStore(path);
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
}
