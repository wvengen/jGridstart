package nl.nikhef.jgridstart.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	    FileUtils.chmod(file, true, true, false, true);
	    permissionsSet = true;
	}
    }

    public boolean delete() {
	// delete file using new File to make it actually work on java 1.4.2
	return (new File(getPath())).delete();
    }
}