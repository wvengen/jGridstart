package nl.nikhef.jgridstart;

import java.io.File;
import java.util.Iterator;

import nl.nikhef.jgridstart.util.FileUtils;

import org.junit.Test;

public class CertificateStoreWithDefaultTest extends CertificateBaseTest {
    
    /** Test if single default cert is recognised */
    @Test
    public void testCreateDefaultStore() throws Exception {
	File storePath = newTestStore(0);
	File childPath = newTestCertificate(new File(storePath, "user-cert-0000")).getPath();
	FileUtils.MoveFiles(FileUtils.listFilesOnly(childPath), storePath);
	assertTrue(childPath.delete());
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(storePath);
	assertEquals(1, store.size());
	assertEquals(storePath, store.getDefault().getPath());
    }

    /** Create new store with one entry, copy files, check if is seen as default */
    @Test
    public void testCompareDefaultCertificate() throws Exception {
	File storePath = newTestStore(0);
	File childPath = newTestCertificate(new File(storePath, "user-cert-0000")).getPath();
	FileUtils.CopyFiles(FileUtils.listFilesOnly(childPath), storePath);
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(storePath);
	assertEquals(1, store.size());
	assertTrue(store.compareDefaultCertificate(store.get(0)));
	// removal of certificate should still see it as default
	new File(storePath, "usercert.pem").delete();
	assertTrue(store.compareDefaultCertificate(store.get(0)));
    }
    
    /** Create new store with one entry, copy files, remove key, check if not seen as default */
    @Test
    public void testCompareDefaultCertificateNotEqual() throws Exception {
	File storePath = newTestStore(0);
	File childPath = newTestCertificate(new File(storePath, "user-cert-0000")).getPath();
	FileUtils.CopyFiles(FileUtils.listFilesOnly(childPath), storePath);
	new File(storePath, "userkey.pem").delete();
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(storePath);
	assertEquals(1, store.size());
	assertFalse(store.compareDefaultCertificate(store.get(0)));
    }
    
    /** Create new store with two entries, set default, check other is not default */
    @Test
    public void testCompareDefaultCertificateOther() throws Exception {
	File storePath = newTestStore(0);
	File childPath1 = newTestCertificate(new File(storePath, "user-cert-0000")).getPath();
	File childPath2 = newTestCertificate(new File(storePath, "user-cert-0001")).getPath();
	FileUtils.CopyFiles(FileUtils.listFilesOnly(childPath1), storePath);
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(storePath);
	assertEquals(2, store.size());
	CertificatePair cert1 = null, cert2 = null;
	for (Iterator<CertificatePair> it = store.iterator(); it.hasNext(); ) {
	    CertificatePair thisCert = it.next();
	    if (thisCert.getPath().equals(childPath1)) cert1 = thisCert;
	    if (thisCert.getPath().equals(childPath2)) cert2 = thisCert;
	}
	assertNotNull(cert1);
	assertTrue(store.compareDefaultCertificate(cert1));
	assertNotNull(cert2);
	assertFalse(store.compareDefaultCertificate(cert2));
    }
    
    /** Create new store with three entries, set defaults, check results */
    @Test
    public void testSetDefault() throws Exception {
	File storePath = newTestStore(0);
	File childPath1 = newTestCertificate(new File(storePath, "user-cert-0001")).getPath();
	File childPath2 = newTestCertificate(new File(storePath, "user-cert-0002")).getPath();
	File childPath3 = newTestCertificate(new File(storePath, "user-cert-0003")).getPath();
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(storePath);
	assertEquals(3, store.size());
	CertificatePair cert1 = null, cert2 = null, cert3 = null;
	for (Iterator<CertificatePair> it = store.iterator(); it.hasNext(); ) {
	    CertificatePair thisCert = it.next();
	    if (thisCert.getPath().equals(childPath1)) cert1 = thisCert;
	    if (thisCert.getPath().equals(childPath2)) cert2 = thisCert;
	    if (thisCert.getPath().equals(childPath3)) cert3 = thisCert;
	}
	assertNull(store.getDefault());
	
	store.setDefault(cert1);
	assertTrue(store.compareDefaultCertificate(cert1));
	assertEquals(cert1, store.getDefault());
	store.setDefault(cert1);
	assertTrue(store.compareDefaultCertificate(cert1));
	assertEquals(cert1, store.getDefault());

	store.setDefault(cert3);
	assertTrue(store.compareDefaultCertificate(cert3));
	assertEquals(cert3, store.getDefault());
	store.setDefault(cert2);
	assertTrue(store.compareDefaultCertificate(cert2));
	assertEquals(cert2, store.getDefault());

	store.setDefault(cert1);
	assertTrue(store.compareDefaultCertificate(cert1));
	assertEquals(cert1, store.getDefault());
    }
    
    // TODO more setDefault tests; at least also with new instance of CertificateStoreWithDefault
}