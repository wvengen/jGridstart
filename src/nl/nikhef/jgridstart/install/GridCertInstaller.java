package nl.nikhef.jgridstart.install;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.CertificatePair;

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
     */
    protected static String runProgram(String progname, String[] args) throws IOException {
	String s = System.getProperty("line.separator");
	// create full command line
	String[] cmd = new String[args.length+1];
	cmd[0] = progname;
	System.arraycopy(args, 0, cmd, 1, args.length);
	// TODO make sure we can find the program
	if (System.getProperty("os.name").startsWith("Windows"))
	    cmd[0] += ".exe";
	// run
	String scmd = "";
	for (int i=0; i<cmd.length; i++) scmd += " "+cmd[i]; 
	logger.finer("Running command:"+scmd);
	Process p = Runtime.getRuntime().exec(cmd);
	// retrieve output
	String lineout, lineerr;
	String output = "";
	BufferedReader stdout = new BufferedReader(
		new InputStreamReader(p.getInputStream()));
	BufferedReader stderr = new BufferedReader(
		new InputStreamReader(p.getErrorStream()));
	while ( (lineout=stdout.readLine()) != null && (lineerr=stderr.readLine()) != null) {
	    if (lineout!=null) output += lineout + s;
	    if (lineerr!=null) output += lineerr + s;
	}
	stdout.close();	
	
	return output;
    }
}
