package nl.nikhef.jgridstart.install;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import nl.nikhef.jgridstart.install.exception.BrowserExecutionException;
import nl.nikhef.jgridstart.install.exception.BrowserNotAvailableException;

/** Interface to a system web browser.
 * <p>
 * This allows one to open an URL in a web browser, and install certificates and keys
 * into their certificate stores.
 * <p>
 * Projects that share some of the goals are
 * <a href="http://www.centerkey.com/java/browser/">BareBonesBrowserLauncher</a>
 * and <a href="http://browserlaunch2.sourceforge.net/">BrowserLaunch2</a>. These
 * would be good alternatives when no certificate installation is required.
 * This API is heavily inspired by the latter.
 * 
 * @author wvengen
 */
public interface IBrowsers {
    
    /** Discovers available browsers. */
    public void initialize() throws IOException;
    
    /**
     * Opens the passed url in the system's default browser.
     *
     * @param urlString URL to open
     * @throws BrowserExecutionException 
     */
    public void openUrl(String urlString)
    	throws BrowserNotAvailableException, BrowserExecutionException;

    /** Opens a URL in a specific browser.
     * <p>
     * Allows user to target a specific browser. The id's of
     * potential browsers can be accessed via the
     * {@link #getBrowserList() getBrowserList} method.
     * <p>
     * If the call to the requested browser fails, the code will
     * fail over to the default browser.
     *
     * @param browserid browser identifier
     * @param urlString URL to open
     * @throws BrowserNotAvailableException 
     * @throws BrowserExecutionException 
     */
    public void openUrl(String browserid, String urlString)
    	throws BrowserNotAvailableException, BrowserExecutionException;

    /** Opens a URL in one of the supplied browsers.
     * <p>
     * Allows user to target several browsers. The names of
     * potential browsers can be accessed via the
     * {@link #getBrowserList() getBrowserList} method.
     * <p>
     * The browsers from the list will be tried in order
     * (first to last) until one of the calls succeeds. If
     * all the calls to the requested browsers fail, the code
     * will fail over to the default browser.
     *
     * @param browserids List of browser identifiers to try
     * @param urlString URL to open
     * @throws BrowserNotAvailableException 
     * @throws BrowserExecutionException 
     */
    public void openUrl(List<String> browserids, String urlString)
    	throws BrowserNotAvailableException, BrowserExecutionException;

    /** Returns a list of browser id's to be used for browser targetting.
     *
     * @return List
     */
    public Set<String> getBrowserList();
    
    /** Returns a list of browser id's that are recognised by the program.
     * <p>
     * They may not be all present in the system, use {@link #getBrowserList}
     * for that.
     */
    public Set<String> getKnownBrowserList();
    
    /** Returns the default system browser's id.
     * <p>
     * If no default system browser could be found, returns the first
     * browser found. Returns null if no browsers found.
     */
    public String getDefaultBrowser();
    
    /** Returns the descriptive name of a browser's id.
     * <p>
     * This should be used when presenting an option to the user.
     */
    public String getBrowserName(String browserid);
    
    /** Installs a PKCS#12 file into a browser's keystore.
     * <p>
     * This probably requires user interaction such as entering the keystore's
     * master password, and the PCKS#12 file's passphrase.
     * 
     * @param browserid browser identifier to i nstall certificate for
     * @param pkcs file containing PKCS#12 certificate
     */
    public void installPKCS12(String browserid, File pkcs)
    	throws BrowserNotAvailableException, BrowserExecutionException;
    
    /** Installs a PKCS#12 file into the default browser's keystore.
     * @see #installPKCS12(String, File) */
    public void installPKCS12(File pkcs)
	throws BrowserNotAvailableException, BrowserExecutionException;
}
