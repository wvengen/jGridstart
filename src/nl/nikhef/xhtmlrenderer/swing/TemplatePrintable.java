package nl.nikhef.xhtmlrenderer.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.lang.reflect.Field;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.newmatch.Matcher;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PageRule;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.XHTMLPrintable;

/**
 * Fix for XHTMLPrintable to set document margins to the printer's margins.
 * <p>
 * Seems to work nicely on Windows, not yet so much on Linux.
 * <p>
 * Currently also converts form elements to text items. TODO make this
 * configurable.
 * <p>
 * Originally, this code was based on a
 * <a href="http://markmail.org/message/37rc4vaiz6peto5h">thread</a>
 * on xhtmlrenderer's users mailing list.
 * 
 * <p>
 * TODO test more and finish
 */
public class TemplatePrintable extends XHTMLPrintable {
    
    public TemplatePrintable(XHTMLPanel panel) {
	super(panel);
    }

    /** {@inheritDoc}
     * <p>
     * This method fixes the margins just before layouting.
     */
    @Override
    public int print(Graphics g, PageFormat pf, int page) {
	if (g2r==null) {
            g2r = new Graphics2DRenderer();
            g2r.getSharedContext().setPrint(true);
            g2r.getSharedContext().setInteractive(false);
            g2r.getSharedContext().setDPI(72f);
            g2r.getSharedContext().getTextRenderer().setSmoothingThreshold(0);
            g2r.getSharedContext().setUserAgentCallback(panel.getSharedContext().getUserAgentCallback());
            g2r.setDocument(translateFormElements(panel.getDocument()), panel.getSharedContext().getUac().getBaseURL());
            g2r.getSharedContext().setReplacedElementFactory(panel.getSharedContext().getReplacedElementFactory());
            // scale down a little to match pdf export (tuned manually)
            float oldFontScale = panel.getSharedContext().getTextRenderer().getFontScale();
            g2r.getSharedContext().getTextRenderer().setFontScale(oldFontScale*0.9f);
            g2r.layout((Graphics2D)g, null);
            g2r.getPanel().assignPagePrintPositions((Graphics2D)g);
	}
	return super.print(g, pf, page);
    }
    
    /** Replace form elements with text.
     * <p>
     * Translates form elements to ordinary DIV's so one doesn't
     * see the form elements on printout:
     * <code><pre>
     * &lt;span class="replaced-form-element"&gt;<i>form element value</i>&lt;/span&gt;
     * </pre></code>
     */
    public static Document translateFormElements(Document origDoc) {
	// clone the document
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    Document newDoc = factory.newDocumentBuilder().newDocument();
	    newDoc.appendChild(newDoc.importNode(origDoc.getFirstChild(), true));
	    if (origDoc.getDocumentURI()!=null)
		newDoc.setDocumentURI(origDoc.getDocumentURI());
	    newDoc.setStrictErrorChecking(origDoc.getStrictErrorChecking());
	    newDoc.setXmlStandalone(origDoc.getXmlStandalone());
	    newDoc.setXmlVersion(origDoc.getXmlVersion());
	    // iterate the document, replace form elements with their contents
	    NodeList elements = newDoc.getElementsByTagName("*");
	    for (int i=0; i<elements.getLength(); i++) {
		Node el = elements.item(i);
		String text = null;
		// element: input
		if ("input".equals(el.getNodeName()) ||
			"button".equals(el.getNodeName())) {
		    Node attr = el.getAttributes().getNamedItem("value");
		    if (attr!=null) text = attr.getNodeValue();
		    if (text==null) text = "";
		}
		// TODO: other form elements 

		// now create document elements
		if (text!=null) {
		    Element newEl = newDoc.createElement("span");
	    	    newEl.setTextContent(text);
	    	    newEl.setAttribute("class", "replaced-form-element");
	    	    el.getParentNode().replaceChild(newEl, el);
		}
	    }
	    return newDoc;
	    
	} catch (ParserConfigurationException e) {
	    // on error, return original document; no form element fix :(
	    return origDoc;
	}
    }
}
