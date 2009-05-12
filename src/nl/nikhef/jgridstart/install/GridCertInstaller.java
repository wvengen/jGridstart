package nl.nikhef.jgridstart.install;

import java.io.IOException;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.util.FileUtils;

/** Install a certificate into a certain browser. This is the general
 * class from which all others are derived. 
 * 
 * @author wvengen
 */
public abstract class GridCertInstaller {
    
    static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.install");
    
    /** helper method: run a program and return its stdout+stderr
     * 
     * @param progname name of program to run (without .exe or other suffix)
     * @param args Array of Strings with command and arguments
     * @return output of the program (stdout+stderr)
     * @throws IOException
     * @throws InterruptedException 
     */
    protected static String runProgram(String progname, String[] args) throws IOException {
	// create full command line
	String[] cmd = new String[args.length+1];
	cmd[0] = progname;
	System.arraycopy(args, 0, cmd, 1, args.length);
	// TODO make sure we can find the program
	if (System.getProperty("os.name").startsWith("Windows"))
	    cmd[0] += ".exe";
	// run
	String output = "";
	FileUtils.Exec(cmd, output);
	return output;
    }
}
