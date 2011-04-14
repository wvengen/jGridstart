package nl.nikhef.browsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;
import org.junit.Test;

/** Mac OS X tests, parsing-tests can be run on any platform. */
public class BrowsersMacOSXTest extends TestCase {
    
    /** Helper method: get contents of a resource */
    protected String getResourceAsString(String name) throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	InputStream in = getClass().getResourceAsStream(name);
	int len;
	while ( (len=in.available()) > 0 ) {
	    if (len>1024) len=1024;
	    byte[] datb = new byte[len];
	    in.read(datb);
	    out.write(datb);
	}
	in.close();
	return new String(out.toByteArray());
    }
    
    /** Helper method: check browser is ok in output of lsregister dump */
    protected void assertBrowserOk(String uti, ArrayList<Properties> p) throws Exception {
	// find the browser's discovered properties
	Properties pi = null;
	uti = uti.toLowerCase();
	for (Iterator<Properties> it = p.iterator(); it.hasNext(); ) {
	    pi = it.next();
	    if (uti.equals(pi.getProperty("uti")))
		break;
	}
	if (pi==null ||!uti.equals(pi.getProperty("uti")))
	    throw new Exception("Browser not found: "+uti);
	// make sure details are ok; very crude right now
	assertNotNull(pi.getProperty("desc"));
    }
    

    /** Test parsing of real-world lsregister dump */
    @Test
    public void testParseRegister1() throws Exception {
	String data = getResourceAsString("test.lsregister0001.dump");
	ArrayList<Properties> p = BrowsersMacOSX.parseSystemBrowsers(data);
	assertEquals(2, p.size());
	assertBrowserOk("org.mozilla.firefox", p);
	assertBrowserOk("com.apple.safari", p);
    }
    /** Test parsing of real-world lsregister dump */
    @Test
    public void testParseRegister2() throws Exception {
	String data = getResourceAsString("test.lsregister0002.dump");
	ArrayList<Properties> p = BrowsersMacOSX.parseSystemBrowsers(data);
	assertEquals(3, p.size());
	assertBrowserOk("org.mozilla.firefox", p);
	assertBrowserOk("org.mozilla.camino", p);
	assertBrowserOk("com.apple.safari", p);
	// no com.RealNetworks.RealPlayer since it is filtered out as spam
    }
}
