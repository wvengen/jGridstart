package nl.nikhef.jgridstart.gui.wizard;

/** Thrown when user has not succesfully filled in the page contents */
public class ValidationException extends Exception {
	public ValidationException(String msg) {
	    super(msg);
	}
}
