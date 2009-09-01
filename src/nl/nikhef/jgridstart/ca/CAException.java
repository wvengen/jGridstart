package nl.nikhef.jgridstart.ca;

public class CAException extends Exception {
    private String msg = null;
    
    public CAException(String msg) {
	this.msg = msg;
    }
    
    @Override
    public String toString() {
	return msg;
    }
}
