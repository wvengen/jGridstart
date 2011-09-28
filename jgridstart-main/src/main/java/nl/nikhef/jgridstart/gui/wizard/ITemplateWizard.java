package nl.nikhef.jgridstart.gui.wizard;

import java.awt.Component;

import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;

/** HTML-based wizard dialog.
 * <p>
 * This is an {@link ITemplatePanel} with next/previous buttons by which
 * the user can walk through a list of pages.
 * <p>
 * Each page is implemented by a {@link TemplateWizardPage} and is registered
 * with a page id. One can use {@link #setStep} on the page id
 * or {@link #setStepRelative} to change the current page.
 */
public interface ITemplateWizard extends ITemplatePanel {

    /** Change wizard page
     * <p>
     * This fails if the exitpage or enterpage handlers veto this
     * (by returning {@literal false}), or if the page is not
     * present. In these cases, {@literal false} is returned.
     * <p>
     * If the argument is {@literal null}, the first page is shown.
     *
     * @param id identifier of step to show, or {@literal null} for first page
     * @return whether the step was shown or no
     */
    public boolean setPage(String id);
    
    /** Sets the page to the first that is not done.
     * <p>
     * Each page is queried, starting from the first one, and if
     * {@link TemplateWizardPage#isDone isDone} returns {@literal false},
     * that page is shown.
     * <p>
     * If all pages are done, the last one is displayed.
     */
    public boolean setPageDetect();

    /** go to another page by relative distance */
    public boolean setPageRelative(int delta);
    
    /** Button: next @see #setButtonEnabled */
    static public final int BUTTON_PREV = 1;
    static public final int BUTTON_NEXT = 2;
    static public final int BUTTON_CLOSE = 3;
    
    /** Make button enabled or disabled
     * 
     * @param button one of {@linkplain #BUTTON_PREV}, {@linkplain #BUTTON_NEXT}
     *          or {@linkplain #BUTTON_CLOSE}.
     * @param enabled whether to enable the button or no
     */
    public void setButtonEnabled(int button, boolean enabled);
    
    /** Return if button is enabled or not */
    public boolean getButtonEnabled(int button);
    
    /** Indicate whether closing the wizard leaves system changed or not.
     * <p>
     * This affects the close/cancel button. If closing the wizard leaves
     * the user unaffected, the button should be named "Cancel". If the
     * wizard has changed anything on the system, it should be named "Close".
     * <p>
     * For example, when closing the wizard reverts back all changes that
     * were done, it should definitely be called "Cancel". 
     */
    public void setSystemAffected(boolean affected);
    
    /** Add a page to the wizard. */
    public void addPage(ITemplateWizardPage page);
    
    /** Workaround for the absence of an IContainer class */
    public void setVisible(boolean show);
    
    /** Returns the request wizard this page is part of, or {@literal null} if none (yet) */
    public ITemplateWizard getWizard();
    
    /** Returns the parent window the wizard is (part of) */
    public Component getWindow();
}
