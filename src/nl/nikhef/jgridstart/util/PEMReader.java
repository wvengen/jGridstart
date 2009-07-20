package nl.nikhef.jgridstart.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;

/** PEM file reader that works directory on files, integrated with {@link PasswordCache}. */
public class PEMReader extends org.bouncycastle.openssl.PEMReader {
    
    /** File, if any (for {@linkplain PasswordCache} invalidation on error) */
    private File in = null;
    
    /** Create PEMReader for a File */
    public PEMReader(File in) throws FileNotFoundException {
	super(new FileReader(in));
	this.in = in;
    }
    
    /** Create PEMReader for an encrypted File */
    public PEMReader(File in, String msg) throws FileNotFoundException, IOException {
	super(new FileReader(in), PasswordCache.getInstance().getDecryptPasswordFinder(msg, in.getCanonicalPath()));
	this.in = in;
    }
    
    /** Return single object from PEM file.
     * <p>
     * The first object will be returned, others are discarded.
     */
    public static Object readObject(File in) throws FileNotFoundException, IOException {
	PEMReader r = new PEMReader(in);
	Object o = r.readObject();
	r.close();
	return o;
    }
    
    /** {@inheritDoc}
     * <p>
     * This method also invalidates the password when an {@linkplain IOException} occurs
     * and throws a {@link PasswordCancelledException} when the user cancelled the
     * password entry.
     */
    @Override
    public Object readObject() throws IOException {
	try {
	    return super.readObject();
	} catch(IOException e) {
	    if (in!=null) {
		// if no password finder was supplied, don't invalidate. This is to
		// allow just opening a file without bothering the user for a password
		// and silently do something else. In all other cases, the password
		// must be invalidated so it will be asked again
		if (PasswordCache.isPasswordNotSuppliedException(e))
		    throw new PasswordCancelledException();

		PasswordCache.getInstance().invalidate(in.getCanonicalPath());
		// Convert back to PasswordCancelledException if none was supplied.
		//   When the empty string was supplied as password, the exception
		//   message is different, so this can be used to detect if the
		//   password was cancelled.
		if (PasswordCache.isPasswordCancelledException(e))
		    throw new PasswordCancelledException();
	    }
	    throw e;
	}
    }

    /** Return single object from PEM file.
     * <p>
     * This version accepts a type as argument. The first object
     * that matches the type is returned, others are discarded.
     * 
     * @return The object read, or null if type not found.
     */
    public static Object readObject(File in, Class<?> type) throws FileNotFoundException, IOException {
	PEMReader r = new PEMReader(in);
	Object o = null;
	while ( (o=r.readObject())!=null && !type.isInstance(o) );
	r.close();
	if (type.isInstance(o))
	    return o;
	return null;
    }

    
    /** Return single object from encrypted PEM file.
     * <p>
     * The first object will be returned, others are discarded.
     */
    public static Object readObject(File in, String msg) throws FileNotFoundException, IOException {
	PEMReader r = new PEMReader(in, msg);
	Object o = r.readObject();
	r.close();
	return o;
    }

    /** Return single object from encrypted PEM file.
     * <p>
     * This version accepts a type as argument. The first object
     * that matches the type is returned, others are discarded.
     * 
     * @return The object read, or null if type not found.
     */
    public static Object readObject(File in, Class<?> type, String msg) throws FileNotFoundException, IOException {
	PEMReader r = new PEMReader(in, msg);
	Object o = null;
	while ( (o=r.readObject())!=null && !type.isInstance(o) );
	r.close();
	if (type.isInstance(o))
	    return o;
	return null;
    }

}
