package nl.nikhef.jgridstart.install.exception;

/**
 * Browser exception occuring during the execution of the browser
 */
public class BrowserNotAvailableException extends BrowserException {
    public BrowserNotAvailableException(String browser) {
        super(browser, "Browser not available");
    }
}
