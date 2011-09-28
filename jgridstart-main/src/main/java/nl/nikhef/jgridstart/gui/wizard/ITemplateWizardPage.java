package nl.nikhef.jgridstart.gui.wizard;

import java.awt.Component;

import nl.nikhef.xhtmlrenderer.swing.ITemplateDocument;
import nl.nikhef.xhtmlrenderer.swing.TemplateDocument;

/** Page of a HTML-based wizard dialog.
 * <p>
 * This is a {@link TemplateDocument} embedded in a {@link TemplateWizard}.
 */
public interface ITemplateWizardPage extends ITemplateDocument {
    
    /** Return the identifier of this page.
     * <p>
     * The identifier is used internally as a way to reference
     * this page, e.g. to select a certain wizard page.
     * <p>
     * The default implementation returns the id as specified via the
     * constructor.
     */
    public String getId();

    /** Return title of this page.
     * <p>
     * This assumes that only one {@literal title} tag is present in the whole xml document.
     * If no {@literal title} tag is present at all,  {@code null} is returned. 
     */
    public String getTitle();
    
    /** Return title of this page used in the contents.
     * <p>
     * This is generally used in the list of steps in a wizard. Defaults to
     * {@linkplain #getTitle}, but can be customized in child classes.
     * 
     * @see #getTitle
     */
    public String getContentsTitle();
    
    /** Return whether this page is considered done or not.
     * <p>
     * If a certain page doesn't need to be executed, it is a good
     * idea to add a checkmark in front of the contents list. This 
     * method should return {@literal true} if this step needs to
     * action from the user anymore.
     * <p>
     * You can safely ignore this if you don't want the functionality.
     */
    public boolean isDone();
    
    /** Callback when leaving a page.
     * <p>
     * Default implementation returns {@link #validate}. If that throws
     * a {@link ValidationException} which a non-{@literal null} message,
     * an error dialog is shown and {@literal false} is returned.
     * 
     * @see #validate
     * @param newPage new page that will be shown
     * @param isNext is this is a result of a "Next" action
     * @return {@literal false} to stay on the current page
     */
    public boolean pageLeave(ITemplateWizardPage newPage, boolean isNext);
    
    /** Callback when entering a page.
     * <p>
     * Default implementation does nothing.
     * 
     * @param oldPage old page that was shown, or {@literal null} if this
     *          is the first page shown.
     */
    public void pageEnter(ITemplateWizardPage oldPage);
    
    /** Validate page contents.
     * <p>
     * This is called by default when a page is left. This method should
     * throw a {@linkplain ValidationException} if the contents of this
     * wizard page cannot be validation (this would generally be user input).
     * If the exception has a message that is not {@literal null}, it will
     * be shown to the user in a dialog box. If the exception message has
     * been set explicitely to {@literal null}, no message is shown but
     * {@linkplain #pageLeave} still returns {@literal false} so that the
     * user remains on the current page; this is to handle error handling
     * in the page itself.
     *
     * @throws ValidationException if validation did not succeed
     */
    public void validate() throws ValidationException;
    
    /** Returns the wizard this page is part of, or {@literal null} if none (yet) */
    public ITemplateWizard getWizard();
    
    /** Returns the parent window this wizard is (part of) */
    public Component getParent();
    
    /** Sets parent of this page.
     * <p>
     * This should only be done by the wizard itself, an
     * {@link ITemplateWizard}.
     * 
     * @see #getWizard
     * @see #getParent
     */
    public void associate(ITemplateWizard wizard, Component parent);
}
