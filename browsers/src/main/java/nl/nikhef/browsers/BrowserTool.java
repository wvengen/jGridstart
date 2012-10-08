package nl.nikhef.browsers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import nl.nikhef.browsers.exception.BrowserExecutionException;
import nl.nikhef.browsers.exception.BrowserNotAvailableException;

import org.apache.commons.cli.*;

/** Command-line tool to open a web browser and install a PKCS#12 file
 * into a browser's certificate store.
 * 
 * @author wvengen
 */
public class BrowserTool {
    
    static protected Logger logger = Logger.getLogger("nl.nikhef.browsers");

    @SuppressWarnings("static-access") // to use OptionBuilder conveniently
    public static void main(String[] args) throws Exception {
	try {
	    final Options opts = new Options();
	    final OptionGroup main = new OptionGroup();

	    // one of the following actions
	    main.addOption(new Option("h", "help", false, "show help message"));
	    main.addOption(new Option("l", "list", false, "list available browsers"));
	    main.addOption(new Option("k", "known", false, "list known browsers"));
	    main.addOption(OptionBuilder.withArgName("url").hasArg()
		    .withDescription("open a web page with the browser")
		    .withLongOpt("open").create('o'));
	    main.addOption(OptionBuilder.withArgName("pkcs#12 file").hasArg()
		    .withDescription("install a pkcs#12 file for the web browser")
		    .withLongOpt("install").create('i'));
	    main.setRequired(true);
	    opts.addOptionGroup(main);

	    // and options
	    opts.addOption(OptionBuilder.withArgName("browser id").hasArg()
		    .withDescription("use the specified web browser (see list)")
		    .withLongOpt("browser").create('b'));
	    opts.addOption(OptionBuilder.withArgName("level").hasArg()
		    .withDescription("debugging level (use 'help' as level for details)")
		    .withLongOpt("debug").create('d'));

	    CommandLineParser parser = new GnuParser();
	    CommandLine line = parser.parse(opts, args);
	    
	    if (line.hasOption("debug")) {
		if (line.getOptionValue("debug").toLowerCase().equals("help")) {
		    // show available debug levels and quit
		    System.out.println("The debug level can be an integer value, or one of:");
		    System.out.println("  off, severe, warning, info, config, fine, finer, finest, all");
		    System.out.println("Please refer to the Java logging documentation for more details.");
		    System.out.println("http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/Level.html");
		    System.exit(0);
		}
		Level level = Level.parse(line.getOptionValue("debug").toUpperCase());
		logger.setLevel(level);
		// include default log handler if none present
		if (logger.getHandlers().length==0) {
		    Handler handler = new ConsoleHandler();
		    handler.setFormatter(new SimpleFormatter() {
			public String format(LogRecord record) {
		                return record.getLevel() + ": " + record.getMessage() + "\r\n";
		        }			
		    });
		    logger.addHandler(handler);
		}
		// set logging level for all handlers as well
		for (int i=0; i<logger.getHandlers().length; i++)
		    logger.getHandlers()[i].setLevel(level);
	    }

	    if (line.hasOption("help"))
		actionHelp(line, opts);
	    if (line.hasOption("list"))
		actionList(line);
	    if (line.hasOption("known"))
		actionKnown(line);
	    if (line.hasOption("open"))
		actionOpen(line);
	    if (line.hasOption("install"))
		actionInstall(line);
	
	} catch (ParseException e) {
	    System.err.println(e.getLocalizedMessage());
	}
	System.exit(0);
    }

    /** Show help message */
    protected static void actionHelp(CommandLine line, Options opts) {
	// figure out executable name; shellscript sets variable to aid in this
	String prog = System.getenv("INVOKED_PROGRAM");
	if (prog==null) prog = "java "+BrowserTool.class.getName();
	// print help
	HelpFormatter fmt = new HelpFormatter();
	if (System.getenv("COLUMNS")!=null)
	    fmt.setWidth(Integer.valueOf(System.getenv("COLUMNS")));
	PrintWriter out = new PrintWriter(System.out, true);
	out.println("usage: "+prog+" (-h|-l|-k|-o <url>|-i <pkcs#12 file>) [-b <browser>]");
	fmt.printOptions(out, fmt.getWidth(), opts, fmt.getLeftPadding(), fmt.getDescPadding());
	System.exit(0);
    }
    
    /** Show the list of detected browsers */
    protected static void actionList(CommandLine line) throws IOException {
	IBrowsers browsers = BrowserFactory.getInstance();
	for (Iterator<String> it = browsers.getBrowserList().iterator(); it.hasNext(); ) {
	    String b = it.next();
	    char c = ' ';
	    if (b.equals(browsers.getDefaultBrowser())) c = '*';
	    System.out.println(String.format("%c %-25s %s", c, b, browsers.getBrowserName(b) ));
	}
    }
    
    /** Show the list of recognised browsers */
    protected static void actionKnown(CommandLine line) throws IOException {
	IBrowsers browsers = BrowserFactory.getInstance();
	for (Iterator<String> it = browsers.getKnownBrowserList().iterator(); it.hasNext(); ) {
	    String b = it.next();
	    char c = ' ';
	    if (b.equals(browsers.getDefaultBrowser())) c = '*';
	    System.out.println(String.format("%c %-25s %s", c, b, browsers.getBrowserName(b) ));
	}
    }
    
    /** Open the specified url 
     * 
     * @throws IOException 
     * @throws BrowserExecutionException 
     * @throws BrowserNotAvailableException */
    protected static void actionOpen(CommandLine line) throws BrowserNotAvailableException, BrowserExecutionException, IOException {
	if (line.hasOption("browser"))
	    BrowserFactory.getInstance().openUrl(line.getOptionValue("browser"), line.getOptionValue("open"));
	else
	    BrowserFactory.getInstance().openUrl(line.getOptionValue("open"));
    }
    
    /** Install the specified PKCS#12 file into the browser */
    protected static void actionInstall(CommandLine line) throws BrowserNotAvailableException, BrowserExecutionException, IOException {
	if (line.hasOption("browser"))
	    BrowserFactory.getInstance().installPKCS12(line.getOptionValue("browser"), new File(line.getOptionValue("install")));
	else
	    BrowserFactory.getInstance().installPKCS12(new File(line.getOptionValue("install")));
	// sleep 5s to give the Firefox installer a chance to read the files involved
	// before the tempfiles are deleted
	try {
	    Thread.sleep(5*1000);
	} catch (InterruptedException e) { }
    }
}
