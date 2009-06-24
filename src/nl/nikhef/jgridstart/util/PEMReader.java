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
    
    /** Return single object from PEM file */
    public static Object readObject(File in) throws FileNotFoundException, IOException {
	PEMReader r = new PEMReader(in);
	Object o = r.readObject();
	r.close();
	return o;
    }

    /** Return single object from encrypted PEM file */
    public static Object readObject(File in, String msg) throws FileNotFoundException, IOException {
	PEMReader r = new PEMReader(in, msg);
	Object o = r.readObject();
	r.close();
	return o;
    }
}
