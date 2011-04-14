package nl.nikhef.jgridstart;

import java.util.Properties;
import org.junit.Test;

/** High-level {@link CertificateStore} test cases */
public class CertificateStore2Test extends CertificateBaseTest {
    
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
