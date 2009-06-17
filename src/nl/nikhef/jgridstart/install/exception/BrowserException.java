package nl.nikhef.jgridstart.install.exception;

/**
 * Browser exception.
 * <p>
 * All other browser-related exceptions are derived from this class.
 */
public class BrowserException extends Exception {
    public BrowserException(String browser, Throwable cause) {
        super(browser+": ", cause);
    }

    public BrowserException(String browser, String message) {
        super(browser+": "+message);
    }
}
