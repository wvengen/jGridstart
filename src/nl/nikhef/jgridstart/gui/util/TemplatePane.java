package nl.nikhef.jgridstart.gui.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;


public class TemplatePane extends XHTMLPanel {
    
    protected Properties data = new Properties();
    
    protected Action submitAction = null;
    
    @SuppressWarnings("unchecked")
    public TemplatePane() {
	super();
	setPreferredSize(new Dimension(400, 200)); // TODO set size from content
	setFormSubmissionListener(this);
	// don't open links in the same pane but in an external web browser instead.
	// BareBonesActionLaunch also handles action: links
	List<FSMouseListener> ls = (List<FSMouseListener>)getMouseTrackingListeners();
	for (Iterator<FSMouseListener> it = ls.iterator(); it.hasNext(); ) {
	    FSMouseListener l = it.next();
	    if (l instanceof LinkListener)
		removeMouseTrackingListener(l);
	}
	addMouseTrackingListener(new LinkListener() {
	    @Override
            public void linkClicked(BasicPanel panel, String uri) {
		BareBonesActionLaunch.openURL(uri, panel);
	    }
	});
	// TODO filter to replace "<tag .../>" by "<tag ...></tag>"
    }
    
    public TemplatePane(URL src) throws IOException {
	this();
	setPage(src);
    }
    
    public void setData(Properties p) {
	data = p;
	refresh();
    }
    public Properties data() {
	return data;
    }
    /** Return the content of the html page's title tag */
    public String getTitle() {
	return getDocumentTitle();
    }
    /** Refresh the contents so that all parsing is redone */
    public void refresh() {
	reloadDocument(getDocument());
    }
    public void setPage(URL url) {
	setDocument(url.toString());
    }
    public URL getPage() {
	return getURL();
    }
    /** Set the action to perform on form submission. If this is set
     * to null, the standard behaviour is done: posting data to the url
     * supplied by the form. */
    public void setSubmitAction(Action e) {
	submitAction = e;
    }
    
    public boolean print() {
	PrintUtilities print = new PrintUtilities(this);
	print.print();
	return true;
    }
    
    /** run user-supplied action on form submission */
    @Override
    public void submit(String url) {
	if (submitAction!=null) {
	    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
	    submitAction.actionPerformed(e);
	}
    }
        
    private static class Test {
	public static void main(String[] args) throws Exception {
	    final TemplatePane pane = new TemplatePane();
	    final String testPage = 
		"<html>"+
		"<head><title>Test page</title></head>"+
		"<body>"+
		"<h1>Test page</h1>"+
		// check substitution in ordinary attribute
		"<p>Check that this points to <a href='${theurl}'>www.w3.org</a>.</p>"+
		// check literal conditional
		"<p>Basic conditionals are<span style='color:red' if='false'> not</span> "+
			"<span style='color:green' if='true'>working</span></p>"+
		// check negated conditional on set property
		"<p>Variable foo is<span style='color:red' if='!${foo}'> not</span> set</p>"+
		// check conditional on set property
		"<p>Variable foo is<span style='color:green' if='${foo}'> certainly</span> set</p>"+
		// check conditional on unset property
		"<p>Variable bar is<span style='color:green' if='!${bar}'> not</span> set</p>"+
		// check substitution with set property
		"<p>And so foo is set to '<i c='${foo}'></i>', "+
		// check substitution with unset property
		"while bar is set to '<i c='${bar}'>(removed)</i>' (should be empty).</p>"+
		// check readonly attribute on form element and value from property
		"<form><p><input type='checkbox' readonly='yes' name='chk'/> a checked readonly checkbox</p>"+
		// check that submit button sets property values from elements
		"<p>type <input type='text' name='txt' value='some text'/> and <input type='submit' value='submit'/></p></form>"+
		// check a locked input element
		"<p>this is a <input type='text' name='txtlocked' value='readonly'/> input element</p>"+
		"</body>"+
		"</html>";		    
	    pane.data().setProperty("foo", "the contents of this foo variable");
	    pane.data().setProperty("theurl", "http://www.w3.org/");
	    pane.data().setProperty("chk", "true");
	    pane.data().setProperty("lock.txtlocked", "true");
	    // don't set "bar"
	    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    pane.setDocument(builder.parse(new ByteArrayInputStream(testPage.getBytes())));
	    final JFrame frame = new JFrame();
	    frame.getContentPane().add(new JScrollPane(pane));
	    frame.setSize(pane.getPreferredSize());
	    frame.setTitle("TemplatePane Test");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    pane.setSubmitAction(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    JOptionPane.showMessageDialog(frame,
			"Checkbox: "+pane.data().getProperty("chk")+"\n"+
		    	"Text: "+pane.data().getProperty("txt"),
			"Form submitted",
			JOptionPane.INFORMATION_MESSAGE);
		}
	    });
	    frame.setVisible(true);
	}
    }
}
