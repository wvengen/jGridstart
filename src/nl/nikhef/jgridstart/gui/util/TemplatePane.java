package nl.nikhef.jgridstart.gui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xhtmlrenderer.simple.XHTMLPanel;


public class TemplatePane extends XHTMLPanel {
    
    protected Properties data = new Properties();
    
    protected Action submitAction = null;
    
    public TemplatePane() {
	super();
	setPreferredSize(new Dimension(400, 200)); // TODO set size from content
	/*
	addHyperlinkListener(new HyperlinkListener() {
	    public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		    // use getDescription() because action: events may not be
		    // recognised by URL so e.getURL() can be null.
		    BareBonesActionLaunch.openURL(e.getDescription(), this);
	       }
	   }
	});
	*/
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
	try {
	    return new URL(getDocument().getDocumentURI());
	} catch (MalformedURLException e) {
	    return null;
	}
    }
    /** Set the action to perform on form submission. If this is set
     * to null, the standard behaviour is done: posting data to the url
     * supplied by the form. */
    public void setSubmitAction(Action e) {
	submitAction = e;
    }
    
    /** Return the HTMLDocument */
    public HTMLDocument getHTMLDocument() {
	return (HTMLDocument)getDocument();
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
		"<p>Basic conditionals are<font color='red' if='false'> not</font> "+
			"<font color='green' if='true'>working</font></p>"+
		// check negated conditional on set property
		"<p>Variable foo is<font color='red' if='!${foo}'> not</font> set</p>"+
		// check conditional on set property
		"<p>Variable foo is<font color='green' if='${foo}'> certainly</font> set</p>"+
		// check conditional on unset property
		"<p>Variable bar is<font color='green' if='!${bar}'> not</font> set</p>"+
		// check substitution with set property
		"<p>And so foo is set to '<i c='${foo}'></i>', "+
		// check substitution with unset property
		"while bar is set to '<i c='${bar}'>(removed)</i>' (that's right, nothing).</p>"+
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
