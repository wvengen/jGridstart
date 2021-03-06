package nl.nikhef.jgridstart.passwordcache;

import java.util.Arrays;
import org.bouncycastle.openssl.PasswordFinder;
import junit.framework.TestCase;

public class PasswordCacheTest extends TestCase {
    
    private PasswordCache cache;
    private int oldUI;
    private int oldTimeout;
    
    @Override
    public void setUp() {
	cache = PasswordCache.getInstance();
	oldUI = cache.getUI();
	cache.setUI(PasswordCache.UI_NONE);
	oldTimeout = cache.getTimeout();
    }
    
    @Override
    public void tearDown() {
	cache.setUI(oldUI);
	cache.setTimeout(oldTimeout);
    }
    
    public void testGetInstance() {
	assertEquals(cache, PasswordCache.getInstance());
    }

    public void testSet() throws PasswordCancelledException {
	cache.clear();
	char[] pw = "Xyz123zyX".toCharArray();
	cache.set("foo", pw);
	// check this entry
	assertNotNull(cache.getForDecrypt("", "foo"));
	assertTrue(Arrays.equals(cache.getForDecrypt("", "foo"), pw));
	// make sure another entry isn't set
	assertNull(cache.getForDecrypt("", "bar"));
    }

    public void testInvalidate() throws PasswordCancelledException {
	cache.clear();
	cache.set("blah", "test".toCharArray());
	assertNotNull(cache.getForDecrypt("", "blah"));
	cache.invalidate("blah");
	assertNull(cache.getForDecrypt("", "blah"));
    }
    
    public void testClear() throws PasswordCancelledException {
	cache.set("foobar", "faosdifj".toCharArray());
	cache.set("barfoo", "asduiofs".toCharArray());
	assertNotNull(cache.getForDecrypt("", "foobar"));
	assertNotNull(cache.getForDecrypt("", "barfoo"));
	cache.clear();
	assertNull(cache.getForDecrypt("", "foobar"));
	assertNull(cache.getForDecrypt("", "barfoo"));
    }

    public void testGetDecryptPasswordFinder() {
	cache.clear();
	char[] pw = "aoksdJLKASjkl".toCharArray();
	cache.set("bar", pw);
	PasswordFinder f = cache.getDecryptPasswordFinder("", "bar");
	assertTrue(Arrays.equals(f.getPassword(), pw));
    }
    
    public void testTimeout() throws InterruptedException, PasswordCancelledException {
	int timeout = 1;
	cache.clear();
	cache.setTimeout(timeout);
	cache.set("blah", "test".toCharArray());
	assertNotNull(cache.getForDecrypt("", "blah"));
	Thread.sleep((timeout+1)*1000);
	assertNull(cache.getForDecrypt("", "blah"));
    }

}
