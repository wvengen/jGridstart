package nl.nikhef.xhtmlrenderer.swing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TemplateDocumentTest extends TestCase {
    
    /** Helper method: return a test document from a html body. 
     * 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException */
    protected static Document parseBody(String html) throws SAXException, IOException, ParserConfigurationException {
	byte[] data = (
		"<html>" +
		"<head><title>TemplateDocument Test page</title></head>" +
		"<body>" + html + "</body>" +
		"</html>").getBytes();
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE);
	return factory.newDocumentBuilder().parse(new ByteArrayInputStream(data));
    }
    /** Helper method: return the body Node of a Document. */
    protected static Node getBody(Document doc) {
	NodeList nl = doc.getElementsByTagName("body");
	return nl.item(0);
    }
    /** Helper method: compare the body Node of a Document to a string.
     * 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException */
    protected static boolean bodyEquals(Document doc, String body) throws SAXException, IOException, ParserConfigurationException {
	Node n1 = getBody(doc);
	Node n2 = getBody(parseBody(body));
	n1.normalize();
	n2.normalize();
	return isEqualNode(n1, n2);
    }
    /** Helper method: compare template String to output String. */
    protected static void templateTest(String in, String parsed) throws SAXException, IOException, ParserConfigurationException {
	TemplateDocument template = new TemplateDocument(parseBody(in));
	assertTrue(bodyEquals(template, parsed));	
    }
    /** Helper method: compare template String to output String with properties */
    protected static void templateTest(String in, String parsed, Properties p) throws SAXException, IOException, ParserConfigurationException {
	TemplateDocument template = new TemplateDocument(parseBody(in), p);
	assertTrue(bodyEquals(template, parsed));	
    }
    
    /** Helper method: isEqualNode that works for deep comparisons
     * <p>
     * This is needed since Node.isEqualNode doesn't seem to deeply compare child nodes :)
     * 
     * @see Document#isEqualNode
     */
    protected static boolean isEqualNode(Node n1, Node n2) {
	// ok if the are the same objects
	if (equals(n1, n2)) return true;
	if (n1==null || n2==null) return false;
	// compare properties
	if (n1.getNodeType() != n2.getNodeType()) return false;
	if (!equals(n1.getNodeName(), n2.getNodeName())) return false;
	// don't compare localname because its availability is DOM implementation dependent
	//if (!equals(n1.getLocalName(), n2.getLocalName())) return false;
	if (!equals(n1.getNamespaceURI(), n2.getNamespaceURI())) return false;
	if (!equals(n1.getPrefix(), n2.getPrefix())) return false;
	if (!equals(n1.getNodeValue(), n2.getNodeValue())) return false;
	// handle non-elements differently
	if (n1.getNodeType() != Node.ELEMENT_NODE) return true;
	// compare attributes but skip template elements "if" and "c"
	int attrlen1 = n1.getAttributes().getLength();
	int attrlen2 = n2.getAttributes().getLength();
	if (n1.getAttributes().getNamedItem("if")!=null) attrlen1--;
	if (n1.getAttributes().getNamedItem("c")!=null) attrlen1--;
	if (n2.getAttributes().getNamedItem("if")!=null) attrlen2--;
	if (n2.getAttributes().getNamedItem("c")!=null) attrlen2--;
	if (attrlen1 != attrlen2) return false;
	for (int i=0; i<n1.getAttributes().getLength(); i++) {
	    String name = n1.getAttributes().item(i).getNodeName();
	    if (name.equals("if") || name.equals("c")) continue;
	    Node n1a = n1.getAttributes().item(i);
	    Node n2a = n2.getAttributes().getNamedItem(name);
	    // test
	    if (!isEqualNode(n1a, n2a)) return false;
	}
	// compare child nodes
	if (n1.getChildNodes().getLength() != n2.getChildNodes().getLength()) return false;
	for (int i=0; i<n1.getChildNodes().getLength(); i++) {
	    // node replacement (attribute "c") can introduce root element
	    Node n1n = n1.getChildNodes().item(i);
	    Node n2n = n2.getChildNodes().item(i);
	    if (!isEqualNode(n1n, n2n)) return false;
	}
	// all equal
	return true;
    }
    /** Return whether two objects are equals, also work when one or both are null. */
    protected static boolean equals(Object a, Object b) {
	if (a==b) return true;
	if (a==null || b==null) return false;
	return a.equals(b);
    }

    @Test
    public void testTemplateDocumentDocument() throws Exception {
	TemplateDocument template = new TemplateDocument(parseBody("<p>hi there</p>"));
	assertNotNull(template);
    }

    @Test
    public void testTemplateDocumentDocumentProperties() throws Exception {
	Properties p = new Properties();
	p.setProperty("test", "yeah");
	TemplateDocument template = new TemplateDocument(parseBody("<p>hi there</p>"), p);
	assertNotNull(template);
	assertEquals("yeah", template.data().getProperty("test"));
    }

    @Test
    public void testData() throws Exception {
	Properties p = new Properties();
	p.setProperty("test", "yeah");
	TemplateDocument template = new TemplateDocument(parseBody("<p>hi there</p>"));
	assertNull(template.data().getProperty("test"));
	template.setData(p);
	assertEquals("yeah", template.data().getProperty("test"));
    }

    @Test
    public void testConditionals() throws Exception {
	// single static values
	templateTest("<p if='true'>1</p>", "<p>1</p>");
	templateTest("<p if='false'>1</p>", "");
	templateTest("<p if='somestring'>1</p>", "<p>1</p>");
	templateTest("<p if=''></p>", "");
	// negations
	templateTest("<p if='!true'>1</p>", "");
	templateTest("<p if='!false'>1</p>", "<p>1</p>");
	templateTest("<p if='!!false'>1</p>", "");
	templateTest("<p if='!'>1</p>", "<p>1</p>");
	// boolean or
	templateTest("<p if='true or true'>1</p>", "<p>1</p>");
	templateTest("<p if='true or false'>1</p>", "<p>1</p>");
	templateTest("<p if='false or true'>1</p>", "<p>1</p>");
	templateTest("<p if='false or false'>1</p>", "");
	// boolean and
	templateTest("<p if='true and true'>1</p>", "<p>1</p>");
	templateTest("<p if='true and false'>1</p>", "");
	templateTest("<p if='false and true'>1</p>", "");
	templateTest("<p if='false and false'>1</p>", "");
	// brackets
	templateTest("<p if='()'>1</p>", "");
	templateTest("<p if='(true)'>1</p>", "<p>1</p>");
	templateTest("<p if='((true))'>1</p>", "<p>1</p>");
	templateTest("<p if='(((true)))'>1</p>", "<p>1</p>");
	templateTest("<p if='((((((((((((((((((((true))))))))))))))))))))'>1</p>", "<p>1</p>");
	templateTest("<p if='(true or false)'>1</p>", "<p>1</p>");
	templateTest("<p if='true or (false)'>1</p>", "<p>1</p>");
	templateTest("<p if='(true) or false'>1</p>", "<p>1</p>");
	templateTest("<p if='(true or (false))'>1</p>", "<p>1</p>");
	templateTest("<p if='!()'>1</p>", "<p>1</p>");
	templateTest("<p if='!(!(!true))'>1</p>", "");
	templateTest("<p if='true and ((true or (true and false)) and true)'>1</p>", "<p>1</p>");
	templateTest("<p if='true and ((true or (true and false)) and !true)'>1</p>", "");
	// comparison
	templateTest("<p if='a==a'>1</p>", "<p>1</p>");
	templateTest("<p if='a!=a'>1</p>", "");
	templateTest("<p if=' \"SDOjioSDFIOJSD\" != \"SDOjioSDFIOJSD\"   '>1</p>", "<p>1</p>");
	templateTest("<p if=' \"SDOjioSDFIOJSD\" !=    '>1</p>", "<p>1</p>");
	templateTest("<p if='=='>1</p>", "<p>1</p>");
	templateTest("<p if='!='>1</p>", "");
	templateTest("<p if=' =='>1</p>", "<p>1</p>");
	templateTest("<p if=' !='>1</p>", "");
	templateTest("<p if='== '>1</p>", "<p>1</p>");
	templateTest("<p if='!= '>1</p>", "");
    }
    
    @Test
    public void testContentsreplaces() throws Exception {
	// static replacements
	templateTest("<p c='test'/>", "<p>test</p>");
	templateTest("<p c=''/>", "<p/>");
	// breaks xerces parser in {@link #parseBody}, so skip
	//templateTest("<p c='<i>test</i>'/>", "<p><i>test</i></p>");
	
	// replacements from properties
	Properties p = new Properties();
	p.setProperty("vtrue", "true");
	p.setProperty("vfalse", "false");
	p.setProperty("vempty", "");
	p.setProperty("vhtml", "<i>this is <u>some</u> html</i>");
	templateTest("<p c='${vtrue}'/>", "<p/>");
	templateTest("<p c='${vtrue}'/>", "<p>true</p>", p);
	templateTest("<p c='${vfalse}'/>", "<p>false</p>", p);
	templateTest("<p c='${vtrue} yeah'/>", "<p>true yeah</p>", p);
	templateTest("<p c='${vempty}'/> blup", "<p/> blup");
	templateTest("<p c='${vhtml}'/>", "<p>"+p.getProperty("vhtml")+"</p>", p);
    }
    
    /** Test more complex html substitution */
    @Test
    public void testContentsReplace2() throws Exception {
	Properties p =new Properties();
	p.setProperty("foohtml", "<a href='http://www.w3.org/'>w3</a>, this <div>yeah</div>");
	templateTest("<p c='${foohtml}'/>", "<p>"+p.getProperty("foohtml")+"</p>", p);
    }
    
    /** Test contents substitution with boolean evaluation */
    @Test
    public void testContentsReplace3() throws Exception {
	Properties p = new Properties();
	p.setProperty("foo", "true");
	templateTest("<p c='${(true)}'/>", "<p>true</p>");
	templateTest("<p c='${(false)}'/>", "<p>false</p>");
	templateTest("<p c='${((true))}'/>", "<p>true</p>");
	templateTest("<p c='${((false))}'/>", "<p>false</p>");
	templateTest("<p c='${(!true)}'/>", "<p>false</p>");
	templateTest("<p c='${(!false)}'/>", "<p>true</p>");
	templateTest("<p class='${(true)}'/>", "<p class='true'/>");
	templateTest("<p c='${(${foo})}'/>", "<p>false</p>");
	templateTest("<p c='${(${foo})}'/>", "<p>true</p>", p);
    }
    
    @Test
    public void testLock() throws Exception {
	templateTest("<input type='text' name='foo' readonly='readonly'/>", "<input type='text' name='foo' readonly='readonly' disabled='disabled'/>");
	Properties p = new Properties();
	templateTest("<input type='text' name='foo'/>", "<input type='text' name='foo'/>", p);
	p.setProperty("foo.lock", "true");
	templateTest("<input type='text' name='foo'/>", "<input type='text' name='foo' disabled='disabled'/>", p);
    }
    
    @Test
    public void testRefresh() throws Exception {
	String body = "<p c='${txt}'/>";
	TemplateDocument template = new TemplateDocument(parseBody(body));
	assertTrue(bodyEquals(template, "<p/>"));
	template.data().setProperty("txt", "yeah");
	template.refresh();
	assertTrue(bodyEquals(template, "<p>yeah</p>"));
    }
    
    @Test
    public void testSystemData() throws Exception {
	String body = "<p c='${os.name}'/>";
	TemplateDocument template = new TemplateDocument(parseBody(body));
	assertTrue(bodyEquals(template, "<p>"+System.getProperty("os.name")+"</p>"));
    }
}
