package nl.nikhef.jgridstart.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import nl.nikhef.jgridstart.gui.Main;

/** General utility functions that don't fit elsewhere */
public class GeneralUtils {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
    
    /** Properties resource containing standard configuration @see #loadConfig */
    protected static final String standardConfig = "/resources/conf/global.properties";
    
    /** Loads standard system properties from configuration.
     * <p>
     * Reads the standard configuration properties into the {@linkplain System}
     * property store. Doesn't overwrite existing properties, so that they
     * can be overridden from the command-line.
     * 
     * @throws IOException
     */
    public static void loadConfig() throws IOException {
	Properties sysp = System.getProperties();
	Properties p = getConfig();
	for (Enumeration<?> e = p.keys(); e.hasMoreElements(); ) {
	    String key = (String)e.nextElement();
	    if (sysp.getProperty(key)==null)
		sysp.setProperty(key, p.getProperty(key));
	}
	logger.info("jGridstart version "+System.getProperty("jgridstart.version")+" (r"+System.getProperty("jgridstart.revision")+"), configuration loaded");
    }
    
    /** Returns default properties @see #loadConfig */
    public static Properties getConfig() throws IOException {
	Properties p = new Properties();
	p.load(Main.class.getResourceAsStream(standardConfig));
	return p;
    }
}
