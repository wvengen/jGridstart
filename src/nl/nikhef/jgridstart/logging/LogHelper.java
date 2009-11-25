package nl.nikhef.jgridstart.logging;

import java.util.logging.LogManager;
import java.util.logging.Logger;

/** Logging helper methods */
public class LogHelper {
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.logging");
    
    /** Setup logging for the application */
    public static void setupLogging(boolean debug) {
	// load config
	final String conf = debug ? "/logging.debug.properties" : "/logging.properties";
	try {
	    LogManager.getLogManager().readConfiguration(LogHelper.class.getResourceAsStream(conf));
	} catch(Exception e) {
	    System.out.println("Warning: logging configuration could not be set");
	}
	// emit some extra info
	logger.info("Platform: "+System.getProperty("os.name")+" "+System.getProperty("os.arch")+" " +
		"version "+System.getProperty("sys.version"));
	logger.info("JRE: "+System.getProperty("java.vendor")+" version "+System.getProperty("java.version") +
		" JIT "+System.getProperty("java.compiler") + "; " +
		"installed in "+System.getProperty("java.home"));
	logger.info("  classpath="+System.getProperty("java.class.path"));
    }
}
