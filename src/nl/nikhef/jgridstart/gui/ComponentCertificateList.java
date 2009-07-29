package nl.nikhef.jgridstart.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.bouncycastle.asn1.x509.X509Name;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.Organisation;

/** List of certificates.
 * <p>
 * Java Swing component that contains a list of certificates. It is a view for
 * {@link CertificateStore} and optionally interfaces with a
 * {@link CertificateSelection}.
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
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus) {
	    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    if (value==null) return this;
	    // we want a more rich markup from the object
	    CertificatePair cert = (CertificatePair)value;
	    String line1 = "", line2 = "";
	    String dfl = "";
	    // name of person
	    line2 += cert.getSubjectPrincipalValue(X509Name.CN);
	    // add star to default certificate
	    CertificatePair dflCert = ((CertificateStore)getModel()).getDefault();
	    if ( cert.equals(dflCert) )
		dfl += "&nbsp;<b color='#ffcc00'>&#x2730</b>";
	    // organisation
	    Organisation org = Organisation.getFromCertificate(cert);
	    if (org!=null) line1 += org.getProperty("name.full"); // TODO full name, incl. O if OU
	    // set html contents
	    String s =
		"<html><body width='100%'>" +
	    	"<table border='0' cellpadding='2' cellspacing='0'>" +
	    	"  <tr>" +
	    	"    <td width='19' rowspan='2' align='center'>" + cert.getProperty("state.icon.html") + "</td>" +
	    	"    <td>" +
	    	       line1 + dfl + "<br>" +
	    	"      <small>" + line2 + "</small>" +
	    	"    </td>" +
	    	"  </tr>" +
	    	"</html></body>";
	    setText(s);
	    return this;
	}
    }
}
