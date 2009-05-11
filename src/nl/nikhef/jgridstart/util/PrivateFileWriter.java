package nl.nikhef.jgridstart.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * A FileWriter that writes to a file that is accessible only to the user.
 * 
 * @author wvengen
 */
public class PrivateFileWriter extends FileWriter {
    static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
    
    private File file = null;
    private boolean permissionsSet = false;

    /** Create a FileWriter that writes to a file with permissions so that
     * only the user has access to its contents.
     * 
     * @throws IOException
     */
    public PrivateFileWriter(File f) throws IOException {
	super(f);
	this.file = f;
    }

    /** Return pathname string. See File.getPath() for details. */
    public String getPath() {
	return file.getPath();
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

    protected void ensurePermissions() {
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