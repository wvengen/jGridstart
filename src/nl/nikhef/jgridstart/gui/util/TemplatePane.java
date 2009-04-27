package nl.nikhef.jgridstart.gui.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.SelectionHighlighter;
import org.xhtmlrenderer.util.Configuration;


public class TemplatePane extends XHTMLPanel {
    
    protected Properties data = new Properties();
    protected Action submitAction = null;
    
    @SuppressWarnings("unchecked") // getMouseTrackingListeners() returns unchecked List
    public TemplatePane() {
	super();
	setPreferredSize(new Dimension(550, 350)); // TODO set size from content
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
	// allow selecting, and copying using Ctrl-C
	SelectionHighlighter highlighter = new SelectionHighlighter();
	highlighter.install(this);
	SelectionHighlighter.CopyAction copyAction = new SelectionHighlighter.CopyAction();
	copyAction.install(highlighter);
	copyAction.putValue(Action.ACCELERATOR_KEY,
		KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
	getActionMap().put(copyAction.getValue(Action.NAME), copyAction);
	getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
		(KeyStroke)copyAction.getValue(Action.ACCELERATOR_KEY),
		copyAction.getValue(Action.NAME));
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
    
    /** Override setDocument() to process the template. This could have been
     * done using xhtmlrenderer's configuration key
     *   xr.load.xml-reader=nl.nikhef.jgridstart.gui.util.TemplatePane$TemplateXMLReader
     * but I found no way to pass data to the class, which is needed for the
     * variables. This is the setDocument() method that is called by each of
     * the others.
     */
    @Override
    public void setDocument(Document doc, String url, NamespaceHandler nsh) {
	doTemplate(doc);
	super.setDocument(doc, url, nsh);
    }
    @Override
    public void setDocument(Document doc, String url) {
	// XHTMLPanel.resetListeners() is private so copy here :(
        if (Configuration.isTrue("xr.use.listeners", true)) {
            resetMouseTracker();
        }
	doTemplate(doc);
        super.setDocument(doc, url, new XhtmlNamespaceHandler());
    }
    @Override
    public void setDocument(InputStream stream, String url) {
        setDocument(XMLResource.load(stream).getDocument(), url);
    }

    /** Processes an XML Node to apply the template recursively. */
    protected void doTemplate(Node node) {
	if (node.getNodeType() == Node.ELEMENT_NODE) {
	    // expand variables in attributes
	    NamedNodeMap attrs = node.getAttributes();
	    for (int i=0; i<attrs.getLength(); i++) {
		attrs.item(i).setNodeValue(parseExpression(attrs.item(i).getNodeValue()));
	    }
	    // apply "if" attributes
	    Node ifNode = attrs.getNamedItem("if");
	    if (ifNode!=null && !parseConditional(ifNode.getNodeValue())) {
		node.getParentNode().removeChild(node);		
		return;
	    }
	    // replace contents of "c" attributes
	    Node cNode = attrs.getNamedItem("c");
	    if (cNode!=null) {
		NodeList nl = node.getChildNodes();
		for (int i=0; i<nl.getLength(); i++)
		    node.removeChild(nl.item(i));
		node.appendChild(node.getOwnerDocument().createTextNode(cNode.getNodeValue()));
	    }
	    // fill form controls from data indicated by the "value" attribute
	    Node nameNode = attrs.getNamedItem("name");
	    if (nameNode!=null && data.getProperty(nameNode.getNodeValue())!=null) {
		String dataValue = data.getProperty(nameNode.getNodeValue());
		if (node.getNodeName().toLowerCase().equals("input")) {
		    Node typeNode = attrs.getNamedItem("type");
		    if (typeNode!=null) {
			String type = typeNode.getNodeValue().toLowerCase();
			// input elements with textual data
			if (type.equals("text") || type.equals("password") || type.equals("hidden")) {
			    Node valueAttr = node.getOwnerDocument().createAttribute("value");
			    valueAttr.setNodeValue(dataValue);
			    attrs.setNamedItem(valueAttr);
			// boolean
			} else if (type.equals("checkbox") || type.equals("radio")) {
			    if (Boolean.valueOf(dataValue)) {
				Node checkedAttr = node.getOwnerDocument().createAttribute("checked");
				checkedAttr.setNodeValue("checked");
				attrs.setNamedItem(checkedAttr);
			    } else {
				attrs.removeNamedItem("checked");
			    }
			} else {
			    // TODO warn unsupported input type
			}
		    }
		// textarea
		} else if (node.getNodeName().toLowerCase().equals("textarea")) {
			NodeList nl = node.getChildNodes();
			for (int i=0; i<nl.getLength(); i++)
			    node.removeChild(nl.item(i));
			node.appendChild(node.getOwnerDocument().createTextNode(dataValue));
		} else {
		    // TODO warn unsupported element
		}
	    }
	}
	// recursively parse children
	NodeList nl = node.getChildNodes();
	for (int i=0; i<nl.getLength(); i++) {
	    doTemplate(nl.item(i));
	}
    }

    /** returns the result of a boolean expression */
    // TODO more intelligent expression parsing, e.g. using JUEL
    protected boolean parseConditional(String expr) {
	if (expr==null) return true;
	expr = expr.trim();
	// handle negations
	if (expr.startsWith("!"))
	    return !parseConditional(expr.substring(1));
	if (expr.equals("true")) return true;
	if (expr.equals("false")) return false;
	return !expr.equals("");
    }
    /** evaluates an expression by replacing variables */
    // TODO more intelligent expression parsing, e.g. using JUEL
    protected String parseExpression(String expr) {
	// substitute variables
	StringBuffer dstbuf = new StringBuffer();
	final Pattern pat = Pattern.compile("(\\$\\{(.*?)\\})", Pattern.MULTILINE|Pattern.DOTALL);
	Matcher match = pat.matcher(expr);
	while (match.find()) {
	    String key = match.group(2).trim();
	    String sub = data!=null ? data.getProperty(key) : null;
	    if (sub==null) sub="";
	    match.appendReplacement(dstbuf, sub);
	}
	match.appendTail(dstbuf);
	return dstbuf.toString();
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
		"<form><p><input type='checkbox' disabled='disabled' name='chk' id='chk'/> <label for='chk'>a checked readonly checkbox</label></p>"+
		// check that submit button sets property values from elements
		"<p>type <input type='text' name='txt' value='**this is bad text**'/> and <input type='submit' value='submit'/></p></form>"+
		// check a locked input element
		"<p>this is a <input type='text' name='txtlocked' value='readonly'/> input element</p>"+
		"</body>"+
		"</html>";		    
	    pane.data().setProperty("foo", "the contents of this foo variable");
	    pane.data().setProperty("theurl", "http://www.w3.org/");
	    pane.data().setProperty("chk", "true");
	    pane.data().setProperty("txt", "some text");
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
