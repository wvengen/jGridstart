package nl.nikhef.jgridstart.wrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map.Entry;

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
	ClassLoader cl = Wrapper.class.getClassLoader();
	if (info) System.out.println("Extracting JARs to temporary directory: "+tmpdir.getAbsolutePath());
        String mainJar = null;
        String classpath = readStream(cl.getResourceAsStream("lib/classpath"));
	for (String fpath: classpath.split(":")) {
	    fpath = fpath.trim();
	    if (fpath.equals("")) continue;
	    String fname = fpath;
	    if (fname.contains("/"))
		fname = fname.substring(fname.lastIndexOf('/')+1);
	    if (info) System.out.println("  "+fname);
	    File f = new File(tmpdir, fname);
	    if (cleanup) f.deleteOnExit();
	    InputStream in = cl.getResourceAsStream(fpath);
	    OutputStream out = new FileOutputStream(f);
	    byte[] buf = new byte[2048];
	    while (true) {
		int n = in.read(buf);
		if (n<=0) break;
		out.write(buf, 0, n);
	    }
	    out.close();
	    in.close();
            if (fname.startsWith(mainJarStart)) mainJar = fname;
	}
	
	if (mainJar==null)
	    throw new Exception("Could not find main application JAR.");
	
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
    
    public static String readStream(InputStream in) throws IOException {
	StringBuffer b = new StringBuffer();
	byte[] data = new byte[1024];
	while (in.read(data) > 0)
	    b.append(new String(data));
	in.close();
	return b.toString();
    }
}
