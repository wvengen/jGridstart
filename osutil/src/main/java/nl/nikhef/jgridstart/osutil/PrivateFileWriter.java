package nl.nikhef.jgridstart.osutil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/** {@link FileWriter} that writes to a file that is accessible only to the user.
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
    /** Return the associated File object */
    public File getFile() {
	return file;
    }

    @Override
    public void write(char[] cbuf) throws IOException {
	ensurePermissions();
	super.write(cbuf);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
	ensurePermissions();
	super.write(cbuf, off, len);
    }

    @Override
    public void write(int c) throws IOException {
	ensurePermissions();
	super.write(c);
    }

    @Override
    public void write(String str) throws IOException {
	ensurePermissions();
	super.write(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
	ensurePermissions();
	super.write(str, off, len);
    }

    protected void ensurePermissions() {
	// make sure we have correct permissions before actually writing data
	if (!permissionsSet) {
	    FileUtil.chmod(file, true, true, false, true);
	    permissionsSet = true;
	}
    }

    public boolean delete() {
	// delete file using new File to make it actually work on java 1.4.2
	return (new File(getPath())).delete();
    }
    
    /** Returns an output stream for this writer. While Java 1.6 accepts
     * a FileWriter mostly, older versions still need an OutputStream in
     * some places, e.g. like Properties.store().
     * 
     * TODO verify encoding; Properties.store() uses iso8859-1 but double check
     */
    public OutputStream getOutputStream() {
	return new OutputStream() {
	    @Override
	    public void close() throws IOException {
		PrivateFileWriter.this.close();
	    }
	    @Override
	    public void flush() throws IOException {
		PrivateFileWriter.this.flush();
	    }
	    @Override
	    public void write(int b) throws IOException {
		PrivateFileWriter.this.write(b);
	    }
	    @Override
	    public void write(byte[] b) throws IOException {
		PrivateFileWriter.this.write(new String(b));
	    }
	    @Override
	    public void write(byte[] b, int off, int len) throws IOException {
		PrivateFileWriter.this.write(new String(b, off, len));
	    }
	};
    }
}
