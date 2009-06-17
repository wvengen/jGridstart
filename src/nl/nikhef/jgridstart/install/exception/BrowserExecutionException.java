package nl.nikhef.jgridstart.install.exception;

/**
 * Browser exception occuring during the execution of the browser
 */
public class BrowserExecutionException extends BrowserException {
    public BrowserExecutionException(String browser, Throwable cause) {
        super(browser, cause);
    }

    public BrowserExecutionException(String browser, String message) {
        super(browser, message);
    }
}
