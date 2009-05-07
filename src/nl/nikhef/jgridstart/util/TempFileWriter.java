package nl.nikhef.jgridstart.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A more safe temporary file writer class
 * 
 * The standard File.createTempFile() method has a number of problems. This is
 * an attempt to counter some of its problems. - file is made accessible by user
 * only on write (when supported) - file can be deleted properly after use (it
 * is always deleted automatically on exit)
 * 
 * @author wvengen
 */
public class TempFileWriter extends FileWriter {
    static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
    
    private File file = null;
    private boolean permissionsSet = false;

    /**
     * Create a temporary file
     * 
     * See File.createTempFile() for a description of the arguments.
     * 
     * @param prefix
     * @param suffix
     * @throws IOException
     */
    public TempFileWriter(String prefix, String suffix) throws IOException {
	this(File.createTempFile(prefix, suffix));
    }

    /** Return pathname string. See File.getPath() for details. */
    public String getPath() {
	return file.getPath();
    }

    private TempFileWriter(File file) throws IOException {
	super(file);
	this.file = file;
	file.deleteOnExit(); // that at the least
    }

    public void write(char[] cbuf) throws IOException {
	ensurePermissions();
	super.write(cbuf);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
	ensurePermissions();
	super.write(cbuf, off, len);
    }

    public void write(int c) throws IOException {
	ensurePermissions();
	super.write(c);
    }

    public void write(String str) throws IOException {
	ensurePermissions();
	super.write(str);
    }

    public void write(String str, int off, int len) throws IOException {
	ensurePermissions();
	super.write(str, off, len);
    }

    private void ensurePermissions() {
	// make sure we have correct permissions before actually writing data
	if (!permissionsSet) {
	    chmod(file, true, true, false, true);
	    permissionsSet = true;
	}
    }

    public boolean delete() {
	// delete file using new File to make it actually work on java 1.4.2
	return (new File(getPath())).delete();
    }

    /**
     * Change file permissions
     * 
     * Not supported natively until java 1.6. Bad Java.
     * 
     * @param file File to set permissions on
     * @param read  if reading should be allowed
     * @param write if writing should be allowed
     * @param exec if executing should be allowed
     * @param ownerOnly true to set group&world permissions to none,
     *                  false to set them all alike
     */
    static public boolean chmod(File file, boolean read, boolean write,
	    boolean exec, boolean ownerOnly) {
	try {
	    // Try Java 1.6 method first.
	    // This is also compilable on lower java versions
	    boolean ret = true;
	    Method setReadable = File.class.getDeclaredMethod("setReadable",
		    new Class[] { boolean.class, boolean.class });
	    Method setWritable = File.class.getDeclaredMethod("setWritable",
		    new Class[] { boolean.class, boolean.class });
	    Method setExecutable = File.class.getDeclaredMethod("setExecutable",
		    new Class[] { boolean.class, boolean.class });

	    ret &= (Boolean)setReadable.invoke(file, new Object[] { read, ownerOnly });
	    ret &= (Boolean)setWritable.invoke(file, new Object[] { write, ownerOnly });
	    ret &= (Boolean)setExecutable.invoke(file, new Object[] { exec, ownerOnly });
	    
	    if (logger.isLoggable(Level.FINEST)) {
		String perms = new String(new char[] {
			read?  'r' : '-',
			write? 'w' : '-',
			exec?  'x' : '-'
		}) + (ownerOnly ? "user" : "all");
		logger.finest("Java 1.6 chmod "+perms+" of "+file+" returns "+ret);
	    }

	    return ret;
	} catch (InvocationTargetException e) {
	    // throw exceptions caused by set* methods
	    throw (SecurityException)e.getTargetException();
	    // return false; // (would be unreachable code)

	} catch (NoSuchMethodException e) {
	} catch (IllegalAccessException e) {
	} catch (IllegalArgumentException e) {
	}
	
	try {
	    // fallback to unix command
	    int perm = 0;
	    if (read)  perm |= 4;
	    if (write) perm |= 2;
	    if (exec)  perm |= 1;
	    String[] chmodcmd = { "chmod", null, file.getPath() };
	    if (ownerOnly) {
		Object args[] = { new Integer(perm) };
		chmodcmd[1] = String.format("0%1d00", args);
	    } else {
		Object args[] = {new Integer(perm),new Integer(perm),new Integer(perm)};
		chmodcmd[1] = String.format("0%1d%1d%1d", args);
	    }
	    Process p = Runtime.getRuntime().exec(chmodcmd);
	    int ret = p.waitFor();
	    String schmodcmd = "";
	    for (int i=0; i<chmodcmd.length; i++) schmodcmd += " "+chmodcmd[i]; 
	    logger.finest("exec"+schmodcmd+" returns "+ret);
	    return ret == 0;
	} catch (Exception e2) {
	    return false;
	}
    }
}