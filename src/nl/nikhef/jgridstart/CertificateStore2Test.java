package nl.nikhef.jgridstart;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import nl.nikhef.jgridstart.util.FileUtils;

import org.junit.Test;

import junit.framework.TestCase;

/** High-level {@link CertificateStore} test cases */
public class CertificateStore2Test extends TestCase {
    
    protected File tmpBasePath = null;
    private String oldCAProvider = null;

    @Override
    public void setUp() throws Exception {
	// create temp dir to work in
	tmpBasePath = FileUtils.createTempDir("test-certificatestore");
	// use LocalCA for testing
	oldCAProvider = System.getProperty("jgridstart.ca.provider");
	System.setProperty("jgridstart.ca.provider", "LocalCA");
    }
    
    @Override
    public void tearDown() throws Exception {
	// remove temp dir
	CertificateStore1Test.recursiveDelete(tmpBasePath);
	// restore CA
	if (oldCAProvider==null)
	    System.getProperties().remove("jgridstart.ca.provider");
	else
	    System.setProperty("jgridstart.ca.provider", oldCAProvider);
    }
    
    /** @see CertificateStore1Test#newTestStore */
    protected File newTestStore(int num) throws IOException {
	File path = FileUtils.createTempDir("test-store", tmpBasePath);
	for (int i=1; i<=num; i++) {
	    addCopyTest("testO-0"+i, path);
	}
	return path;
    }

    /** @see CertificateStore1Test#addCopyTest */
    protected static File addCopyTest(String name, File store) throws IOException {
	return CertificateStore1Test.addCopyTest(name, store);
    }

    /** Simple new request run */
    @Test
    public void testRequest01() throws Exception {
	// start with empty store
	CertificateStore store = new CertificateStore(newTestStore(0));
	// generate request
	Properties p = new Properties();
	p.setProperty("subject", "/O=dutchgrid/O=users/O=nikhef/CN=John Doe");
	p.setProperty("email", "john.doe@example.com");
	CertificatePair cert = store.generateRequest(p, "abcdefg__1234567".toCharArray());
	assertEquals(1, store.size());
	assertNotNull(cert);
	assertNotNull(cert.getPrivateKey());
	assertNotNull(cert.getCSR());
	assertNull(cert.getCertificate());
	assertFalse(Boolean.valueOf(cert.getProperty("request.submitted")));
	assertEquals("nikhef", cert.getProperty("org").toLowerCase());
	// "upload" certificate
	cert.uploadRequest();
	assertTrue(Boolean.valueOf(cert.getProperty("request.submitted")));
	// download certificate (is directly available from LocalCA)
	assertTrue(cert.isCertificationRequestProcessed());
	cert.downloadCertificate();
	assertNotNull(cert.getCertificate());
	assertTrue(Boolean.valueOf(cert.getProperty("valid")));
    }

    // TODO import/export from several PEM, PKCS, path good and bad tests; maybe separate class


}
