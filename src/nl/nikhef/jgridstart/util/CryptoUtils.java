package nl.nikhef.jgridstart.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.PasswordFinder;

/**
 * convenience functions for cryptography
 * 
 * @author wvengen
 */
public class CryptoUtils {

    /**
     * Write an object to a PEM file without a password
     * 
     * @param src Object to write (see PEMWriter.writeObject() )
     * @param writer destination to write to
     * @throws IOException
     */
    static public void writePEM(Object src, Writer writer) throws IOException {
	PEMWriter pemwriter = new PEMWriter(writer);
	pemwriter.writeObject(src);
	pemwriter.close();
    }

    /**
     * Write an object to a PEM file and encrypt with password
     * 
     * @param src Object to write (see PEMWriter.writeObject() )
     * @param writer destination to write to
     * @param pwf PasswordFinder to obtain password from
     * @throws IOException
     */
    static public void writePEM(Object src, Writer writer, PasswordFinder pwf) throws IOException {
	PEMWriter pemwriter = new PEMWriter(writer);
	try {
	    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
	    pemwriter.writeObject(src, "DESEDE", pwf.getPassword(), random);
	} catch (NoSuchAlgorithmException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	pemwriter.close();
    }
    
    /**
     * Read an object from a PEM file
     * 
     * @param reader source to read from
     * @param pwf PasswordFinder to obtain password from, or null to load without password
     * @return Crypto object read
     * @throws IOException
     */
    static public Object readPEM(Reader reader, PasswordFinder pwf) throws IOException {
	Object ret;
	PEMReader pemreader;
	if (pwf!=null)
	    pemreader = new PEMReader(reader, pwf);
	else
	    pemreader = new PEMReader(reader);
	ret = pemreader.readObject();
	pemreader.close();
	return ret;
    }
}
