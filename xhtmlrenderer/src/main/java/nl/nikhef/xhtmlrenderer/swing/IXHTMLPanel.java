package nl.nikhef.xhtmlrenderer.swing;

import java.awt.event.ComponentListener;
import java.awt.print.PrinterException;
import java.util.List;

import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;

/** {@link XHTMLPanel} interface
 * <p>
 * TODO add remaining methods. */
public interface IXHTMLPanel extends IDocumentContainer, FormSubmissionListener, ComponentListener, UserInterface {
    /**
     * Sets the {@link RenderingContext} attribute of the XHTMLPanel object. Generally
     * you should not use this unless you have a heavily customized context to
     * use. To modify just some rendering behavior, consider using
     * {@link #getSharedContext()} to retrieve the current context, and using
     * mutators to change its behavior.
     *
     * @param ctx A new RenderingContext to use for rendering.
     */
    public void setSharedContext(SharedContext ctx);

    public SharedContext getSharedContext();    
    
    public void addMouseTrackingListener(FSMouseListener l);
    public void removeMouseTrackingListener(FSMouseListener l);
    @SuppressWarnings("rawtypes")
    public List getMouseTrackingListeners();

    public void setFormSubmissionListener(FormSubmissionListener fsl);
    
    /* === additions to standard xhtmlrenderer === */
    
    /** Replace the listener that is activated when a link is clicked.
     * <p>
     * This is a convenience method that first removes all LinkListeners, and
     * then adds the supplied one.
     * 
     * @param llnew New LinkListener to use for this panel
     */
    public void replaceLinkListener(LinkListener llnew);

    /** print the contents of this pane.
     * <p>
     * A print dialog is shown first.
     * 
     * @throws PrinterException
     */
    public boolean print() throws PrinterException;
}
