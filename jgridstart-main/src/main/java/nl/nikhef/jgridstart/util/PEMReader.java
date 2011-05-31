package nl.nikhef.jgridstart.util;

import java.io.IOException;
import java.io.Reader;

import org.bouncycastle.openssl.PasswordFinder;

/** PEM-file reader
 * <p>
 *  Identical to BouncyCastle's version, but this one always accepts
 *  garbage before and after the BEGIN/END object blocks. BouncyCastle
 *  1.46 stopped accepting garbage at the beginning, and this class
 *  does accept it.
 *  <p>
 *  Note that {@link #mark} and {@link #reset} cannot be used because
 *  of this workaround.
 *  
 * @author wvengen
 *
 */
public class PEMReader extends org.bouncycastle.openssl.PEMReader {

    public PEMReader(Reader paramReader) {
	super(paramReader);
    }

    public PEMReader(Reader paramReader, PasswordFinder paramPasswordFinder) {
	super(paramReader, paramPasswordFinder);
    }

    public PEMReader(Reader paramReader, PasswordFinder paramPasswordFinder,
	    String paramString) {
	super(paramReader, paramPasswordFinder, paramString);
    }

    public PEMReader(Reader paramReader, PasswordFinder paramPasswordFinder,
	    String paramString1, String paramString2) {
	super(paramReader, paramPasswordFinder, paramString1, paramString2);
    }
    
    @Override
    public Object readObject() throws IOException {
	String line;
	do {
	    mark(256);
	    line = readLine();
	} while (line!=null && !line.startsWith("-----BEGIN"));
	if (line!=null) reset();
	return super.readObject();
    }

}
