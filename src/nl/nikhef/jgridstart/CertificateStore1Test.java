package nl.nikhef.jgridstart;

import java.io.File;

import nl.nikhef.jgridstart.util.PasswordCache;

import org.junit.Test;

/** Test basic operations of a {@link CertificateStore} */
public class CertificateStore1Test extends CertificateBaseTest {
    
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
	File f = newTestCertificate(new File(path, "user-cert-foo")).getPath();
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
	newTestCertificate(new File(store.path, "user-cert-0001"));
	store.refresh();
	assertEquals(1, store.size());
    }

    /** Test if refresh picks up removed item */
    @Test
    public void testRefreshRemove() throws Exception {
	File path = newTestStore(0);
	File entry = newTestCertificate(new File(path, "user-cert-bar")).getPath();
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
    
    /** Test PKCS certificate export */
    @Test
    public void testExportImportPKCS() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	dotestExportImport(store, "123".toCharArray(), ".p12");
    }
    /** Test PKCS certificate export with a long password.
     * Breaks when using standard Java JSSE :( */
    @Test
    public void testExportImportPKCSLongPassword() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	dotestExportImport(store, "123ksjldfhljk3342398p4O*(43hlui2#H$LIU%H:OI'opKL:MJ34jK".toCharArray(), ".p12");
    }
    /** Test PEM certificate export */
    @Test
    public void testExportImportPEM() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	char[] pw = PasswordCache.getInstance().getForDecrypt("", store.get(0).getKeyFile().getCanonicalPath());
	dotestExportImport(store, pw, ".pem");
    }
    /** Test PEM certificate export with a long password. */
    @Test
    public void testExportImportPEMLongPassword() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	char[] pw = PasswordCache.getInstance().getForDecrypt("", store.get(0).getKeyFile().getCanonicalPath());
	dotestExportImport(store, pw, ".pem");
    }
    
    protected void dotestExportImport(CertificateStore store, char[] pw, String ext) throws Exception {
	File exported = File.createTempFile("cert", ext);
	try {
	    PasswordCache.getInstance().set(exported.getCanonicalPath(), pw);
	    store.get(0).exportTo(exported);
	    // now import into new store
	    CertificateStore store2 = new CertificateStore(newTestStore(0));
	    store2.importFrom(exported, null, pw);
	    assertEquals(store.get(0), store2.get(0));
	} finally {
	    exported.delete();
	}
    }
}