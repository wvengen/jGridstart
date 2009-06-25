package nl.nikhef.xhtmlrenderer.swing;

import java.util.Properties;
import javax.swing.Action;

/** {@link TemplatePanel} methods related to the {@link TemplateDocument}. */
public interface ITemplateContainer extends IDocumentContainer {
    
    /** Set the properties to use for the template and form elements.
     * <p>
     * Also reparses the document and updates the panel, if a template was set.
     * 
     * @param p New properties to set 
     */
    public void setData(Properties p);
    
    /** Return the properties.
     *<p>
     * Use this to retrieve data from form elements. The name of each
     * element has a corresponding property key, and the value of the property
     * is updated when the form element changes.
     */
    public Properties data();
    
    /** Refresh the contents so that all parsing is redone.
     * <p>
     * Note that the template may or may not be retrieved again from its source.
     * <p>
     * This happens automatically in {@link #setData} and {@link #setDocument}.
     * 
     * @return true on success, false if no document URI is available
     */
    public boolean refresh();
    
    /** Set the action to perform on form submission.
     * <p>
     * If this is set to null, the standard behaviour is done: posting data
     * to the url supplied by the form.
     */
    public void setSubmitAction(Action e);
}
