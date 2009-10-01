package nl.nikhef.jgridstart;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
import nl.nikhef.jgridstart.ca.CAException;
import nl.nikhef.jgridstart.ca.LocalCA;
import nl.nikhef.jgridstart.util.FileUtils;
import nl.nikhef.jgridstart.util.PasswordCache;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
import junit.framework.TestCase;

/** Base class for {@link CertificatePair} and {@link CertificateStore} tests */
public abstract class CertificateBaseTest extends TestCase {
    
    /** temporary path for certificate directories, cleaned up on {@linkplain #tearDown} */
    protected File tmpBasePath = null;
    /** original CA provider instead of LocalCA; done to be able to run tests inside
     * the program without affecting its configuration */
    private String oldCAProvider = null;
    /** original {@linkplain PasswordCache} status if always want to ask encrypt password */
    private boolean pwcacheAlwaysEncrypt = true;
    
    @Override
    public void setUp() throws Exception {
	// create temp dir to work in
	tmpBasePath = FileUtils.createTempDir("testcerts");
	// use LocalCA for testing
	oldCAProvider = System.getProperty("jgridstart.ca.provider");
	System.setProperty("jgridstart.ca.provider", "LocalCA");
	// don't ask passwords for testing
	pwcacheAlwaysEncrypt = PasswordCache.getInstance().setAlwaysAskForEncrypt(false);
    }
    
    @Override
    public void tearDown() throws Exception {
	// remove temp dir
	FileUtils.recursiveDelete(tmpBasePath);
	// restore CA
	if (oldCAProvider==null)
	    System.getProperties().remove("jgridstart.ca.provider");
	else
	    System.setProperty("jgridstart.ca.provider", oldCAProvider);
	// restore passwordcache settings
	PasswordCache.getInstance().setAlwaysAskForEncrypt(pwcacheAlwaysEncrypt);
    }
    
    /** Helper method: create new certificate store directory in temp space.
     * <p>
     * This is cleaned up automatically by {@link #tearDown}.
     * 
     * @param num number of certificates to put in
     * @throws CertificateCheckException 
     * @throws CAException 
     * @throws GeneralSecurityException 
     * @throws IllegalStateException 
     */
    protected File newTestStore(int num) throws IOException, GeneralSecurityException, CAException, CertificateCheckException {
	File path = FileUtils.createTempDir("test-store", tmpBasePath);
	for (int i=1; i<=num; i++) {
	    newTestCertificate(new File(path, "user-cert-000"+i));
	}
	return path;
    }
    
    /** certificate index, used to issue new test certificates */
    private static int certIndex = 0;
    
    /** Generate test request.
     * <p>
     * New key and CSR are generated, and the latter is uploaded using {@link LocalCA}.
     * 
     * @param certPath
     * @return newly created {@linkplain CertificatePair}
     */
    protected CertificatePair newTestRequest(File certPath) throws PasswordCancelledException, IOException, GeneralSecurityException, CAException {
	char[] pw = ("test_password-"+certIndex).toCharArray();
	Properties p = new Properties();
	p.setProperty("subject", "/O=dutchgrid/O=users/O=nikhef/CN=Test User #" + certIndex++);
	certPath.mkdirs();
	CertificatePair cert = CertificatePair.generateRequest(certPath, p, pw);
	assertNotNull(cert);
	assertNotNull(cert.getPrivateKey());
	assertNotNull(cert.getCSR());
	assertNull(cert.getCertificate());
	cert.uploadRequest();
	return cert;
    }
    
    /** Generate test certificate.
     * <p>
     * New key and CSR are generated and the certificate is signed using {@link LocalCA}. 
     * 
     * @param certPath
     * @return newly created {@linkplain CertificatePair}
     */
    protected CertificatePair newTestCertificate(File certPath) throws GeneralSecurityException, PasswordCancelledException, IOException, CAException, CertificateCheckException {
	CertificatePair cert = newTestRequest(certPath);
	cert.downloadCertificate();
	assertNotNull(cert.getCertificate());
	return cert;
    }
}
