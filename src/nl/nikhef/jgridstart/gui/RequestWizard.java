package nl.nikhef.jgridstart.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingworker.SwingWorker;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.LinkListener;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateRequest;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.Organisation;
import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.TemplateButtonPanel;
import nl.nikhef.jgridstart.gui.util.TemplateWizard;
import nl.nikhef.jgridstart.gui.util.FileFilterSuffix;
import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;

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
	setData(new Properties());
    }
    /** New certificate request */
    public RequestWizard(Dialog parent, CertificateStore store, CertificateSelection sel) {
	super(parent);
	this.store = store;
	this.selection = sel;
	setData(new Properties());
    }
    /** View form of existing CertificatePair */
    public RequestWizard(Frame parent, CertificatePair cert, CertificateSelection sel) {
	super(parent);
	this.cert = cert;
	this.selection = sel;
	setData(cert);
    }
    /** View form of existing CertificatePair */
    public RequestWizard(Dialog parent, CertificatePair cert, CertificateSelection sel) {
	super(parent);
	this.cert = cert;
	this.selection = sel;
	setData(cert);
    }
    @Override
    protected void initialize() {
	super.initialize();
	setPreferredSize(new Dimension(785, 500));
	// add the html pages
	pages.add(getClass().getResource("requestwizard-01.html"));
	pages.add(getClass().getResource("requestwizard-02.html"));
	pages.add(getClass().getResource("requestwizard-03.html"));
	pages.add(getClass().getResource("requestwizard-04.html"));
	setHandler(this);
	// extra special handling of "action:" links
	replaceLinkListener(new LinkListener() {
	    @Override
            public void linkClicked(BasicPanel panel, String uri) {
		// handle help toggle buttons, not a regular action
		if (uri.startsWith("action:toggle(")) {
		    String var = uri.substring(14,uri.length()-1);
		    boolean val = Boolean.valueOf(data().getProperty(var));
		    data().setProperty(var, Boolean.toString(!val));
		    data().setProperty(var+".volatile", "true");
		    refresh();
		    return;
		}
		// select certificate so that any "action:" links are executed
		// on the correct certificate.
		if (uri.startsWith("action:"))
		    selection.setSelection(cert);
		BareBonesActionLaunch.openURL(uri, panel);
	    }	    
	});
    }
    @Override
    public void setData(Properties p) {
	super.setData(p);
	// help the user by prefilling some elements
	CertificateRequest.preFillData(p);
	// set static properties for the forms
	if (cert==null)
	    data().setProperty("wizard.title.html", "Request a new certificate");
	else
	    data().setProperty("wizard.title.html", "Certificate Request");
	data().setProperty("wizard.title.html.volatile", "true");
	data().setProperty("organisations.html.options", Organisation.getAllOptionsHTML(cert));
	data().setProperty("organisations.html.options.volatile", "true");
	// workaround for checkboxes without a name; even with checked="checked" they
	// would sometimes not be shown as checked (irregular behaviour though)
	data().setProperty("true", "true");
	data().setProperty("true.volatile", "true");
	// make sure to keep the password safe
	data().setProperty("password1.volatile", "true");
	data().setProperty("password2.volatile", "true");
    }
    
    /** called before a page in wizard is changed */
    public boolean pageLeave(TemplateWizard w, int curPage, int newPage) {
	if (newPage==0) {
	    // lock fields that generate DN
	    if (cert!=null)
		CertificateRequest.postFillDataLock(data());
	}
	
	if (curPage==0 && cert==null) {
	    // make sure passwords are equal
	    if (data().getProperty("password1")!=null &&
		    !data().getProperty("password1").equals(data().getProperty("password2"))) {
		JOptionPane.showMessageDialog(this,
			"Passwords don't match, please make sure they are equal.",
			"Passwords don't match", JOptionPane.ERROR_MESSAGE);
		return false;
	    }
	    // and a level was chosen
	    if (data().getProperty("level")==null) {
		JOptionPane.showMessageDialog(this,
			"Please select a certification level",
			"Missing data", JOptionPane.ERROR_MESSAGE);
		return false;
	    }
	}
	
	// set organisation info if not present
	if (data().getProperty("org.ra.name")==null) {
	    Organisation org = Organisation.get(data().getProperty("org"));
	    if (org!=null)
		org.copyTo(data(), "org.");
	}
	
	// ok!
	return true;
    }

    /** called when page in wizard is changed */
    public void pageEnter(TemplateWizard w, int oldPage, int curPage) {
	// stop worker on page change when needed
	if (worker!=null) {
	    worker.cancel(true);
	    worker = null;
	}
	
	// say "Close" when a certificate is present because everything is done by then
	try {
	    if (cert!=null && cert.getCertificate()!=null)
		cancelAction.putValue(AbstractAction.NAME, "Close");
	} catch (IOException e) { }
	
	if (curPage==1 || curPage==2) {
	    // on page two we need to execute the things
	    worker = new GenerateWorker(w, curPage);
	    worker.execute();
	    // go next only when all actions are finished
	    nextAction.setEnabled(false);
	}

	// redraw buttons
	btnLeft.revalidate();
	btnLeft.repaint();
    }

    /** {@inheritDoc}
     * <p>
     * This method adds a css class done when a step is finished
     * for the state of the certificate.
     */
    @Override
    protected String getWizardContentsLine(int step, int current) {
	String classes = "";
	// standard classes from parent
	if (step==current) classes += " wizard-current";
	if (step>current)  classes += " wizard-future";
	// step 0: done if CertificatePair present
	if (step==0 && cert!=null) classes += " wizard-done";
	// step 1: done if request submitted or certificate present
	if (step==1 && cert!=null &&
		(Boolean.valueOf(cert.getProperty("request.submitted")) ||
		 Boolean.valueOf(cert.getProperty("cert"))) )
	    classes += " wizard-done";
	// step 2: done if certificate present
	if (step==2 && cert!=null && Boolean.valueOf(cert.getProperty("cert")))
	    classes += " wizard-done";
	// step 3: done if certificate installed previously
	if (step==3 && cert!=null && cert.getProperty("cert.installed")!=null)
	    classes += " wizard-done";
	return (classes=="" ? "<li>" : "<li class='"+classes+"'>") + getDocumentTitle(step) + "</li>\n";
    }

    /** worker thread for generation of a certificate */
    protected class GenerateWorker extends SwingWorker<Void, String> {
	/** gui element to refresh on update; don't use in worker thread! */
	protected TemplateWizard w;
	/** exception from background thread */
	protected Exception e = null;
	/** current step */
	protected int step = -1;

	public GenerateWorker(TemplateWizard w, int step) {
	    super();
	    this.w = w;
	    this.step = step;
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
		    CertificatePair newCert = store.generateRequest(w.data(), w.data().getProperty("password1").toCharArray());
		    // clear password
		    w.data().remove("password1");
		    w.data().remove("password1.volatile");
		    w.data().remove("password2");
		    w.data().remove("password2.volatile");
		    // copy properties to certificate pair
		    for (Iterator<Map.Entry<Object, Object>> it =
			w.data().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Object, Object> e = it.next();
			newCert.put(e.getKey(), e.getValue());
		    }
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
		    }
		}
		// make sure gui is updated and user can continue
		if (step==1) publish("state.cancontinue");
		// update downloadable status
		if (!Boolean.valueOf(cert.getProperty("request.processed"))) {
		    cert.isCertificationRequestProcessed();
		    publish((String)null);
		}
		// and download when needed and possible
		if (step==2 && cert.getCertificate()==null && 
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