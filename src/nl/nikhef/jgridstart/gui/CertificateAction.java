package nl.nikhef.jgridstart.gui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;

/** Default implementation for {@link Action}s that operate on a {@link CertificatePair}.
 * <p>
 * It provides a default logger and keeps track of the currently selected
 * certificate which can be retrieved by {#linkplain #getCertificate}.
 * <p>
 * The action has to be bound to a {@link CertificateSelection} by the caller of the
 * {@linkplain CertificateAction} if you want the certificate to be updated when the
 * selection changes.
 * 
 * @author wvengen
 */
public abstract class CertificateAction extends AbstractAction implements ListSelectionListener, ItemListener {
    static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui");

    protected CertificatePair certificatePair = null;
    protected CertificateSelection selection = null;
    protected JFrame parent = null;
    protected boolean isWantEnabled = true;

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
    
    /** Returns whether this action should be enabled or not.
     * <p>
     * This is called when the selection changes and updates the enabled-state.
     * It is not possible to just override the {@linkplain #isEnabled} method
     * Swing gets confused about the enabled-ness of connected components
     * when {@linkplain #isEnabled} is modified.
     * 
     * By default it just returns isEnabled(), so the behaviour is
     * unchanged from an ordinary Action. */
    protected boolean wantsEnabled() {
	return isWantEnabled;
    }
    
    /** Sets the enabled state of this {@linkplain CertificateAction}.
     * <p>
     * Note that when no {@linkplain CertificatePair} is currently selected,
     * the action is always disabled.
     * TODO explain better
     * 
     * @see AbstractAction#setEnabled */
    @Override
    public void setEnabled(boolean e) {
	isWantEnabled = e;
	super.setEnabled(certificatePair!=null && e);
    }

    /** Catch it when the certificate selection is changed */
    public void valueChanged(ListSelectionEvent e) {
	// only operate at the end of a stream of selection events to avoid flickering
	if (e.getValueIsAdjusting()) return;
	// remove itemlistener for previous pair
	if (certificatePair!=null) certificatePair.removeItemListener(this);
	certificatePair = selection.getCertificatePair();
	certificatePair.addItemListener(this);
	super.setEnabled(certificatePair!=null && wantsEnabled());
    }
    /** Catch it when the certificate itself is changed, needed for
     * {@linkplain #wantsEnabled} depending on the certificate state. */
    public void itemStateChanged(ItemEvent e) {
	super.setEnabled(certificatePair!=null && wantsEnabled());
    }
    
    /** Get the certificate to operate on. To be used by child classes. */
    protected CertificatePair getCertificatePair() { return certificatePair; }
    
    /** The actual handler when the action is performed */
    abstract public void actionPerformed(ActionEvent e);
    
    /** Find a {@linkplain Window} given a contained {@linkplain Component}.
     * 
     * @return the window, or null if not found
     */
    public static Window findWindow(Component c) {
	while (c!=null) {
	    if (c instanceof Window) return (Window)c;
	    if (c instanceof JPopupMenu)
		c = ((JPopupMenu)c).getInvoker();
	    else
		c = c.getParent();
	}
	return null;
    }
    /** Find a {@linkplain Window} given a contained {@linkplain Component}.
     * <p>
     * This accepts an {@linkplain Object} as argument, and if that is no
     * {@linkplain Component}, {@code null} is returned.
     * 
     * @return the window, or null if not found
     */
    public static Window findWindow(Object c) {
	if (c instanceof Component)
	    return findWindow((Component)c);
	return null;
    }
}
