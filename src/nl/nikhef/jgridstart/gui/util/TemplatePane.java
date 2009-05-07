package nl.nikhef.jgridstart.gui.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.SelectionHighlighter;
import org.xhtmlrenderer.swing.SwingReplacedElement;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import org.xhtmlrenderer.util.Configuration;
import org.xml.sax.SAXException;


public class TemplatePane extends XHTMLPanel {
    
    protected Properties data = new Properties();
    protected Action submitAction = null;
    
    @SuppressWarnings("unchecked") // getMouseTrackingListeners() returns unchecked List
    public TemplatePane() {
	super();
	setPreferredSize(new Dimension(550, 350)); // TODO set size from content
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
	// install custom form handling hooks
	getSharedContext().setReplacedElementFactory(new TemplateSwingReplacedElementFactory());
	setFormSubmissionListener(this);
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
	// update properties from form
	
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
    
    /** print the contents of this pane with the smallest possible printer margins;
     * a print dialog is shown first.
     * TODO fix margins and related stuff
     * 
     * currently uses attempted fix from http://markmail.org/message/37rc4vaiz6peto5h 
     * @throws PrinterException */
    public boolean print() throws PrinterException {
	final PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new TemplatePrintable(this));
        if (printJob.printDialog()) {
            printJob.print();
        }
	return true;
    }
    
    /** run user-supplied action on form submission */
    @Override
    public void submit(String url) {
	if (submitAction!=null) {
	    String action = null;
	    // find out which submit button was the source
	    NodeList nl = getDocument().getElementsByTagName("input");
	    for (int i=0; i<nl.getLength(); i++) {
		Node node = nl.item(i);
		Node type =  node.getAttributes().getNamedItem("type");
		if (type==null) continue;
		if (!type.getNodeValue().equals("submit")) continue;
		Node name = node.getAttributes().getNamedItem("name");
		if (name==null) continue;
		if (url.contains(name.getNodeValue()+'=')) {
		    action = name.getNodeValue();
		    break;
		}		
	    }
	    // and use that as command string in the action event
	    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, action);
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
		// erase existing contents
		NodeList nl = node.getChildNodes();
		for (int i=0; i<nl.getLength(); i++)
		    node.removeChild(nl.item(i));
		node.setTextContent(null);
		/* and add new content; it might have been nice to do
		 *    node.appendChild(node.getOwnerDocument().createTextNode(cNode.getNodeValue()));
		 * but that doesn't allow one to put html in variables. So a
		 * new html document is created from the parsed node, and the
		 * resulting html is put into the original document.
		 * 
		 * to make adoptNode() work, the following workaround is used
		 *   http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4915524
		 * 
		 * TODO use DOM implementation from node for parsing */ 
		try {
		    byte[] data = ("<root>"+cNode.getNodeValue()+"</root>").getBytes();
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    factory.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE);
		    Document newDoc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(data));
		    node.appendChild(node.getOwnerDocument().adoptNode(newDoc.getFirstChild()));
		} catch (SAXException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (ParserConfigurationException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO shouldn't happen
		    e.printStackTrace();
		}
	    }
	    // readonly not implemented by xhtmlreader itself but disabled is
	    // also if property lock.<name> is set make it readonly
	    Node name = attrs.getNamedItem("name");
	    Node rdNode = attrs.getNamedItem("readonly");
	    if (rdNode!=null ||
		    (name!=null && Boolean.valueOf(data().getProperty(name.getNodeValue()+".lock"))) ) {
		Node attr = node.getOwnerDocument().createAttribute("disabled");
		attr.setNodeValue("disabled");
		attrs.setNamedItem(attr);
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
    
    /** class that returns forms elements with hooks to update the
     * corresponding property (as returned by data() ) on change. Also
     * sets form elements' initial value from properties. */
    protected class TemplateSwingReplacedElementFactory extends SwingReplacedElementFactory {

	@Override
	public ReplacedElement createReplacedElement(LayoutContext context, BlockBox box,
		UserAgentCallback uac, int cssWidth, int cssHeight) {
	    ReplacedElement el = super.createReplacedElement(context, box, uac, cssWidth, cssHeight);
	    if (el instanceof SwingReplacedElement)
		bindProperty(box.getElement(), ((SwingReplacedElement)el).getJComponent());
	    return el;
	}

	/** Add a listener that updates the properties bound to the
	 * enclosing TemplatePane when the supplied component is changed. This
	 * also sets the current value to the property's current value. If no
	 * name is set on the element, nothing is bound. */
	protected void bindProperty(Element e, JComponent c) {
	    String name = e.getAttribute("name");
	    String ivalue = e.getAttribute("value");
	    // only for options the name is taken from enclosing select
	    if (e.getTagName().toLowerCase().equals("option")) {
		Node node;
		while ((node = e.getParentNode()) != null) {
		    if (node.getNodeName().toLowerCase().equals("select")) {
			name = node.getAttributes().getNamedItem("name").getNodeValue();
			break;
		    }
		}
	    }
	    if (name==null) return;
	    String value = data.getProperty(name);
	    // we care about content controls, not scrollpanes
	    if (c instanceof JScrollPane) c = (JComponent)((JScrollPane)c).getViewport().getView();
	    // how to get&set contents depends on type of control.
	    if (c instanceof JTextComponent) {
		// text and password
		if (value!=null)
		    ((JTextComponent)c).setText(value);
		((JTextComponent)c).getDocument().addDocumentListener(new FormComponentListener(e, c));
	    } else if (c instanceof JComboBox || c instanceof JList) {
		// combo box or list
		// find selected items from document; see also listSelectionChanged()
		// TODO implement multiple selection
		int index = -1;
		NodeList nodes = e.getElementsByTagName("option");
		for (int i=0; i<nodes.getLength(); i++) {
		    Node nodeValue = nodes.item(i).getAttributes().getNamedItem("value");
		    if (nodeValue==null) continue;
		    if (nodeValue.getNodeValue().equals(value)) {
			    index = i;
			    break;
		    }
		}
		if (c instanceof JComboBox) {
		    if (value!=null)
			((JComboBox)c).setSelectedIndex(index);
		    ((JComboBox)c).addItemListener(new FormComponentListener(e, c));
		} else if (c instanceof JList) {
		    if (value!=null)
			((JList)c).setSelectedIndex(index);
		    ((JList)c).addListSelectionListener(new FormComponentListener(e, c));
		}
	    } else if (c instanceof JRadioButton) {
		// radio button
		if (value!=null)
		    ((JRadioButton)c).setSelected(ivalue.equals(value));
		((AbstractButton)c).addChangeListener(new FormComponentListener(e, c));
	    } else if (c instanceof AbstractButton) {
		// checkbox
		if (value!=null)
		    ((AbstractButton)c).setSelected(Boolean.valueOf(value));
		((AbstractButton)c).addChangeListener(new FormComponentListener(e, c));
	    }
	    // TODO other form elements as well
	    // TODO just copy hidden to properties?
	}

	protected class FormComponentListener implements DocumentListener, ChangeListener, ListSelectionListener, ItemListener {

	    protected Element el = null;

	    public FormComponentListener(Element el, Object source) {
		this.el = el;
		// make sure data and component are in sync. This is an issue when
		// the html has specified a default value but the property isn't
		// defined. This call updates the property from the default value.
		doUpdate(source);
	    }
	    
	    protected void doUpdate(Object source) {
		String name = el.getAttribute("name");
		
		if (source instanceof JRadioButton && ((JRadioButton)source).isSelected()) {
		    data.setProperty(name, el.getAttribute("value"));
		
		} else if (source instanceof AbstractButton) {
		    data.setProperty(name, Boolean.toString(((AbstractButton)source).isSelected()));
		
		} else if (source instanceof JComboBox || source instanceof JList) { 
		    /** Update the properties bound to the enclosing TemplatePane
		     * from a list. This is called when a list's state is changed.
		     * 
		     * The implementation looks somewhat clumsy. I have no access to the
		     * underlying model since that is declared private. So I get the
		     * selected index and look up the corresponding value from the
		     * document.
		     * TODO implement lists with multiple selections */
		    int index = -1;
		    if (source instanceof JComboBox)
			index = ((JComboBox)source).getSelectedIndex();
		    else if (source instanceof JList)
			index = ((JList)source).getSelectedIndex();
		    // TODO warn if neither?
		    NodeList nodes = el.getElementsByTagName("option");
		    if (index>=0 && index<nodes.getLength()) {
			Node value = nodes.item(index).getAttributes().getNamedItem("value");
			if (value!=null)
			    data.setProperty(name, value.getNodeValue());
		    }
		
		} else if (source instanceof JTextComponent) {
		    // not used since document is used here
		    data.setProperty(name, ((JTextComponent)source).getText());
		} else if (source instanceof javax.swing.text.Document) {
		    javax.swing.text.Document doc = (javax.swing.text.Document)source;
		    try {
			data.setProperty(name, doc.getText(0, doc.getLength()));
		    } catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		} else {
		    // TODO warn
		    System.out.println("unrecognised update!");
		}
	    }

	    // document update (text fields)
	    public void changedUpdate(DocumentEvent e)  { doUpdate(e.getDocument()); }
	    public void insertUpdate(DocumentEvent e)   { doUpdate(e.getDocument()); }
	    public void removeUpdate(DocumentEvent e)   { doUpdate(e.getDocument()); }
	    // button update
	    public void stateChanged(ChangeEvent e) {
		doUpdate(e.getSource());
	    }
	    // combobox selection update
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    doUpdate(e.getSource());
	    }
	    // list selection update
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting())
		    doUpdate(e.getSource());
	    }
	}
    }

    @SuppressWarnings("unused")
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
		// select
		"<p>And a <select name='sel'><option value='bad'>bad</option><option value='selected'>selected</option></select> select box with "+
		// radio buttons
		"<input type='radio' name='rad' value='one'/>one or <input type='radio' name='rad' value='two'/>two radio buttons</p>"+
		// check readonly attribute on form element and value from property
		"<form><p><input type='checkbox' readonly='readonly' name='chk' id='chk'/> <label for='chk'>a checked readonly checkbox</label></p>"+
		// check that submit button sets property values from elements
		"<p>type <input type='text' name='txt' value='**this is bad text**'/> and <input type='submit' name='show' value='submit'/></p></form>"+
		// check a locked input element
		"<p>this is a <input type='text' name='txtlocked' value='readonly'/> input element</p>"+
		// add print button
		"<form><p>you can also <input type='submit' name='print' value='print'/> this page.</p></form>"+
		// test putting in html from a variable
		"<div c='${somehtml}'>hmm, <span style='color:red'>you shouldn't see this text</span></div>"+
		"</body>"+
		"</html>";		    
	    pane.data().setProperty("foo", "the contents of this foo variable");
	    pane.data().setProperty("theurl", "http://www.w3.org/");
	    pane.data().setProperty("chk", "true");
	    pane.data().setProperty("txt", "some text");
	    pane.data().setProperty("lock.txtlocked", "true");
	    pane.data().setProperty("sel", "selected");
	    pane.data().setProperty("somehtml", "you should see <em>this</em> text");
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
		    if (e.getActionCommand().equals("show"))
			JOptionPane.showMessageDialog(frame,
				"Checkbox: "+pane.data().getProperty("chk")+"\n"+
				"Selection: "+pane.data().getProperty("sel")+"\n"+
				"Radio: "+pane.data().getProperty("rad")+"\n"+
				"Text: "+pane.data().getProperty("txt"),
				"Form submitted",
				JOptionPane.INFORMATION_MESSAGE);
		    else if (e.getActionCommand().equals("print"))
			try {
			    pane.print();
			} catch (PrinterException e1) {
			    e1.printStackTrace();
			}
		}
	    });
	    frame.setVisible(true);
	}
    }
}
