package nl.nikhef.jgridstart.util;

import java.io.File;
import java.io.IOException;

/** A more safe temporary file writer
 * <p>
 * The standard {@link File#createTempFile} method has a number of problems. This is
 * an attempt to counter some of its problems. - file is made accessible by user
 * only on write (when supported) - file can be deleted properly after use (it
 * is always deleted automatically on exit)
 * 
 * @author wvengen
 */
public class TempFileWriter extends PrivateFileWriter {
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

    private TempFileWriter(File file) throws IOException {
	super(file);
	file.deleteOnExit(); // that at the least
    }
}
