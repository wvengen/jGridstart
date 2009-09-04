package nl.nikhef.jgridstart;

import java.io.File;
import java.util.Iterator;

import nl.nikhef.jgridstart.util.FileUtils;

import org.junit.Test;

public class CertificateStoreWithDefaultTest extends CertificateStoreBaseTest {

    /** Create new store with one entry, copy files, check if is seen as default */
    @Test
    public void testCompareDefaultCertificate() throws Exception {
	File storePath = newTestStore(0);
	File childPath = addCopyTest("testO-05", storePath);
	FileUtils.CopyFiles(FileUtils.listFilesOnly(childPath), storePath);
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(storePath);
	assertTrue(store.compareDefaultCertificate(store.get(0)));
	// removal of certificate should still see it as default
	new File(storePath, "usercert.pem").delete();
	assertTrue(store.compareDefaultCertificate(store.get(0)));
    }
    /** Create new store with one entry, copy files, remove key, check if not seen as default */
    @Test
    public void testCompareDefaultCertificateNotEqual() throws Exception {
	File storePath = newTestStore(0);
	File childPath = addCopyTest("testO-05", storePath);
	FileUtils.CopyFiles(FileUtils.listFilesOnly(childPath), storePath);
	new File(storePath, "userkey.pem").delete();
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(storePath);
	assertFalse(store.compareDefaultCertificate(store.get(0)));
    }
    /** Create new store with two entries, set default, check other is not default */
    @Test
    public void testCompareDefaultCertificateOther() throws Exception {
	File storePath = newTestStore(0);
	File childPath1 = addCopyTest("testO-05", storePath);
	File childPath2 = addCopyTest("testO-02", storePath);
	FileUtils.CopyFiles(FileUtils.listFilesOnly(childPath1), storePath);
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(storePath);
	CertificatePair cert = null;
	for (Iterator<CertificatePair> it = store.iterator(); it.hasNext(); ) {
	    CertificatePair thisCert = it.next();
	    if (thisCert.getPath().equals(childPath2))
		cert = thisCert;
	}
	assertNotNull(cert);
	assertFalse(store.compareDefaultCertificate(cert));
    }
    // TODO setDefault tests
}