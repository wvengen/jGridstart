package nl.nikhef.jgridstart.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import nl.nikhef.jgridstart.util.TempFileWriter;


public class GridCertInstallerFirefox extends GridCertInstaller {
    
    /** install a base64-encoded PKCS12 client certificate into Firefox */ 
    public static void install(String pkcsfile, char[] pw) throws IOException {
	TempFileWriter pwfile = null;
	try {
	    // store password in temporary file
	    pwfile = new TempFileWriter("jgridstart", ".pw");
	    pwfile.append(new String(pw));
	    pwfile.close();

	    // get profiles
	    HashMap<String, String> profiles = getProfileFolders();
	    if (profiles.size()==0) return;
	    if (profiles.size()>1) {
		// TODO get selection
	    }

	    for (Iterator<String> it = profiles.keySet().iterator(); it.hasNext(); ) {
		String profilename = it.next();
		String profiledir = profiles.get(profilename);
		String args[] = {
			"-d", profiledir,		// directory with certificate store
			"-i", pkcsfile,		// input certificate
			"-w", pwfile.getPath()	// file with pkcs12 password
		};
		logger.info("Adding certificate "+pkcsfile+" to "+profiledir);
		pk12Util(args);
	    }
	} finally {
	    if (pwfile!=null) pwfile.delete();
	}
    }
    
    /** detect where Firefox profiles are present
     *
     * http://kb.mozillazine.org/Profile_folder_-_Firefox
     * http://lopica.sourceforge.net/os.html
     * 
     * @return HashMap of name => path pairs of Firefox profiles
     * @throws IOException 
     */
    private static HashMap<String, String> getProfileFolders() throws IOException {
	ArrayList<String> bases = new ArrayList<String>();
	HashMap<String, String> profiles = new HashMap<String, String>();
	String s = File.separator;
	
	if (System.getProperty("os.name").startsWith("Windows")) {
	    // Microsoft Windows
	    bases.add(System.getenv("APPDATA") + s + "Mozilla" + s +
		    "Firefox" + s + "Profiles");
	    bases.add(System.getenv("APPDATA") + s + "Roaming" + s +
		    "Mozilla" + s + "Firefox" + s + "Profiles");
	} else if (System.getProperty("os.name").startsWith("Mac OS")) {
	    // Mac OS (X)
	    bases.add(System.getProperty("user.home") + s + "Library" + s +
		    "Mozilla" + s + "Firefox" + s + "Profiles");
	    bases.add(System.getProperty("user.home") + s +
		    "Application Support" + s + "Firefox" + s + "Profiles");
	} else {
	    // Assume the others have a unix-like directory structure
	    bases.add(System.getProperty("user.home") + s +
		    ".mozilla" + s + "firefox");
	}
	
	// Find and process profiles.ini
	for (Iterator<String> it = bases.iterator(); it.hasNext(); ) {
	    try {
		String base = it.next();
		BufferedReader rd = new BufferedReader(
			new FileReader(base + s + "profiles.ini"));
		String line;
		String name = null, path = null;
		boolean isrelative = true; 
		while ( (line=rd.readLine()) != null ) {
		    line = line.trim();
		    // handle start of new section
		    if (line.startsWith("[") && line.endsWith("]")) {
			name = path = null;
			isrelative = true;
		    }
		    // handle known parameters
		    if (line.startsWith("Name=")) name = line.substring(5);
		    if (line.startsWith("IsRelative=")) isrelative = line.substring(11).equals("1");
		    if (line.startsWith("Path=")) path = line.substring(5);
		    // add if required parameters are gathered
		    if (name!=null && path!=null) {
			if (isrelative) path = base + s + path;
			profiles.put(name, path);
			name = path = null;
		    }
		}
		rd.close();
	    } catch(FileNotFoundException e) { continue; }
	}
	
	return profiles;
    }

    /** run the pk12util program
     * 
     * @param args arguments to it
     * @return output of the program
     * @throws IOException 
     */
    private static String pk12Util(String[] args) throws IOException {
	return runProgram("pk12util", args);
    }
}
