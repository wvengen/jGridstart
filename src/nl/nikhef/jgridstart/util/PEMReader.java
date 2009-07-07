package nl.nikhef.jgridstart.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/** PEM file reader that works directory on files, integrated with {@link PasswordCache}. */
public class PEMReader extends org.bouncycastle.openssl.PEMReader {
    
    /** Create PEMReader for a File */
    public PEMReader(File in) throws FileNotFoundException {
	super(new FileReader(in));
    }
    
    /** Create PEMReader for an encrypted File */
    public PEMReader(File in, String msg) throws FileNotFoundException, IOException {
	super(new FileReader(in), PasswordCache.getInstance().getDecryptPasswordFinder(msg, in.getCanonicalPath()));
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