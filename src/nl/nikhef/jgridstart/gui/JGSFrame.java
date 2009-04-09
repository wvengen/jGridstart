package nl.nikhef.jgridstart.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextPane;
import javax.swing.BorderFactory;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JScrollPane;

import nl.nikhef.jgridstart.CertificatePair;
import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.CertificateStore;
import nl.nikhef.jgridstart.gui.util.BareBonesBrowserLaunch;
import nl.nikhef.jgridstart.util.PasswordCache;

public class JGSFrame extends JFrame {

    private JPanel jContentPane = null;
    private JMenuBar jMenuBar = null;
    private JPanel jPanel = null;
    private ComponentTemplatePane certInfoPane = null;

    private CertificateStore store = null;
    private JPanel jPanel1 = null;
    private JButton jButton = null;
    private JButton jButton1 = null;
    private JButton jButton2 = null;
    private int buttonBorderWidth = 2;
    private JScrollPane jScrollPane = null;
    
    private CertificateSelection selection = null; 

    /**
     * This is the default constructor
     */
    public JGSFrame() {
	super();
	initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
	store = new CertificateStore();
	selection = new CertificateSelection(store);
	PasswordCache.getInstance().setParent(this);
	
	this.setSize(550, 350);
	this.setMinimumSize(new Dimension(400, 150));
	this.setPreferredSize(new Dimension(600, 350));
	this.setJMenuBar(getJMenuBar());
	this.setContentPane(getJContentPane());
	this.setTitle("jGridStart Mockup");

	store.load(System.getProperty("user.home")+"/.globus-test");
	// TODO handle case when no certificates were found -> show getting started page
	if (store.size() > 0)
	    selection.setSelectionInterval(0, 0);
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
	if (jContentPane == null) {
	    jContentPane = new JPanel();
	    jContentPane.setLayout(new BorderLayout());
	    jContentPane.add(getJPanel(), BorderLayout.CENTER);
	    jContentPane.add(new ComponentCertificateList(store, selection), BorderLayout.WEST);
	}
	return jContentPane;
    }

    /**
     * This method initializes jMenuBar	
     * 	
     * @return javax.swing.JMenuBar	
     */
    public JMenuBar getJMenuBar() {
	if (jMenuBar == null) {
	    JMenu menu;
	    jMenuBar = new JMenuBar();

	    // menu: Identities
	    menu = new JMenu("Certificates");
	    menu.setMnemonic('C');
	    menu.add(new JMenuItem(new ActionRequest(this, store)));
	    menu.add(new JMenuItem(new ActionImport(this, store)));
	    menu.addSeparator();
	    addIdentities(menu);
	    menu.addSeparator();
	    menu.add(new JMenuItem(new ActionQuit(this)));
	    jMenuBar.add(menu);

	    // menu: Certificate
	    menu = new JMenu("Actions");
	    menu.setMnemonic('A');
	    menu.add(new JMenuItem("Request approval...", 'R'));
	    menu.add(new JMenuItem("Install...", 'I'));
	    menu.add(new JMenuItem("Request renewal...", 'N'));
	    menu.add(new JMenuItem(new ActionRevoke(this)));
	    menu.addSeparator();
	    menu.add(new JMenuItem(new ActionExport(this, selection)));
	    menu.add(new JMenuItem("Change passphrase...", 'P'));
	    menu.add(new JMenuItem(new ActionViewLog(this)));
	    jMenuBar.add(menu);
	    menu.getItem(0).setEnabled(false);

	    // menu: View
	    menu = new JMenu("View");
	    menu.setMnemonic('W');
	    menu.add(new JCheckBoxMenuItem("Certificate list", true));
	    menu.add(new JMenuItem("Personal details...", 'P'));
	    menu.add(new JMenuItem(new ActionRefresh(this, store)));
	    jMenuBar.add(menu);
	    
	    // menu: Help
	    jMenuBar.add(Box.createHorizontalGlue());
	    menu = new JMenu("Help");
	    menu.setMnemonic('H');
	    menu.add(new JMenuItem(new ActionAbout(this)));
	    jMenuBar.add(menu);
	}
	return jMenuBar;
    }

    /** add a list of identities to the given menu
     *  
     * @param jMenu
     */
    private void addIdentities(JMenu jMenu) {
	ButtonGroup group = new ButtonGroup();
	for (int i=0; i < store.size(); i++) {
	    CertificatePair cert = store.get(i);
	    JRadioButtonMenuItem jrb = new JRadioButtonMenuItem();
	    jrb.setText(cert.toString());
	    jrb.addActionListener(
		    new ActionSelectCertificate(this, cert, selection));
	    group.add(jrb);
	    jMenu.add(jrb);
	    if (i==0) jrb.setSelected(true);
	}
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
	if (jPanel == null) {
	    jPanel = new JPanel();
	    jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.Y_AXIS));
	    jPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	    jPanel.add(getJScrollPane(), null);
	    jPanel.add(getJPanel1(), null);
	}
	return jPanel;
    }

    /**
     * This method initializes jTextPane	
     * 	
     * @return javax.swing.JTextPane	
     */
    private ComponentTemplatePane getJTextPane() {
	if (certInfoPane == null) {
	    certInfoPane = new ComponentTemplatePane(getClass().getResource("certificate_info.html"));
	    selection.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent ev) {
			CertificatePair c = selection.getCertificatePair();
			certInfoPane.setProperties(c);
		    }
	    });
	}
	return certInfoPane;
    }

    /**
     * This method initializes jPanel1	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel1() {
	if (jPanel1 == null) {
	    jPanel1 = new JPanel();
	    jPanel1.setLayout(new BoxLayout(getJPanel1(), BoxLayout.X_AXIS));
	    jPanel1.setBorder(BorderFactory.createEmptyBorder(buttonBorderWidth, buttonBorderWidth, buttonBorderWidth, buttonBorderWidth));
	    jPanel1.add(Box.createHorizontalGlue());
	    jPanel1.add(getJButton2(), null);
	    jPanel1.add(Box.createRigidArea(new Dimension(buttonBorderWidth,0)));
	    jPanel1.add(getJButton1(), null);
	    jPanel1.add(Box.createRigidArea(new Dimension(buttonBorderWidth,0)));
	    jPanel1.add(getJButton(), null);
	}
	return jPanel1;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton() {
	if (jButton == null) {
	    jButton = new JButton();
	    jButton.setText("Revoke");
	    jButton.setName("jButton");
	    jButton.setMnemonic(KeyEvent.VK_V);
	}
	return jButton;
    }

    /**
     * This method initializes jButton1	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton1() {
	if (jButton1 == null) {
	    jButton1 = new JButton();
	    jButton1.setName("jButton1");
	    jButton1.setMnemonic(KeyEvent.VK_N);
	    jButton1.setText("Renew");
	}
	return jButton1;
    }

    /**
     * This method initializes jButton2	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton2() {
	if (jButton2 == null) {
	    jButton2 = new JButton();
	    jButton2.setText("Install");
	    jButton2.setMnemonic(KeyEvent.VK_I);
	}
	return jButton2;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
    	jScrollPane = new JScrollPane();
    	jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    	jScrollPane.setViewportView(getJTextPane());
        }
        return jScrollPane;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
