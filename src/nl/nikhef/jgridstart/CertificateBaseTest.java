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
    /** original {@linkplain PasswordCache} password timeout */
    private int pwcacheOldTimeout = -1;
    /** original {@linkplain PasswordCache} user-interface provider */
    int pwcacheOldUI = -1;
    /** original crypto configuration */
    String[] oldCryptoConf = null;
    
    @Override
    public void setUp() throws Exception {
	// create temp dir to work in
	tmpBasePath = FileUtils.createTempDir("testcerts");
	// use LocalCA for testing
	oldCAProvider = System.getProperty("jgridstart.ca.provider");
	System.setProperty("jgridstart.ca.provider", "LocalCA");
	// don't ask passwords for testing
	pwcacheOldTimeout = PasswordCache.getInstance().getTimeout(); 
	PasswordCache.getInstance().setTimeout(-1);
	pwcacheAlwaysEncrypt = PasswordCache.getInstance().setAlwaysAskForEncrypt(false);
	pwcacheOldUI = PasswordCache.getInstance().getUI();
	PasswordCache.getInstance().setUI(PasswordCache.UI_NONE);
	// store crypto configuration
	oldCryptoConf = new String[] {
	    System.getProperty("jgridstart.keyalgname"),
	    System.getProperty("jgridstart.keysize"),
	    System.getProperty("jgridstart.sigalgname")
	};
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
	PasswordCache.getInstance().setTimeout(pwcacheOldTimeout);
	PasswordCache.getInstance().setAlwaysAskForEncrypt(pwcacheAlwaysEncrypt);
	PasswordCache.getInstance().setUI(pwcacheOldUI);
	// restore crypto configuration
	try { System.setProperty("jgridstart.keyalgname", oldCryptoConf[0]); } catch(NullPointerException e) { }
	try { System.setProperty("jgridstart.keysize", oldCryptoConf[1]); } catch(NullPointerException e) { }
	try { System.setProperty("jgridstart.sigalgname", oldCryptoConf[2]); } catch(NullPointerException e) { }
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
     * @param pw password for new private key
     * @return newly created {@linkplain CertificatePair}
     */
    protected CertificatePair newTestRequest(File certPath, String pw) throws PasswordCancelledException, IOException, GeneralSecurityException, CAException {
	if (pw==null)
	    pw = "test_password-"+certIndex;
	Properties p = new Properties();
	p.setProperty("subject", "/O=dutchgrid/O=users/O=nikhef/CN=Test User #" + certIndex++);
	certPath.mkdirs();
	CertificatePair cert = CertificatePair.generateRequest(certPath, p, pw.toCharArray());
	assertNotNull(cert);
	assertNotNull(cert.getPrivateKey());
	assertNotNull(cert.getCSR());
	assertNull(cert.getCertificate());
	cert.uploadRequest();
	return cert;
    
    }
    /** Generate test request, generate password.
     *
     *  @see #newTestRequest(File, String)
     */
    protected CertificatePair newTestRequest(File certPath) throws PasswordCancelledException, IOException, GeneralSecurityException, CAException {
	return newTestRequest(certPath, null);
    }
    
    /** Set the certificate algorithm for new certificates.
     * 
     * @param alg Algorithm, either "RSA" or "DSA"
     */
    protected void setAlgorithm(String alg) throws GeneralSecurityException {
	if (alg=="RSA") {
	    System.setProperty("jgridstart.keyalgname", "RSA");
	    System.setProperty("jgridstart.keysize", "1024");
	    System.setProperty("jgridstart.sigalgname", "SHA1WithRSA");
	} else if (alg=="DSA") {
	    System.setProperty("jgridstart.keyalgname", "DSA");
	    System.setProperty("jgridstart.keysize", "1024");
	    System.setProperty("jgridstart.sigalgname", "SHA1WithDSA");
	} else {
	    throw new GeneralSecurityException("Invalid algorithm requested: "+alg);
	}
    }

    /** Generate test certificate.
     * <p>
     * New key and CSR are generated and the certificate is signed using {@link LocalCA}. 
     * 
     * @param certPath
     * @return newly created {@linkplain CertificatePair}
     */
    protected CertificatePair newTestCertificate(File certPath, String pw) throws GeneralSecurityException, PasswordCancelledException, IOException, CAException, CertificateCheckException {
	CertificatePair cert = newTestRequest(certPath, pw);
	cert.downloadCertificate();
	assertNotNull(cert.getCertificate());
	return cert;
    }
    /** Generate test certificate, generate password.
    *
    *  @see #newTestCertificate(File, String)
    */
   protected CertificatePair newTestCertificate(File certPath) throws GeneralSecurityException, PasswordCancelledException, IOException, CAException, CertificateCheckException {
	return newTestCertificate(certPath, null);
   }
}
