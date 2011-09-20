package nl.nikhef.jgridstart.wrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Wrapper {
    
    // TODO make this more configurable
    public static final String mainJarStart = "jgridstart-small-";

    public static File tmpdir;
    
    public static boolean info = true;
    public static boolean cleanup = true;

    /**
     * @param args command-line arguments
     */
    public static void main(String[] args) throws Exception {
	// // create temporary directory
	// TODO race condition!
	tmpdir = File.createTempFile("jgridstart", null);
	if (cleanup) tmpdir.deleteOnExit();
	tmpdir.delete();
	tmpdir.mkdirs();

	// // extract JARs
	if (info) System.out.println("Extracting JARs to temporary directory: "+tmpdir.getAbsolutePath());
        String mainJar = null;
	String[] libfiles = getResourceListing(Wrapper.class, "lib/");
	for (String fn: libfiles) {
	    if (info) System.out.println("  "+fn);
	    File f = new File(tmpdir, fn);
	    if (cleanup) f.deleteOnExit();
	    InputStream in = Wrapper.class.getResourceAsStream("/lib/"+fn);
	    OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
	    byte[] buf = new byte[2048];
	    for (int n=in.read(buf); n>0; n=in.read(buf))
		out.write(buf, 0, n);
	    in.close();
	    out.close();
            if (fn.startsWith(mainJarStart)) mainJar = fn;
	}
	
	// // construct command to run
	// java command
	ArrayList<String> cmd = new ArrayList<String>();
	String javabin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
	if (System.getProperty("os.name").toLowerCase().contains("win"))
	    javabin += "w.exe";
	cmd.add(javabin);
	// non-system properties as defined in this JVM (by java web start, for example)
	for (Entry<Object, Object> k: System.getProperties().entrySet()) {
	    String key = (String)k.getKey();
	    String value = (String)k.getValue();
	    if (key.startsWith("os.")) continue;
	    if (key.startsWith("sun.")) continue;
	    if (key.startsWith("java.")) continue;
	    if (key.equals("user.name")) continue;
	    if (key.equals("file.separator")) continue;
	    if (key.equals("path.separator")) continue;
	    if (key.equals("line.separator")) continue;
	    // java web start has special property for VM arguments, use these
	    if (key.equals("jnlpx.vmargs")) {
		if ( (value.startsWith("\"") && value.endsWith("\"")) ||
			(value.startsWith("'") && value.endsWith("'")) )
		    value = value.substring(1, value.length()-1);
		for (String a: value.split("\\s+(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))"))
		    cmd.add(a);
		continue;
	    }
	    cmd.add("-D"+key+"="+value);
	}
	// main JAR file
	cmd.add("-jar");
	cmd.add(new File(tmpdir, mainJar).getAbsolutePath());
	// and arguments
	for (String a: args) cmd.add(a);
	
	// // and run
	if (info) {
	    System.out.print("Launching:");
	    for (String a: cmd) System.out.print(" "+a);
	    System.out.println();
	}
	Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
	int ret = p.waitFor();
	if (info) System.out.println("Done, exit code: "+ret);
	System.exit(ret);
    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     * 
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException 
     * @throws IOException 
     */
    public static String[] getResourceListing(Class<?> clazz, String path) throws URISyntaxException, IOException {
	URL dirURL = clazz.getClassLoader().getResource(path);
	if (dirURL != null && dirURL.getProtocol().equals("file")) {
	    /* A file path: easy enough */
	    return new File(dirURL.toURI()).list();
	}

	if (dirURL == null) {
	    /* 
	     * In case of a jar file, we can't actually find a directory.
	     * Have to assume the same jar as clazz.
	     */
	    String me = clazz.getName().replace(".", "/")+".class";
	    dirURL = clazz.getClassLoader().getResource(me);
	}

	if (dirURL.getProtocol().equals("jar")) {
	    /* A JAR path */
	    String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
	    JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	    Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	    Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
	    while(entries.hasMoreElements()) {
		String name = entries.nextElement().getName();
		if (name.startsWith(path)) { //filter according to the path
		    String entry = name.substring(path.length());
		    int checkSubdir = entry.indexOf("/");
		    if (checkSubdir >= 0) {
			// if it is a subdirectory, we just return the directory name
			entry = entry.substring(0, checkSubdir);
		    }
		    if (!entry.equals("")) result.add(entry);
		}
	    }
	    return result.toArray(new String[result.size()]);
	} 

	throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }
}
