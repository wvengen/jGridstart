package nl.nikhef.xhtmlrenderer.swing;

import java.awt.Component;
import java.util.Map;

/** Interface for {@link TemplatePanel} classes.
 * <p>
 * This is mostly a {@link IXHTMLPanel} combined with the template functionality
 * of {@link ITemplateContainer}. */
public interface ITemplatePanel extends ITemplateContainer, IXHTMLPanel {
    
    /** Return the Swing/AWT component for a form element by name.
     * <p>
     * Note that multiple radio buttons have the same name, so for these the
     * name is actually in the form {@code name.value}.
     * 
     * @param name like {@code myfield} in {@code &gt;input type="text" name="myfield" /&lt;}
     * @return The Swing/AWT component, or null if not found.
     */
    public Component getFormComponent(String name);
    
    /** Return the list of Swing/AWT components.
     * 
     * @see #getFormComponent
     * @return Readonly map of Swing/AWT components, indexed by their name.
     */
    public Map<String, Component> getFormComponents();

}
