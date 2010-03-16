package nl.nikhef.jgridstart;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

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
	for (CertificatePair thisCert: store) {
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
	for (CertificatePair thisCert: store) {
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
    
    /** Scenario test: <em>Single</em> */
    @Test
    public CertificateStoreWithDefault testScenarioSingle() throws Exception {
	// new store
	CertificateStoreWithDefault store = new CertificateStoreWithDefault(FileUtils.createTempDir("certstore", tmpBasePath));
	assertEquals(0, store.size());
	// generate new request
	Properties p = new Properties();
	p.setProperty("subject", "/O=dutchgrid/O=users/O=nikhef/O=Scenario Single");
	CertificatePair cert = store.generateRequest(p, "footest".toCharArray());
	// make sure output is ok and is default cert
	assertEquals(cert, store.getDefault());
	File[] files = store.path.listFiles();
	for (int i=0; i < files.length; i++)
	    assertFalse(files[i].isDirectory());
	cert.uploadRequest();
	cert.downloadCertificate();
	assertEquals(cert, store.getDefault());
	return store;
    }
    
    /** Scenario test: <em>Single renewal</em> */
    @Test
    public void testScenarioSingleRenewal() throws Exception {
	CertificateStoreWithDefault store = testScenarioSingle();
	CertificatePair oldCert = store.get(0);
	assertEquals(oldCert, store.getDefault());
	CertificatePair newCert = store.generateRenewal(oldCert, "footest2".toCharArray());
	// should become default only after certificate is downloaded
	assertEquals(oldCert, store.getDefault());
	newCert.uploadRequest();
	assertEquals(oldCert, store.getDefault());
	newCert.downloadCertificate();
	assertEquals(newCert, store.getDefault());
    }
    
    /** Scenario test: <em>Dual</em> */
    @Test
    public void testScenarioMultiple() throws Exception {
	CertificateStoreWithDefault store = testScenarioSingle();
	assertEquals(1, store.size());
	// generate new request
	Properties p = new Properties();
	p.setProperty("subject", "/O=dutchgrid/O=users/O=cwi/O=Scenario Dual");
	CertificatePair cert = store.generateRequest(p, "footestdual".toCharArray());
	// make sure output is ok and is not default cert
	assertFalse(cert == store.getDefault());
	cert.uploadRequest();
	assertFalse(cert == store.getDefault());
	cert.downloadCertificate();
	assertFalse(cert == store.getDefault());
    }
    
    // TODO more setDefault tests; at least also with new instance of CertificateStoreWithDefault
}