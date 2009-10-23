package nl.nikhef.jgridstart.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
import nl.nikhef.jgridstart.logging.LogHelper;
import nl.nikhef.jgridstart.util.GeneralUtils;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;

public class Main {

    // setup logging
    static {
	LogHelper.setupLogging(false);
    }
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart");
    static private CertificateStore store = null;
    
    /** command-line interface entry point */
    public static void main(String[] args) {
	// load system properties, not fatal if it fails
	try {
	    GeneralUtils.loadConfig();
	} catch (IOException e) { }

	// parse command-line arguments
	try {
	    CommandLine line = parseCLIOptions(args);

	    // load certificate store
	    store = new CertificateStore();
	    if (line.hasOption("store"))
		store.load(line.getOptionValue("store"));
	    else
		store.load();

	    // action!
	    if (line.hasOption("help"))
		actionHelp(line);
	    if (line.hasOption("list"))
		actionList(line);
	    if (line.hasOption("print"))
		actionPrint(line);
	    if (line.hasOption("import"))
		actionImport(line);
	    if (line.hasOption("get"))
		actionGet(line);
	    
	} catch (Exception e) {
	    logger.severe(e.getLocalizedMessage());
	    System.exit(1);
	}
	
	System.exit(0);
    }
    
    /** parse the command-line options and handle debugging level */
    protected static CommandLine parseCLIOptions(String[] args) throws ParseException {
	Options opts = getCLIOptions();
	CommandLineParser parser = new GnuParser();
	CommandLine line = parser.parse(opts, args);
	
	// debugging level
	if (line.hasOption('d')) {
	    if (line.getOptionValue('d').toLowerCase().equals("help")) {
		// show available debug levels and quit
		System.out.println("The debug level can be an integer value, or one of:");
		System.out.println("  off, severe, warning, info, config, fine, finer, finest, all");
		System.out.println("Please refer to the Java logging documentation for more details.");
		System.out.println("http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/Level.html");
		System.exit(0);
	    }
	    Level level = Level.parse(line.getOptionValue('d').toUpperCase());
	    logger.setLevel(level);
	    for (int i=0; i<logger.getHandlers().length; i++)
		logger.getHandlers()[i].setLevel(level);
	}

	return line;
    }
    /** return the command-line Options for this utility */
    @SuppressWarnings("static-access") // to use OptionBuilder conveniently
    protected static Options getCLIOptions() {
	final Options opts = new Options();
	if (opts.getOptions().size()>0) return opts;
	
	// commands
	OptionGroup main = new OptionGroup();
	main.addOption(new Option("h", "help", false, "show help message"));
	main.addOption(new Option("l", "list", false, "list the certificates present in the certificate store"));
	main.addOption(OptionBuilder.withArgName("bundle").hasArg()
		.withDescription("import an existing certificate into the certificate store")
		.withLongOpt("import").create('i'));
	main.addOption(new Option("n", "new", false, "create a new key and certificate signing request"));
	main.addOption(new Option("p", "print", false, "print certificate details"));
	main.addOption(new Option("g", "get", false, "get a signed certificate via the internet"));
	main.addOption(new Option("s", "install", false, "install certificate into a browser"));
	main.addOption(new Option("r", "revoke", false, "request revocation of a key+certificate"));
	main.setRequired(true);
	opts.addOptionGroup(main);
	
	// options
	opts.addOption(OptionBuilder.withArgName("store").hasArg()
		.withDescription("location of the certificate store (or omit for default location)")
		.withLongOpt("store").create('s'));
	opts.addOption(OptionBuilder.withArgName("level").hasArg()
		.withDescription("debugging level (use 'help' as level for details)")
		.withLongOpt("debug").create('d'));
	opts.addOption(OptionBuilder.withArgName("certificate").hasArg()
		.withDescription("operate on a certificate")
		.withLongOpt("certificate").create('c'));
	
	return opts;
    }
    
    
    /** show help. note that you might want to 'export COLUMNS' when using sh */
    protected static void actionHelp(CommandLine line) throws ParseException {
	// figure out executable name; shellscript sets variable to aid in this
	String prog = System.getenv("INVOKED_PROGRAM");
	if (prog==null) prog = "java "+Main.class.getName();
	// print help
	HelpFormatter fmt = new HelpFormatter();
	if (System.getenv("COLUMNS")!=null)
	    fmt.setWidth(Integer.valueOf(System.getenv("COLUMNS")));
	PrintWriter out = new PrintWriter(System.out, true);
	out.println( 
	    "usage: "+prog+" [options] (-h|-l|-i <file>)\n" +
	    "usage: "+prog+" [options] -n\n" +
	    "usage: "+prog+" [options] -c <certificate> (-p|-g|-s|-r)");
	fmt.printOptions(out, fmt.getWidth(),
		getCLIOptions(), fmt.getLeftPadding(), fmt.getDescPadding());
	System.exit(0);
    }
    
    /** print list of certificates */
    protected static void actionList(CommandLine line) throws ParseException {
	for (int i=0; i<store.getSize(); i++) {
	    System.out.printf("%s: %s\n", store.get(i).getPath().getName(), store.get(i));
	}
    }
    
    /** import a certificate into the certificate store 
     * @throws PasswordCancelledException 
     * @throws IOException 
     * @throws CertificateException 
     * @throws NoSuchProviderException 
     * @throws KeyStoreException 
     * @throws UnrecoverableKeyException 
     * @throws NoSuchAlgorithmException */
    protected static void actionImport(CommandLine line) throws ParseException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, CertificateException, IOException, PasswordCancelledException {
	// TODO implement and setup cli PasswordCache
	throw new ParseException("import not implemented");
	//store.importFrom(new File(line.getOptionValue("import")));
    }
    
    /** print a certificate 
     * @throws ParseException */
    protected static void actionPrint(CommandLine line) throws ParseException, IOException {
	CertificatePair cert = getCertificate(line);
	if (cert.getCertificate()!=null) {
	    System.out.println(cert.getCertificate());
	} else if (cert.getCSR()!=null) {
	    //System.out.println(cert.getCSR()); doesn't give any meaningful information
	    System.out.println("Certificate signing request present, please try to download the certificate (-g)");
	} else {
	    System.out.println("No certificate or certificate signing request present");
	}
    }
    
    /** get a certificate from its online source after signing 
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     * @throws CertificateCheckException */
    protected static void actionGet(CommandLine line) throws ParseException, KeyManagementException, NoSuchAlgorithmException, IOException, CertificateCheckException, Exception {
	CertificatePair cert = getCertificate(line);
	cert.downloadCertificate();
    }
    
    
    /** Return a certificate as specified on the command-line */
    protected static CertificatePair getCertificate(CommandLine line) throws ParseException {
	String value = line.getOptionValue("certificate");
	if (value==null)
	    throw new ParseException("please specify certificate");
	
	CertificatePair cert = null;
	for (Iterator<CertificatePair> it = store.iterator(); it.hasNext(); ) {
	    CertificatePair cur = it.next();
	    if (cur.getPath().getName().equals(value)) {
		cert = cur;
		break;
	    }
	}
	if (cert==null)
	    throw new ParseException("certificate not found: "+value);
	
	return cert;
    }
}
