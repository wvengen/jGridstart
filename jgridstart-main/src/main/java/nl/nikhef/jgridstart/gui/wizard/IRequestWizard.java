package nl.nikhef.jgridstart.gui.wizard;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.CertificateSelection;

/** Wizard for requesting and installing certificates.
 * <p>
 * This wizard is an {@linkplain ITemplateWizard} with a couple of extra
 * methods to access the certificate this wizard is working on.
 */
public interface IRequestWizard extends ITemplateWizard {
    
    /** Retrieve certificate this wizard is working on.
     * <p>
     * Can be {@literal null} if the request still has to be
     * generated.
     */
    public CertificatePair getCertificate();
    
    /** Set the certificate for this wizard.
     * <p>
     * This method must only be used when no certificate has
     * been set before. The only use case is when a new certificate
     * is generated, and needs to be bound to the wizard.
     * <p>
     * Be careful, don't use this otherwise without proper thought!
     */
    public void setCertificate(CertificatePair cert);
    
    /** Retrieve the parent certificate for this certificate.
     * <p>
     * This only makes sense if the certificate is being renewed.
     * Any existing request returns {@literal null}.
     */
    public CertificatePair getParentCertificate();
    
    /** Retrieve the certificate store.
     * <p>
     * This can be used to generate a new certificate.
     */
    public CertificateStore getStore();
    
    /** Retrieve the certificate selection, if any. */
    public CertificateSelection getSelection();

    
    /** Returns the request wizard this page is part of, or {@literal null} if none (yet) */
    public IRequestWizard getWizard();
}