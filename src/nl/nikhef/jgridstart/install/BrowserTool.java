package nl.nikhef.jgridstart.install;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import nl.nikhef.jgridstart.install.exception.BrowserExecutionException;
import nl.nikhef.jgridstart.install.exception.BrowserNotAvailableException;

import org.apache.commons.cli.*;

/** Tool to open a web browser and install a PKCS#12 file into
 * a browser's certificate store.
 * 
 * @author wvengen
 */
public class BrowserTool {

    @SuppressWarnings("static-access") // to use OptionBuilder conveniently
    public static void main(String[] args) throws Exception {
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
	
	CommandLineParser parser = new GnuParser();
	CommandLine line = parser.parse(opts, args);
	
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
    }
}
