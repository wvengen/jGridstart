package nl.nikhef.jgridstart.ca;

import java.util.HashMap;

/** Get or create {@link CA} implementation.
 * <p>
 * This currently returns a singleton instance of the CA specified
 * in the global Java properties named {@code jgridstart.ca.provider}.
 * 
 * @author wvengen
 *
 */
public class CAFactory {
    
    /** Instances of CAs */
    private static HashMap<String, CA> CAs = new HashMap<String, CA>();
    /** The default CA */
    private static CA defaultCA = null;
    
    /** Returns the default CA implementation, or {@code null} if not found.
     * <p>
     * @see #getCA
     */
    public static CA getDefault() throws CAException {
	if (defaultCA==null) {
	    defaultCA = getCA(System.getProperty("jgridstart.ca.provider"));
	}
	return defaultCA;
    }
    
    /** Returns a CA by name.
     * <p>
     * The name can be either a fully qualified Java class name, or a classname
     * without package name as present in the {@code nl.nikhef.jgridstart.ca}
     * package.
     * <p>
     * @throws CAException when CA could not be instantiated
     */
    public static CA getCA(String name) throws CAException {
	// get cached instance
	if (CAs.containsKey(name))
	    return CAs.get(name);
	// or create and store
	CA ca = null;
    	try {
	    ca = (CA)Class.forName(name).newInstance();
	} catch (Exception e) {
	    throw new CAException("Invalid CA in configuration:\n"+e.getLocalizedMessage());
	}
	CAs.put(name, ca);
	return ca;
    }
}
