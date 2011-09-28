package nl.nikhef.jgridstart.gui.wizard;

import java.util.Iterator;
import java.util.List;

import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.passwordcache.PasswordCache;

import org.jdesktop.swingworker.SwingWorker;

/** General worker thread for integration with {@link TemplateWizardPage}
 * <p>
 * This extends {@link SwingWorker} such that it handles {@linkplain #String}
 * {@linkplain #publish} events can propagate exceptions to the template.
 */
abstract public class TemplateWizardWorker<T> extends SwingWorker<T, String> {
    /** exception from background thread */
    protected Throwable e = null;
    /** page to work on for setting error */
    protected ITemplateWizardPage page = null;

    public TemplateWizardWorker(ITemplateWizardPage page) {
	super();
	this.page = page;
    }

    /** Internal background worker method.
     * <p>
     * Don't use this in child classes. To use the error handling this
     * class provides, implement {@link #doWorkInBackground}.
     */
    @Override
    protected T doInBackground() {
	T ret = null;
	try {
	    ret = doWorkInBackground();
	    publish("state.done");
	} catch (Throwable e) {
	    // store exception so it can be shown to the user
	    this.e = e;
	    publish("state.cancelled");
	}
	return ret;
    }
    
    /** Background worker method that child classes should implement */
    abstract protected T doWorkInBackground() throws Exception;

    /** Process {@link #publish} events from worker thread.
     * <p>
     * {@link #process(String)} is called for each string.
     * At the end, the wizard is refreshed so that the document
     * can be parsed again with updated contents.
     */
    @Override
    protected void process(List<String> keys) {
	// process messages
	for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
	    process(it.next());
	}
	// update content pane
	page.getWizard().refresh();
    }

    /**
     * Process a single message from worker thread.
     * <p>
     * Child classes can override this to act on {@link #publish} events from
     * the worker thread. The argument to {@linkplain #publish} is the argument
     * to this method. Make sure to call this parent method at the end, so that
     * the following events are handled properly:
     * <p>
     * When the event <tt>state.cancelled</tt> is published, the wizard will go
     * back one step. Only when an exception has occured, the (volatile)
     * property <tt>wizard.error</tt> will be set to the error message and the
     * exception will be logged. This allows one to handle errors in the
     * template itself.
     * <p>
     * When the thread is finished, the event <tt>state.done</tt> is published.
     */
    protected void process(String key) {
	if (key==null) return;
	// process cancel
	if (key.equals("state.cancelled")) {
	    // if user cancelled, go back one step
	    if (e==null || PasswordCache.isPasswordCancelledException(e)) {
		page.getWizard().setPageRelative(-1);

	    } else {
		// show error message in pane
		ErrorMessage.logException(e);
		if (e.getLocalizedMessage()!=null && !e.getLocalizedMessage().equals("") && !e.getLocalizedMessage().equals("null"))
		    page.data().setProperty("wizard.error", e.getLocalizedMessage());
		else
		    page.data().setProperty("wizard.error", "Unknown error. Please go back and try again.");
		page.data().setProperty("wizard.error.volatile", "true");
		page.getWizard().refresh();
	    }
	    return;
	}
    }
}
