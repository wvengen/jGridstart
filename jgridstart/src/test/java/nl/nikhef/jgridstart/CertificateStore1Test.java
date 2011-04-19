package nl.nikhef.jgridstart;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import nl.nikhef.jgridstart.osutils.FileUtils;
import nl.nikhef.jgridstart.osutils.PKCS12KeyStoreUnlimited;
import nl.nikhef.jgridstart.passwordcache.PasswordCache;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
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
	FileUtils.recursiveDelete(entry);
	store.refresh();
	assertEquals(0, store.size());
    }

    /** Test if refresh picks up certificate state change */
    @Test
    public void testRefreshState() throws Exception {
	System.setProperty("jgridstart.ca.local.hold", "true");
	CertificateStore store = new CertificateStore(newTestStore(0));
	assertEquals(0, store.size());
	CertificatePair cert = newTestRequest(new File(store.path, "user-cert-0001"));
	assertNull(cert.getCertificate());
	store.refresh();
	assertEquals(1, store.size());
	assertFalse(cert.isCertificationRequestProcessed());
	System.setProperty("jgridstart.ca.local.hold", "false");
	store.refresh();
	assertTrue(cert.isCertificationRequestProcessed());
	System.clearProperty("jgridstart.ca.local.hold");
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
    
    /** Test import from directory */
    @Test
    public void testImportDirectory() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	CertificateStore store2 = new CertificateStore(newTestStore(0));
	store2.importFrom(store.get(0).getPath());
	assertEquals(store.get(0), store2.get(0));
    }
    /** Test import from directory with other password */
    @Test
    public void testImportDirectoryPassword() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	CertificateStore store2 = new CertificateStore(newTestStore(0));
	store2.importFrom(store.get(0).getPath(), "foobar".toCharArray());
	assertEquals(store.get(0), store2.get(0));
    }
    /** Test import from PEM file which should use directory instead */
    @Test
    public void testImportDirectoryPEMkey() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	CertificateStore store2 = new CertificateStore(newTestStore(0));
	store2.importFrom(store.get(0).getKeyFile());
	assertEquals(store.get(0), store2.get(0));
    }
    /** Test import from PEM file which should use directory instead */
    @Test
    public void testImportDirectoryPEMcert() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	CertificateStore store2 = new CertificateStore(newTestStore(0));
	store2.importFrom(store.get(0).getCertFile());
	assertEquals(store.get(0), store2.get(0));
    }
    /** Test import from PEM file which should use directory instead */
    @Test
    public void testImportDirectoryPEMreq() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	CertificateStore store2 = new CertificateStore(newTestStore(0));
	store2.importFrom(store.get(0).getCSRFile());
	assertEquals(store.get(0), store2.get(0));
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
	dotestExportImport(store, "123".toCharArray(), ".pem");
    }
    /** Test PEM certificate export with a long password. */
    @Test
    public void testExportImportPEMLongPassword() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	dotestExportImport(store, "kljLKHJ2349oy234789yHOhik234hlkjUP*(324hlkj#KL".toCharArray(), ".pem");
    }
    
    protected void dotestExportImport(CertificateStore store, char[] pw, String ext) throws Exception {
	File exported = File.createTempFile("cert", ext);
	try {
	    store.get(0).exportTo(exported, pw);
	    // now import into new store
	    CertificateStore store2 = new CertificateStore(newTestStore(0));
	    PasswordCache.getInstance().set(exported.getCanonicalPath(), pw);
	    store2.importFrom(exported, "LKAJHlkjhH(".toCharArray());
	    assertEquals(store.get(0), store2.get(0));
	} finally {
	    exported.delete();
	}
    }
    
    /** Test PKCS#12 certificate which has a certificate chain as well */
    @Test
    public void testImportCertificateChain() throws Exception {
	CertificateStore certstore = new CertificateStore(newTestStore(1));
	CertificatePair cert = certstore.get(0);
	char[] pw = "one2t".toCharArray();
	File dst = File.createTempFile("certexport", ".p12", cert.getPath());
	
	// Create certificate chain
	X509Certificate[] certchain = {cert.getCertificate(), cert.getCA().getCACertificate()};
	
	// Create PKCS12 keystore with password
	KeyStore store = PKCS12KeyStoreUnlimited.getInstance();
	store.load(null, null);
	store.setKeyEntry("Grid certificate", cert.getPrivateKey(), null, certchain);
	
	// write file with password
	FileOutputStream out = new FileOutputStream(dst);
	store.store(out, pw);
	out.close();
	
	// now try to import
	CertificateStore certstore2 = new CertificateStore(newTestStore(0));
	PasswordCache.getInstance().set(dst.getCanonicalPath(), pw);
	CertificatePair cert2 = certstore2.importFrom(dst, null);
	assertEquals(cert2, cert);
    }
    
    /** Verify related certificate files are found properly */
    @Test
    public void testRelatedFiles() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	// key, request, cert, userinfo
	store.get(0).store(); // to make sure userinfo is there
	assertEquals(4, store.get(0).getRelatedFiles().length);
	// make sure other files are not counted
	FileOutputStream out = new FileOutputStream(new File(store.get(0).getPath(), "dummy.dat"));
	out.write(0);
	out.close();
	assertEquals(4, store.get(0).getRelatedFiles().length);
	// now remove some files and check again
	store.get(0).getCertFile().delete();
	assertEquals(3, store.get(0).getRelatedFiles().length);
	store.get(0).getCSRFile().delete();
	assertEquals(2, store.get(0).getRelatedFiles().length);
    }
    
    /** Verify additional files are not deleted */
    @Test
    public void testRelatedFilesNotDeleted() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(2));
	File[] paths = {store.get(0).getPath(), store.get(1).getPath()};
	FileOutputStream out = new FileOutputStream(new File(store.get(0).getPath(), "foo.beh"));
	out.write(0);
	out.close();
	// try to delete, should delete directory
	store.delete(1);
	assertFalse(paths[1].exists());
	// try to delete, should not delete directory
	store.delete(0);
	assertTrue(paths[0].exists());
    }
    
    /** Verify that subject is a PRINTABLESTRING (and not UTF8STRING).
     * <p>
     * This is required for some grid software to work properly (mkproxy).
     */
    @Test
    public void testPrintablestring() throws Exception {
	CertificateStore store = new CertificateStore(newTestStore(1));
	CertificatePair cert = store.get(0);
	assertNotNull(cert);
	X509Certificate x509 = cert.getCertificate();
	assertNotNull(x509);
	X509Principal subject = PrincipalUtil.getSubjectX509Principal(x509);
	verifyNoUtf8(subject.getDERObject());
    }
    
    /** Helper function: make sure ASN1 object contains no DERUTF8STRING,
     * either itself or one of its children. */
    protected void verifyNoUtf8(DERObject o) {
	assertFalse(o instanceof DERUTF8String);
	if (o instanceof ASN1Sequence) {
	    ASN1Sequence seq = (ASN1Sequence)o;
	    for (int i=0; i<seq.size(); i++)
		verifyNoUtf8(seq.getObjectAt(i).getDERObject());
	} else if (o instanceof ASN1Set) {
	    ASN1Set seq = (ASN1Set)o;
	    for (int i=0; i<seq.size(); i++)
		verifyNoUtf8(seq.getObjectAt(i).getDERObject());
	}
    }
    
}
