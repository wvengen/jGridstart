package nl.nikhef.jgridstart.gui;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;

/** This class provides the default implementation for Actions that operate
 * on a Certificate. It provides a default logger and keeps track of the
 * currently selected certificate which can be retrieved by getCertificate().
 * 
 * The action has to be bound to a CertificateSelection by the caller of the
 * CertificateAction if you want the certificate to be updated when the
 * selection changes.
 * 
 * @author wvengen
 */
public abstract class CertificateAction extends AbstractAction implements ListSelectionListener {
    static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");

    protected CertificatePair certificatePair = null;
    protected CertificateSelection selection = null;
    protected JFrame parent = null;

    public CertificateAction(JFrame parent, CertificateSelection s) {
	super();
	this.parent = parent;
	this.selection = s;
	if (s!=null) s.addListSelectionListener(this);
    }
    public CertificateAction(JFrame parent) { this(parent, null); }
    
    /** Set the certificate on which the action will be performed */
    public void setCertificatePair(CertificatePair cert) {
	certificatePair = cert;
    }

    /** Catch it when the certificate selection is changed */
    public void valueChanged(ListSelectionEvent e) {
	certificatePair = selection.getCertificatePair();
    }
    
    /** Get the certificate to operate on. To be used by child classes. */
    protected CertificatePair getCertificatePair() { return certificatePair; }
    
    /** The actual handler when the action is performed */
    abstract public void actionPerformed(ActionEvent e);
}
