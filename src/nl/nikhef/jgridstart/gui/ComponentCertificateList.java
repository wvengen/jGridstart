package nl.nikhef.jgridstart.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.bouncycastle.asn1.x509.X509Name;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;

/**
 * Java Swing component that contains a list of certificates. It is a view for
 * the CertificateStore class and optionally interfaces with a
 * CertificateSelection.
 * 
 * @author wvengen
 */
public class ComponentCertificateList extends JList {

    protected CertificateSelection selection = null;
    
    public ComponentCertificateList() {
	super();
	initialize();
    }
    public ComponentCertificateList(CertificateStore store, CertificateSelection selection) {
	super();
	initialize();
	setModel(store);
	setSelectionModel(selection);
	setCellRenderer(new CertificateCellRenderer());
    }

    /**
     * Initialize this component, create its gui. Should be called only once.
     */
    protected void initialize() {
	setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
    }
    
    class CertificateCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus) {
	    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    if (value==null) return this;
	    // we want a more rich markup from the object
	    CertificatePair cert = (CertificatePair)value;
	    String s = "";
	    // TODO icon showing certificate state
	    // name of person
	    s += cert.getSubjectPrincipalValue(X509Name.CN);
	    CertificatePair dflCert = ((CertificateStore)getModel()).getDefault();
	    // add star to default certificate
	    if ( cert.equals(dflCert) ) {
		// TODO get default certificate from store (not yet implemented)
		s += "<b color='#ffcc00'>&#x2730</b>";
	    }	
	    // organisations
	    if (cert.getSubjectPrincipalValue(X509Name.CN)!=null) {
		s += "<br>\n&nbsp;<i style='font-size:80%;'>"+
			cert.getSubjectPrincipalValue(X509Name.O)+"</i>";
	    }
	    // set html contents
	    setText("<html><body width='100%' style='margin: 2pt;'>"+s+"</html></body>");
	    return this;
	}
    }
}
