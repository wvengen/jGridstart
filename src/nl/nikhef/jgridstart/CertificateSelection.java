package nl.nikhef.jgridstart;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

public class CertificateSelection extends DefaultListSelectionModel {
    protected CertificateStore store = null;

    public CertificateSelection(CertificateStore store) {
	super();
	this.store = store;
	super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    /** Set the current selection by index */
    public void setSelection(int index) {
	setSelectionInterval(index, index);
    }
    /** Set the current selection by object */
    public void setSelection(CertificatePair c) {
	setSelection(store.indexOf(c));
    }
    
    /** Retrieve the currently selected index, or -1 if none */
    public int getIndex() {
	return getMinSelectionIndex();
    }
    /** Retrieve the currently selected Certificate, or null if none */
    public CertificatePair getCertificatePair() {
	int i = getIndex();
	if (i<0) return null;
	return store.get(i);
    }
}
