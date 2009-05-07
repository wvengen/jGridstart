package nl.nikhef.jgridstart.cli;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;

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

public class Main {

    // setup logging
    static {
	try {
	    LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
	} catch(Exception e) {
	    System.out.println("Warning: logging configuration could not be set");
	}
    }
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart");
    static private CertificateStore store = null;
    
    /** command-line interface entry point */
    public static void main(String[] args) {
	// parse command-line arguments
	CommandLine line = null;
	try {
	    line = parseCLIOptions(args);
	} catch (ParseException e) {
	    logger.severe("Malformed command-line: " + e.getLocalizedMessage());
	    System.exit(1);
	}
	
	// load certificate store
	store = new CertificateStore();
	if (line.hasOption("store"))
	    store.load(line.getOptionValue("store"));
	else
	    store.load();
	
	// action!
	if (line.hasOption("list"))
	    actionList(line);
	
	System.exit(0);
    }
    
    /** parse the command-line options and handle the most basic options */
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

	// help
	if (line.hasOption('h')) {
	    // figure out executable name; shellscript sets variable to aid in this
	    String prog = System.getenv("INVOKED_PROGRAM");
	    if (prog==null) prog = "java "+Main.class.getName();
	    // print help
	    // note that might want to 'export COLUMNS' using sh
	    HelpFormatter fmt = new HelpFormatter();
	    if (System.getenv("COLUMNS")!=null)
		fmt.setWidth(Integer.valueOf(System.getenv("COLUMNS")));
	    fmt.printHelp(prog, opts);
	    System.exit(0);
	}
	
	return line;
    }
    /** return the command-line Options for this utility */
    @SuppressWarnings("static-access") // to use OptionBuilder conveniently
    protected static Options getCLIOptions() {
	final Options opts = new Options();
	
	// commands
	OptionGroup main = new OptionGroup();
	main.addOption(new Option("l", "list", false, "list the certificates present in the certificate store"));
	main.addOption(new Option("h", "help", false, "show help message"));
	main.setRequired(true);
	opts.addOptionGroup(main);
	
	// options
	opts.addOption(OptionBuilder.withArgName("store").hasArg()
		.withDescription("location of the certificate store (or omit for default location)")
		.withLongOpt("store").create('s'));
	opts.addOption(OptionBuilder.withArgName("level").hasArg()
		.withDescription("debugging level (use 'help' as level for details)")
		.withLongOpt("debug").create('d'));
	opts.addOption(OptionBuilder.withArgName("serial").hasArg()
		.withDescription("operate on a certificate")
		.withLongOpt("certificate").create('c'));
	
	return opts;
    }
    
    // print list of certificates
    protected static void actionList(CommandLine line) {
	for (int i=0; i<store.getSize(); i++) {
	    System.out.printf("%s: %s\n", store.get(i).getPath().getName(), store.get(i));
	}
    }
}
