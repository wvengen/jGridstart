package nl.nikhef.passwordcache;

import java.io.IOException;
    
/** user cancelled password entry */
public class PasswordCancelledException extends IOException {

    public PasswordCancelledException() {
	super();
    }

    @Override
    public String toString() {
	return "Password request was cancelled";
    }
}
