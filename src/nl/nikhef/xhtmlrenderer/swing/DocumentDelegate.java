package nl.nikhef.xhtmlrenderer.swing;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

/** Document class that delegates all methods to another {@link Document}. */
public class DocumentDelegate implements Document {
	private Document doc = null;
	public DocumentDelegate() {
	}
	public DocumentDelegate(Document doc) {
	    setDocument(doc);
	}
	/** returns whether this delegate has a valid document, or if it is {@literal null}.
	 * <p>
	 * When this method returns false, one should not call any other method
	 * before running {@link #setDocument}.
	 */
	public boolean isValid() {
	    return doc != null;
	}
	public void setDocument(Document doc) {
	    this.doc = doc;
	}
	public Node adoptNode(Node source) throws DOMException {
	    return doc.adoptNode(source);
	}
	public Node appendChild(Node newChild) throws DOMException {
	    return doc.appendChild(newChild);
	}
	public Node cloneNode(boolean deep) {
	    return doc.cloneNode(deep);
	}
	public short compareDocumentPosition(Node other) throws DOMException {
	    return doc.compareDocumentPosition(other);
	}
	public Attr createAttribute(String name) throws DOMException {
	    return doc.createAttribute(name);
	}
	public Attr createAttributeNS(String namespaceURI, String qualifiedName)
	throws DOMException {
	    return doc.createAttributeNS(namespaceURI, qualifiedName);
	}
	public CDATASection createCDATASection(String data) throws DOMException {
	    return doc.createCDATASection(data);
	}
	public Comment createComment(String data) {
	    return doc.createComment(data);
	}
	public DocumentFragment createDocumentFragment() {
	    return doc.createDocumentFragment();
	}
	public Element createElement(String tagName) throws DOMException {
	    return doc.createElement(tagName);
	}
	public Element createElementNS(String namespaceURI, String qualifiedName)
	throws DOMException {
	    return doc.createElementNS(namespaceURI, qualifiedName);
	}
	public EntityReference createEntityReference(String name)
	throws DOMException {
	    return doc.createEntityReference(name);
	}
	public ProcessingInstruction createProcessingInstruction(String target,
		String data) throws DOMException {
	    return doc.createProcessingInstruction(target, data);
	}
	public Text createTextNode(String data) {
	    return doc.createTextNode(data);
	}
	public NamedNodeMap getAttributes() {
	    return doc.getAttributes();
	}
	public String getBaseURI() {
	    return doc.getBaseURI();
	}
	public NodeList getChildNodes() {
	    return doc.getChildNodes();
	}
	public DocumentType getDoctype() {
	    return doc.getDoctype();
	}
	public Element getDocumentElement() {
	    return doc.getDocumentElement();
	}
	public String getDocumentURI() {
	    return doc.getDocumentURI();
	}
	public DOMConfiguration getDomConfig() {
	    return doc.getDomConfig();
	}
	public Element getElementById(String elementId) {
	    return doc.getElementById(elementId);
	}
	public NodeList getElementsByTagName(String tagname) {
	    return doc.getElementsByTagName(tagname);
	}
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
	    return doc.getElementsByTagNameNS(namespaceURI, localName);
	}
	public Object getFeature(String feature, String version) {
	    return doc.getFeature(feature, version);
	}
	public Node getFirstChild() {
	    return doc.getFirstChild();
	}
	public DOMImplementation getImplementation() {
	    return doc.getImplementation();
	}
	public String getInputEncoding() {
	    return doc.getInputEncoding();
	}
	public Node getLastChild() {
	    return doc.getLastChild();
	}
	public String getLocalName() {
	    return doc.getLocalName();
	}
	public String getNamespaceURI() {
	    return doc.getNamespaceURI();
	}
	public Node getNextSibling() {
	    return doc.getNextSibling();
	}
	public String getNodeName() {
	    return doc.getNodeName();
	}
	public short getNodeType() {
	    return doc.getNodeType();
	}
	public String getNodeValue() throws DOMException {
	    return doc.getNodeValue();
	}
	public Document getOwnerDocument() {
	    return doc.getOwnerDocument();
	}
	public Node getParentNode() {
	    return doc.getParentNode();
	}
	public String getPrefix() {
	    return doc.getPrefix();
	}
	public Node getPreviousSibling() {
	    return doc.getPreviousSibling();
	}
	public boolean getStrictErrorChecking() {
	    return doc.getStrictErrorChecking();
	}
	public String getTextContent() throws DOMException {
	    return doc.getTextContent();
	}
	public Object getUserData(String key) {
	    return doc.getUserData(key);
	}
	public String getXmlEncoding() {
	    return doc.getXmlEncoding();
	}
	public boolean getXmlStandalone() {
	    return doc.getXmlStandalone();
	}
	public String getXmlVersion() {
	    return doc.getXmlVersion();
	}
	public boolean hasAttributes() {
	    return doc.hasAttributes();
	}
	public boolean hasChildNodes() {
	    return doc.hasChildNodes();
	}
	public Node importNode(Node importedNode, boolean deep) throws DOMException {
	    return doc.importNode(importedNode, deep);
	}
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
	    return doc.insertBefore(newChild, refChild);
	}
	public boolean isDefaultNamespace(String namespaceURI) {
	    return doc.isDefaultNamespace(namespaceURI);
	}
	public boolean isEqualNode(Node arg) {
	    return doc.isEqualNode(arg);
	}
	public boolean isSameNode(Node other) {
	    return doc.isSameNode(other);
	}
	public boolean isSupported(String feature, String version) {
	    return doc.isSupported(feature, version);
	}
	public String lookupNamespaceURI(String prefix) {
	    return doc.lookupNamespaceURI(prefix);
	}
	public String lookupPrefix(String namespaceURI) {
	    return doc.lookupPrefix(namespaceURI);
	}
	public void normalize() {
	    doc.normalize();
	}
	public void normalizeDocument() {
	    doc.normalizeDocument();
	}
	public Node removeChild(Node oldChild) throws DOMException {
	    return doc.removeChild(oldChild);
	}
	public Node renameNode(Node n, String namespaceURI, String qualifiedName)
	throws DOMException {
	    return doc.renameNode(n, namespaceURI, qualifiedName);
	}
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
	    return doc.replaceChild(newChild, oldChild);
	}
	public void setDocumentURI(String documentURI) {
	    doc.setDocumentURI(documentURI);
	}
	public void setNodeValue(String nodeValue) throws DOMException {
	    doc.setNodeValue(nodeValue);
	}
	public void setPrefix(String prefix) throws DOMException {
	    doc.setPrefix(prefix);
	}
	public void setStrictErrorChecking(boolean strictErrorChecking) {
	    doc.setStrictErrorChecking(strictErrorChecking);
	}
	public void setTextContent(String textContent) throws DOMException {
	    doc.setTextContent(textContent);
	}
	public Object setUserData(String key, Object data, UserDataHandler handler) {
	    return doc.setUserData(key, data, handler);
	}
	public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
	    doc.setXmlStandalone(xmlStandalone);
	}
	public void setXmlVersion(String xmlVersion) throws DOMException {
	    doc.setXmlVersion(xmlVersion);
	}
}