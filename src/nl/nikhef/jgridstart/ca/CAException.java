package nl.nikhef.jgridstart.ca;

public class CAException extends Exception {
    public CAException(String msg) {
	super(msg);
    }
    public CAException(Exception e) {
	super(e);
    }
    public CAException(String msg, Exception e) {
	super(msg, e);
    }
}
