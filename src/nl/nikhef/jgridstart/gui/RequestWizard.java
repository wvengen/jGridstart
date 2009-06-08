package nl.nikhef.jgridstart.gui;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.print.PrinterException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
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

    /** New certificate request */
    public RequestWizard(Frame parent, CertificateStore store, CertificateSelection sel) {
	super(parent);
	this.store = store;
	this.selection = sel;
    }
    /** New certificate request */
    public RequestWizard(Dialog parent, CertificateStore store, CertificateSelection sel) {
	super(parent);
	this.store = store;
	this.selection = sel;
    }
    /** View form of existing CertificatePair */
    public RequestWizard(Frame parent, CertificatePair cert) {
	super(parent);
	this.cert = cert;
	setData(cert);
    }
    /** View form of existing CertificatePair */
    public RequestWizard(Dialog parent, CertificatePair cert) {
	super(parent);
	this.cert = cert;
	setData(cert);
    }
    @Override
    protected void initialize() {
	super.initialize();
	setPreferredSize(new Dimension(650, 450)); // fit form in step 4
	pages.add(getClass().getResource("certificate_request_01.html"));
	pages.add(getClass().getResource("certificate_request_02.html"));
	pages.add(getClass().getResource("certificate_request_03.html"));
	pages.add(getClass().getResource("certificate_request_04.html"));
	setHandler(this);
    }
    @Override
    public void setData(Properties p) {
	super.setData(p);
	// set static properties for the forms
	data().setProperty("organisations.html.options", Organisation.getAllOptionsHTML(cert));
	data().setProperty("organisations.html.options.volatile", "true");
	// workaround for checkboxes without a name; even with checked="checked" they
	// would sometimes not be shown as checked (irregular behaviour though)
	data().setProperty("true", "true");
	data().setProperty("true.volatile", "true");
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
	    if (org!=null) {
		org.copyTo(data(), "org.");
		data().setProperty("ra.address", org.getAddress());
		data().setProperty("ra.address.volatile", "true");
	    }
	    // on page two we need to execute the things
	    worker = new GenerateWorker(w);
	    worker.execute();
	    // go next only when all actions are finished
	    nextAction.setEnabled(false);
	}
	if (page==3) {
	    /*
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
	    */
	}
	if (page==4) {
	    // quit wizard
	    setVisible(false);
	    dispose();
	}
	// say "Close" when a certificate is present because everything is done by then
	cancelAction.putValue(AbstractAction.NAME, "Close");
    }

    /** worker thread for generation of a certificate */
    protected class GenerateWorker extends SwingWorker<Void, String> {
	/** gui element to refresh on update; don't use in worker thread! */
	protected TemplateWizard w;
	/** exception from background thread */
	protected Exception e = null;

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
		// generate request when no key or certificate
		if (cert==null) {
		    CertificateRequest.postFillData(w.data());
		    CertificatePair newCert = store.generateRequest(w.data());
		    // copy properties to certificate pair
		    for (Iterator<Map.Entry<Object, Object>> it =
			w.data().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Object, Object> e = it.next();
			newCert.put(e.getKey(), e.getValue());
		    }
		    // now that request has been generated, lock fields
		    // used for that since they are in the request now
		    newCert.setProperty("org.lock", "true");
		    newCert.setProperty("level.lock", "true");
		    newCert.setProperty("givenname.lock", "true");
		    newCert.setProperty("surname.lock", "true");
		    // and make sure data is saved
		    newCert.store();
		    // TODO check if cert can safely be set in this thread
		    cert = newCert;
		    setData(cert);
		    publish("state.certificate_created");
		}
		// upload request only if no certificate present yet
		if (cert.getCertificate()==null) {
		    // upload request if it hasn't been done
		    if (!Boolean.valueOf(cert.getProperty("request.submitted"))) {
			cert.uploadRequest();
			publish("state.cancontinue");
		    }
		}
		// make sure gui is updated and user can continue
		publish("state.cancontinue");
		// update downloadable status
		if (!Boolean.valueOf(cert.getProperty("request.processed"))) {
		    cert.isCertificationRequestProcessed();
		    publish((String)null);
		}
		// and download when needed and possible
		if (cert.getCertificate()==null && 
			Boolean.valueOf(cert.getProperty("request.processed"))) {
		    cert.downloadCertificate();
		    publish((String)null);
		}
	    } catch (PasswordCancelledException e) {
		// special state to go to the previous page
		publish("state.cancelled");
	    } catch (Exception e) {
		// store exception so it can be shown to the user
		this.e = e;
		publish("state.cancelled");
	    }
	    return null;
	}

	/** process publish() event from worker thread. This updates the
	 * gui in the sense that a property is added to the TemplateWizard
	 * and it is refreshed. This gives a template the opportunity to
	 * change the display based on a property (e.g. a checkbox) */
	protected void process(List<String> keys) {
	    // process messages
	    for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
		String key = it.next();
		if (key==null) continue;
		// process cancel
		if (key.equals("state.cancelled")) {
		    if (e!=null)
			ErrorMessage.error(getParent(), "Error during request", e);
		    setStepRelative(-1);
		    return;
		}
		// select certificate in main view on creation
		if (key.equals("state.certificate_created")) {
		    assert(cert!=null);
		    if (selection!=null) {
			int index = store.indexOf(cert);
			selection.clearSelection();
			selection.setSelection(index);
		    }
		}
		// update next button
		if (key.equals("state.cancontinue"))
		    nextAction.setEnabled(true);
	    }
	    // update content pane
	    w.refresh();
	}
    }
}