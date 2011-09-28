package nl.nikhef.jgridstart.gui.wizard;

/** Page of an {@link IRequestWizard}.
 * <p>
 * This interface is present to be able to get the {@linkplain IRequestWizard}
 * and retrieve specific request details from that.
 * 
 * @author wvengen
 */
public interface IRequestWizardPage extends ITemplateWizardPage {
    /** Returns the request wizard this page is part of, or {@literal null} if none (yet) */
    public IRequestWizard getWizard();
}
