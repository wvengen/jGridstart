package nl.nikhef.jgridstart.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.URLLauncher;
import nl.nikhef.jgridstart.install.BrowserFactory;
import nl.nikhef.jgridstart.install.IBrowsers;

/** Pops up a dialog for selecting the certificate's preferred web browser. */
public class ActionChangeBrowser extends CertificateAction {

    public ActionChangeBrowser(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Change Browser");
	putValue(MNEMONIC_KEY, new Integer('B'));
	URLLauncher.addAction("changebrowser", this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	try {
	    IBrowsers b = BrowserFactory.getInstance();
	    final String bdefault = "(default browser, " +
	    	b.getBrowserName(b.getDefaultBrowser()) + ")";
	    String bcurrent = getCertificatePair().getProperty("install.browser");
	    if (bcurrent==null) bcurrent = bdefault;
	    // populate list
	    DefaultListModel browserModel = new DefaultListModel();
	    ArrayList<String> bnames = new ArrayList<String>();
	    bnames.addAll(Arrays.asList(b.getBrowserList().toArray(new String[]{})));
	    int bcuridx = -1;
	    for (int i=0; i<bnames.size(); i++) {
		browserModel.addElement(b.getBrowserName(bnames.get(i)));
		// find index of currently selected browser
		if (bnames.get(i).equals(bcurrent)) bcuridx = i;
	    }
	    // add option for default browser
	    bnames.add(0, bdefault);
	    browserModel.add(0, bdefault);
	    bcuridx++;
	    // setup UI
	    JList browserList = new JList(browserModel);
	    browserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    browserList.setLayoutOrientation(JList.VERTICAL);
	    browserList.setSelectedIndex(bcuridx);
	    JScrollPane browserListPane = new JScrollPane(browserList);
	    browserListPane.setPreferredSize(new Dimension(250, 80));
	    int res = JOptionPane.showConfirmDialog(
		    findWindow(e.getSource()),
		    new Object[] {
		    "Select the webbrowser to use this certificate with",
		    browserListPane
	    }, "Select preferred webbrowser", JOptionPane.OK_CANCEL_OPTION);
	    // parse result
	    if (res==JOptionPane.OK_OPTION) {
		String selected = bnames.get(browserList.getSelectedIndex());
		if (selected != bdefault)
		    getCertificatePair().setProperty("install.browser", selected);
		else
		    getCertificatePair().remove("install.browser");
	    }
	} catch (IOException e1) {
	    ErrorMessage.internal(findWindow(e.getSource()), e1);
	}
    }
}
