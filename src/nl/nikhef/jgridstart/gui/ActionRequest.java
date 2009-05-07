package nl.nikhef.jgridstart.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jdesktop.swingworker.SwingWorker;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.Organisation;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.TemplateWizard;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;

public class ActionRequest extends AbstractAction {
    
    static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");
    protected JFrame parent = null;
    protected CertificateStore store = null;
    protected CertificateSelection selection = null;
    
    public ActionRequest(JFrame parent, CertificateStore store, CertificateSelection selection) {
	super();
	this.parent = parent;
	this.store = store;
	this.selection = selection;
	putValue(NAME, "Request new...");
	putValue(MNEMONIC_KEY, new Integer('R'));
	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
	BareBonesActionLaunch.addAction("request", this);
    }
    
    public void actionPerformed(ActionEvent e) {
	logger.finer("Action: "+getValue(NAME));
	TemplateWizard dlg = new RequestWizard();
	Properties p = new Properties();
	p.setProperty("organisations.html.options", Organisation.getAllOptionsHTML());
	p.setProperty("organisations.html.options.volatile", "true");
	p.setProperty("surname", "Klaassen");
	p.setProperty("givenname", "Piet");
	p.setProperty("country", "NL");
	p.setProperty("level", "medium");
	dlg.setData(p);
	dlg.setVisible(true);
    }
    
    /** Wizard that asks the user for information and generates the certificate */
    protected class RequestWizard extends TemplateWizard implements TemplateWizard.PageListener {
	
	/** the resulting CertificatePair, or null if not yet set */
	protected CertificatePair cert = null;
	/** working thread */
	protected SwingWorker<Void, String> worker = null;
	
	public RequestWizard() {
	    super(parent);
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
			    // TODO Auto-generated catch block
			    e.printStackTrace();
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
			Properties p = new Properties(w.data());
			// construct subject
			String subject = "";
			if (p.getProperty("level").equals("tutorial"))
			    subject += "O=edgtutorial";
			else
			    subject += "O=dutchgrid";
			if (p.getProperty("level").equals("demo"))
			    subject += ", O=dutch-demo";
			subject += ", O=users";
			subject += ", CN=" + p.getProperty("givenname").trim() +
				   " " + p.getProperty("surname").trim();
			p.setProperty("subject", subject);
			// generate request
			CertificatePair newCert = store.generateRequest(p);
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
			int index = store.indexOf(cert);
			selection.clearSelection();
			selection.setSelection(index);
		    }
		}
		w.refresh();
	    }
	}
    }
}
