package nl.nikhef.jgridstart.gui;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.nikhef.jgridstart.CertificateSelection;
import nl.nikhef.jgridstart.gui.util.URLLauncher;
import nl.nikhef.jgridstart.gui.util.ErrorMessage;
import nl.nikhef.jgridstart.gui.util.FileFilterSuffix;
import nl.nikhef.jgridstart.gui.util.TemplateButtonPanel;
import nl.nikhef.xhtmlrenderer.swing.ITemplatePanel;

import org.xhtmlrenderer.pdf.ITextRenderer;

/** Open verification form dialog.
 * <p>
 * 
 * @author wvengen
 */
public class ActionViewVerificationForm extends CertificateAction {
    
    protected VerificationDialog dlg = null;

    public ActionViewVerificationForm(JFrame parent, CertificateSelection s) {
	super(parent, s);
	putValue(NAME, "Verification form");
	putValue(MNEMONIC_KEY, new Integer('V'));
	URLLauncher.addAction("verificationform", this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
	if (dlg==null) {
	    Window w = findWindow(e.getSource());
	    if (w instanceof Frame)
		dlg = new VerificationDialog((Frame)w, getCertificatePair());
	    else if (w instanceof Dialog)
		dlg = new VerificationDialog((Dialog)w, getCertificatePair());
	    else
		ErrorMessage.internal(w, "Expected Frame or Dialog as owner");
	    dlg.pack();
	} else {
	    dlg.setData(getCertificatePair());
	}
	dlg.setVisible(true);
    }
    
    /** Dialog that shows the verification form */
    protected static class VerificationDialog extends JDialog {
	private TemplateButtonPanel form = null;
	public VerificationDialog(Frame parent, Properties data) {
	    super(parent);
	    initialize(data);
	}
	public VerificationDialog(Dialog parent, Properties data) {
	    super(parent);
	    initialize(data);
	}
	
	private void initialize(Properties data) {
	    try {
		form = new TemplateButtonPanel(getClass().getResource("verification_form.html").toExternalForm());
		form.setData(data);
		add(form);
		setTitle(form.getDocumentTitle());
		setPreferredSize(new Dimension(750, 470));
		// min size to avoid fixed-size fields overlap text
		//  setMinimumSize is Java 1.6 or higher, so don't complain in that case
		try { setMinimumSize(new Dimension(700, 400)); }
		catch (NoSuchMethodError e) { }
		form.addButton(new JButton(new PrintAction(this, form)), true);
		form.addButton(new JButton(new SaveToPDFAction(this, form)), true);
		form.addSeparator();
		form.addButton(new JButton(new CloseAction(this)), true);
	    } catch (IOException e) {
		ErrorMessage.internal(getParent(), e);
	    }
	}
	/** update the window's contents */
	public void setData(Properties data) {
	    form.setData(data);
	}
    }
    
    /** Action for printing the page displayed */
    protected static class PrintAction extends AbstractAction {
	private ITemplatePanel panel;
	private Window src;
	public PrintAction(Window src, ITemplatePanel panel) {
	    this.panel = panel;
	    this.src = src;
	    putValue(NAME, "Print...");
	    putValue(MNEMONIC_KEY, new Integer('P'));
	}
	public void actionPerformed(ActionEvent e) {
	    src.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    try {
			panel.print();
			src.setCursor(Cursor.getDefaultCursor());
		    } catch (PrinterException e) {
			ErrorMessage.error(src, "Printing failed", e);
		    }
		}
	    });
	}
    }
    
    /** Action for closing the form window */
    protected static class CloseAction extends AbstractAction {
	private Window src;
	public CloseAction(Window src) {
	    this.src = src;
	    putValue(NAME, "Close");
	    putValue(MNEMONIC_KEY, new Integer('C'));
	}
	
	public void actionPerformed(ActionEvent e) {
	    src.dispose();
	}
    }
    
    /** Action for saving the page displayed to a PDF */
    protected static class SaveToPDFAction extends AbstractAction {
	private ITemplatePanel panel;
	private Window src;
	public SaveToPDFAction(Window src, ITemplatePanel panel) {
	    this.panel = panel;
	    this.src = src;
	    putValue(NAME, "Save as PDF...");
	    putValue(MNEMONIC_KEY, new Integer('S'));
	}
	public void actionPerformed(ActionEvent e) {
	    JFileChooser chooser = new JFileChooser();
	    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
	    chooser.setFileFilter(new FileFilterSuffix("PDF document", ".pdf"));
	    chooser.setDialogTitle("Save form as PDF");
	    int result = chooser.showDialog(src, null);
	    if (result == JFileChooser.APPROVE_OPTION) {
		doSave(chooser.getSelectedFile());
	    }
	}
	public void doSave(File dest) {
	    // add .pdf if not that extension
	    if (!dest.getName().toLowerCase().endsWith(".pdf"))
		dest = new File(dest.toString() + ".pdf");
	    // and render to pdf
	    try {
		ITextRenderer r = new ITextRenderer();
		r.setDocument(panel.getDocument(), panel.getDocument().getDocumentURI());
		OutputStream os;
		os = new FileOutputStream(dest);
		r.layout();
		r.createPDF(os);
		os.close();	    
	    } catch (Exception e) {
		ErrorMessage.error(src, "Saving to PDF failed", e);
	    }
	}
    }
}
