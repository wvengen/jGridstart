package nl.nikhef.jgridstart.gui;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.print.PrinterException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.jdesktop.swingworker.SwingWorker;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateRequest;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.Organisation;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.TemplateWizard;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;

/** Wizard that asks the user for information and generates the certificate */
public class RequestWizard extends TemplateWizard implements TemplateWizard.PageListener {

    /** CertificateStore to operate on */
    protected CertificateStore store = null;
    /** CertificateSelection to select newly requested certificate*/
    protected CertificateSelection selection = null;
    /** the resulting CertificatePair, or null if not yet set */
    protected CertificatePair cert = null;
    /** working thread */
    protected SwingWorker<Void, String> worker = null;

    public RequestWizard(Frame parent, CertificateStore store, CertificateSelection sel) {
	super(parent);
	this.store = store;
	this.selection = sel;
    }
    public RequestWizard(Dialog parent, CertificateStore store, CertificateSelection sel) {
	super(parent);
	this.store = store;
	this.selection = sel;
    }
    @Override
    protected void initialize() {
	super.initialize();
	setPreferredSize(new Dimension(600, 400)); // fit form in step 4
	pages.add(getClass().getResource("certificate_request_01.html"));
	pages.add(getClass().getResource("certificate_request_02.html"));
	pages.add(getClass().getResource("certificate_request_03.html"));
	pages.add(getClass().getResource("certificate_request_04.html"));
	setHandler(this);
    }

    /** called when page in wizard was changed */
    public void pageChanged(TemplateWizard w, int page) {
	// stop worker on page change when needed
	if (worker!=null) {
	    worker.cancel(true);
	    worker = null;
	}
	if (page==2) {
	    // set data from organisation selection
	    Organisation org = Organisation.get(data().getProperty("org"));
	    org.copyTo(data(), "org.");
	    data().setProperty("ra.address", org.getAddress());
	    data().setProperty("ra.address.volatile", "true");
	    // on page two we need to execute the things
	    worker = new GenerateWorker(w);
	    worker.execute();
	    // go next only when all actions are finished
	    nextAction.setEnabled(false);
	}
	if (page==3) {
	    // print form; enable close button after print dialog
	    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    cancelAction.setEnabled(Boolean.valueOf(data().getProperty("state.cancontinue")));
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    try {
			print();
			cancelAction.setEnabled(true);
			setCursor(Cursor.getDefaultCursor());
		    } catch (PrinterException e) {
			ErrorMessage.error(getParent(), "Printing failed", e);
		    }
		}
	    });
	}
	if (page==4) {
	    // quit wizard
	    setVisible(false);
	    dispose();
	}
    }

    /** worker thread for generation of a certificate */
    protected class GenerateWorker extends SwingWorker<Void, String> {
	/** gui element to refresh on update; don't use in worker thread! */
	protected TemplateWizard w;

	public GenerateWorker(TemplateWizard w) {
	    super();
	    this.w = w;
	}

	/** worker thread that generates the certificate, etc. */
	@Override
	protected Void doInBackground() throws Exception {
	    // Generate a keypair and certificate signing request
	    // TODO make this configurable
	    // TODO check w.data() can safely be accessed in this thread
	    // TODO check error handling
	    try {
		if (cert==null) {
			// generate request
		    CertificateRequest.postFillData(w.data());
		    CertificatePair newCert = store.generateRequest(w.data());
		    // copy properties to certificate pair
		    for (Iterator<Map.Entry<Object, Object>> it =
			w.data().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Object, Object> e = it.next();
			newCert.put(e.getKey(), e.getValue());
		    }
		    newCert.store();
		    // TODO check if cert can safely be set in this thread
		    cert = newCert;
		    setData(cert);
		    // now that request has been generated, lock fields
		    // used for that since they are in the request now
		    publish("org.lock");
		    publish("level.lock");
		    publish("givenname.lock");
		    publish("surname.lock");
		    // update gui
		    publish("state.keypair");
		    publish("state.gencsr");
		    // TODO only upload if not yet done
		    cert.uploadRequest();
		    publish("state.submitcsr");
		}
		publish("state.cancontinue");
	    } catch (PasswordCancelledException e) {
		// special state to go to the previous page
		publish("state.cancelled");
	    }
	    return null;
	}

	/** process publish() event from worker thread. This updates the
	 * gui in the sense that a property is added to the TemplateWizard
	 * and it is refreshed. This gives a template the opportunity to
	 * change the display based on a property (e.g. a checkbox) */
	protected void process(List<String> keys) {
	    // update content pane
	    for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
		String key = it.next();
		// process cancel
		if (key.equals("state.cancelled")) {
		    setStepRelative(-1);
		    return;
		}
		w.data().setProperty(key, "true");
		// update next button
		if (key.equals("state.cancontinue"))
		    nextAction.setEnabled(true);
		// select certificate when created or update selection
		if (cert!=null) {
		    // forcibly update for other changes
		    // TODO create change listener for main window to catch refreshes and just refresh
		    if (selection!=null) {
			int index = store.indexOf(cert);
			selection.clearSelection();
			selection.setSelection(index);
		    }
		}
	    }
	    w.refresh();
	}
    }
}