package nl.nikhef.jgridstart;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import org.junit.Test;

import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
import nl.nikhef.jgridstart.util.FileUtils;
import nl.nikhef.jgridstart.util.PasswordCache;

public class CertificateCheckTest extends CertificateBaseTest {
    
    /** Helper method: get {@link CertificatePair}; not load()ed !!! */
    protected CertificatePair getCert(File f) throws IOException {
	// create CertificatePair
	// use protected methods to workaround built-in CertificateCheck invocation
	CertificatePair cert = new CertificatePair();
	cert.path = f;
	return cert;
    }
    /** Helper method: test a dir that is loaded, optional private decryption (via {@link PasswordCache}).*/
    protected void test(File f, String passw) throws IOException, CertificateCheckException {
	CertificatePair cert = getCert(f);
	CertificateCheck check = new CertificateCheck(cert);
	check.check();
	// private key check if asked for
	if (passw!=null) {
	    PasswordCache.getInstance().set(cert.getKeyFile().getCanonicalPath(), passw.toCharArray());
	    check.checkPrivate();
	}
	// private key check with random password should fail
	final SecureRandom random;
	try {
	    random = SecureRandom.getInstance("SHA1PRNG", "SUN");
	} catch (Exception e) { throw new IOException(e.getLocalizedMessage()); }
	char[] wrongpw = new char[12];
	for (int i=0; i<wrongpw.length; i++) wrongpw[i] = (char)(random.nextInt(128-32)+32);
	PasswordCache.getInstance().set(cert.getKeyFile().getCanonicalPath(), wrongpw);
	try {
	    check.checkPrivate();
	    throw new IOException("Private key check succeeds with random password");
	} catch(CertificateCheckException e) { /* ok */ }
    }
    /** Helper method: test without decrypting private key */
    protected void test(File f) throws IOException, CertificateCheckException {
	test(f, null);
    }
    /** Helper method: test and assume that an CertificateCheckException occurs.*/
    protected void testE(File f, String passw) throws IOException {
	try {
	    test(f, passw);
	} catch(CertificateCheckException e) {
	    return;
	}
	throw new IOException("Expected a CertificateCheckException");
    }
    /** Helper method: test and assume that an CertificateCheckException occurs (no decrypt). */
    protected void testE(File f) throws IOException {
	testE(f, null);
    }
    /** Helper method: replace random bits in PEM file */
    protected void confusePEM(File f) throws IOException {
	byte[] pem = FileUtils.readFile(f).getBytes();
	String sep = System.getProperty("line.separator");
	int startpos = pem.toString().indexOf(sep+sep)+2;
	int endpos = pem.toString().indexOf("-----END");
	for (int i=startpos; i<endpos; i+=18)
	    if (pem[i]>'A' && pem[i]<'z') pem[i]++;
	FileUtils.writeFile(f, pem.toString());
    }

    
    /** Ordinary, correct cert+key+request */
    @Test public void testO_01() throws Exception {
	File tmp = new File(tmpBasePath, "testO01");
	newTestCertificate(tmp, "xyzblupblup9");
	test(tmp, "xyzblupblup9");
    }
    /** Ordinary, correct cert+key */
    @Test public void testO_02() throws Exception {
	File tmp = new File(tmpBasePath, "testO02");
	newTestCertificate(tmp, "xyzblupbl11up9").getCSRFile().delete();
	test(tmp, "xyzblupbl11up9");
    }
    /** Ordinary, correct key+request */
    @Test public void testO_03() throws Exception {
	File tmp = new File(tmpBasePath, "testO03");
	newTestRequest(tmp, "foobar1");
	test(tmp, "foobar1");
    }
    /** Test ordinary cert+key with DSA algorithm */
    @Test public void testO_06() throws Exception {
	setAlgorithm("DSA");
	File tmp = new File(tmpBasePath, "testO06");
	newTestCertificate(tmp, "hJKHhklQW34213AWE");
	setAlgorithm("RSA");
	test(tmp, "hJKHhklQW34213AWE");
    }

    /** Empty directory */
    @Test public void testE_01() throws Exception { 
	File tmp = new File(tmpBasePath, "testE01");
	tmp.mkdir();
	testE(tmp);
    }
    /** Empty private key */
    @Test public void testE_02() throws Exception {
	File tmp = new File(tmpBasePath, "testE02");
	tmp.mkdir();
	FileUtils.writeFile(new File(tmp, "userkey.pem"), "");
	testE(tmp);
    }
    /** Malformed private key; random chars replaced */
    @Test public void testE_04() throws Exception {
	File tmp = new File(tmpBasePath, "testE04");
	confusePEM(newTestRequest(tmp, "oLKJjlk123").getKeyFile());
	testE(tmp, "oLKJjlk123");
    }
    /** Empty certificate */
    @Test public void testE_05() throws Exception {
	File tmp = new File(tmpBasePath, "testE05");
	File cert = newTestRequest(tmp, "99JakLKJ").getCertFile();
	FileUtils.writeFile(cert, "");
	testE(tmp, "99JakLKJ");
    }
    /** Malformed certificate; random chars replaced */
    @Test public void testE_07() throws Exception {
	File tmp = new File(tmpBasePath, "testE07");
	confusePEM(newTestCertificate(tmp, "hHJKkj(*Ykh3l214").getCertFile());
	testE(tmp, "hHJKkj(*Ykh3l214");
    }
    /** Key/certificate mismatch, both RSA */
    @Test public void testE_08() throws Exception {
	File tmp1 = new File(tmpBasePath, "testE08-1");
	File tmp2 = new File(tmpBasePath, "testE08-2");
	File cert1 = newTestCertificate(tmp1, "yayayayayay").getCertFile();
	File cert2 = newTestCertificate(tmp2, "yayayayayay").getCertFile();
	cert2.delete(); // for windows
	cert1.renameTo(cert2);
	testE(tmp2, "yayayayayay");
    }
    /** Key/certificate mismatch, key DSA, cert RSA */
    @Test public void testE_09() throws Exception {
	File tmp1 = new File(tmpBasePath, "testE09-1");
	File tmp2 = new File(tmpBasePath, "testE09-2");
	setAlgorithm("DSA");
	File cert1 = newTestCertificate(tmp1, "yayayayayay2").getCertFile();
	setAlgorithm("RSA");
	File cert2 = newTestCertificate(tmp2, "yayayayayay2").getCertFile();
	cert1.delete();
	cert2.renameTo(cert1);
	testE(tmp1, "yayayayayay2");
    }
    /** Key/certificate mismatch, key RSA, cert DSA */
    @Test public void testE_10() throws Exception {
	File tmp1 = new File(tmpBasePath, "testE10-1");
	File tmp2 = new File(tmpBasePath, "testE10-2");
	setAlgorithm("DSA");
	File cert1 = newTestCertificate(tmp1, "yayayayayay3").getCertFile();
	setAlgorithm("RSA");
	File cert2 = newTestCertificate(tmp2, "yayayayayay3").getCertFile();
	cert2.delete();
	cert1.renameTo(cert2);
	testE(tmp2, "yayayayayay3");
    }
    /** Key/certificate mismatch, both DSA */
    @Test public void testE_11() throws Exception {
	File tmp1 = new File(tmpBasePath, "testE11-1");
	File tmp2 = new File(tmpBasePath, "testE11-2");
	setAlgorithm("DSA");
	File cert1 = newTestCertificate(tmp1, "huphup12839218").getCertFile();
	File cert2 = newTestCertificate(tmp2, "huphup12839218").getCertFile();
	cert2.delete();
	cert1.renameTo(cert2);
	setAlgorithm("RSA");
	testE(tmp2, "huphup12839218");
    }
    /** Only key. */
    @Test public void testE_12() throws Exception {
	File tmp = new File(tmpBasePath, "testO01");
	newTestRequest(tmp, "sljklkLJKwe").getCSRFile().delete();
	testE(tmp, "sljklkLJKwe");
    }
}
