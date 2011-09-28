package nl.nikhef.xhtmlrenderer.swing;

import java.util.Properties;

import org.w3c.dom.Document;

/** A Document that is based on a template.
 * 
 * @see TemplateDocument
 * @author wvengen
 */
public interface ITemplateDocument extends Document {
    
    /** Set the properties to use for the template.
     * 
     * @param p New properties to set 
     */
    public void setData(Properties p);
    
    /** Return the properties. */
    public Properties data();
    
    /** Rebuild the document from its template and properties */
    public void refresh();    
}
