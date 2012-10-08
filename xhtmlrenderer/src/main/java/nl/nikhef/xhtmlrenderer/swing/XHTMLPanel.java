package nl.nikhef.xhtmlrenderer.swing;

import java.awt.Font;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Iterator;
import java.util.List;

import javax.swing.UIManager;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.newmatch.Selector;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.NaiveUserAgent;

/** An {@link org.xhtmlrenderer.simple.XHTMLPanel} with minor tweaks.
 * <p>
 * This class sets the font from the system's default dialog font so
 * it blends nicely into the user-interface.
 * <p>
 * The method {@link #replaceLinkListener} is added for convenience.
 * <p>
 * If no {@linkplain UserAgentCallback} is supplied on creation, a default
 * one is installed that resolves {@literal resources:<path>} uri's to the file
 * <tt><classpath>/resources/<path></tt>. 
 * 
 * @author wvengen
 */
public class XHTMLPanel extends org.xhtmlrenderer.simple.XHTMLPanel implements IXHTMLPanel {

    public XHTMLPanel() {
	super(new ResourceUserAgent());
	initialize_tweaks();
    }

    public XHTMLPanel(UserAgentCallback uac) {
	super(uac);
	initialize_tweaks();
    }

    /** Replace the listener that is activated when a link is clicked.
     * <p>
     * This is a convenience method that first removes all LinkListeners, and
     * then adds the supplied one.
     * 
     * @param llnew New LinkListener to use for this panel
     */
    @SuppressWarnings("unchecked") // getMouseTrackingListeners() returns unchecked List
    public void replaceLinkListener(LinkListener llnew) {
	// remove all existing LinkListeners
	List<FSMouseListener> ls = (List<FSMouseListener>)getMouseTrackingListeners();
	for (Iterator<FSMouseListener> it = ls.iterator(); it.hasNext(); ) {
	    FSMouseListener l = it.next();
	    if (l instanceof LinkListener)
		removeMouseTrackingListener(l);
	}
	// and add new one
	addMouseTrackingListener(llnew);
    }    
    
    /** print the contents of this pane with the smallest possible printer margins;
     * a print dialog is shown first.
     * 
     * @throws PrinterException
     */
    public boolean print() throws PrinterException {
	PrinterJob printJob = PrinterJob.getPrinterJob();
	PageFormat format = printJob.defaultPage();
	// nuke margins, we can set them in xhtmlrenderer and need a page fit 
	Paper paper = format.getPaper();
	paper.setImageableArea(1e-3d, 1e-3d, Double.MAX_VALUE, Double.MAX_VALUE);
	format.setPaper(paper);
	format = printJob.validatePage(format);
	// print with dialog
        printJob.setPrintable(new XHTMLPrintable(this), format);
        if (printJob.printDialog()) {
            format = printJob.validatePage(format);
            printJob.setPrintable(new XHTMLPrintable(this), format);
            printJob.print();
        }
	return true;
    }
    
    /** Setup the XHTMPanel tweaks */
    private void initialize_tweaks() {
	getSharedContext().setNamespaceHandler(new XhtmlNamespaceHandler() {
	    private StylesheetInfo _defaultStylesheet = null;
	    
	    @Override
	    public StylesheetInfo getDefaultStylesheet(StylesheetFactory factory) {
		if (_defaultStylesheet == null) {
		    // get default font properties
		    Font font = getFont();
		    // workaround Linux where it seems too large
		    String osName = System.getProperty("os.name");
		    if (!osName.startsWith("Mac OS") && !osName.startsWith("Windows"))
			font = font.deriveFont(font.getSize2D()*0.88f);
		    
		    // add to UA stylesheet
		    _defaultStylesheet = super.getDefaultStylesheet(factory);
		    
		    Ruleset fontRule = new Ruleset(StylesheetInfo.USER_AGENT);
		    PropertyValue family = new PropertyValue(PropertyValue.CSS_STRING, font.getName(), font.getName());
		    PropertyValue size = new PropertyValue(PropertyValue.CSS_PT, font.getSize2D(), font.getSize2D()+"pt");
		    fontRule.addProperty(new PropertyDeclaration(CSSName.FONT_FAMILY, family, false, StylesheetInfo.USER_AGENT));
		    fontRule.addProperty(new PropertyDeclaration(CSSName.FONT_SIZE, size, false, StylesheetInfo.USER_AGENT));
		    if (font.isBold()) {
			PropertyValue bold = new PropertyValue(PropertyValue.CSS_STRING, "bold", "bold");
			fontRule.addProperty(new PropertyDeclaration(CSSName.FONT_WEIGHT, bold, false, StylesheetInfo.USER_AGENT));
		    }
		    if (font.isItalic()) {
			PropertyValue italic = new PropertyValue(PropertyValue.CSS_STRING, "italic", "italic");
			fontRule.addProperty(new PropertyDeclaration(CSSName.FONT_STYLE, italic, false, StylesheetInfo.USER_AGENT));
		    }
		    
		    // get colours from UIManager; can still override in user stylesheet
		    java.awt.Color fg = UIManager.getColor("Panel.foreground");
		    PropertyValue fgCol = new PropertyValue(new FSRGBColor(fg.getRed(), fg.getGreen(), fg.getBlue()));
		    fontRule.addProperty(new PropertyDeclaration(CSSName.COLOR, fgCol, false, StylesheetInfo.USER_AGENT));
		    java.awt.Color bg = UIManager.getColor("Panel.background");
		    PropertyValue bgCol = new PropertyValue(new FSRGBColor(bg.getRed(), bg.getGreen(), bg.getBlue()));
		    fontRule.addProperty(new PropertyDeclaration(CSSName.BACKGROUND_COLOR, bgCol, false, StylesheetInfo.USER_AGENT));
		    
		    Selector bodySelector = new Selector();
		    bodySelector.setName("body");
		    bodySelector.setParent(fontRule);
		    fontRule.addFSSelector(bodySelector);
		    _defaultStylesheet.getStylesheet().addContent(fontRule);
		}
		return _defaultStylesheet;
	    }
	});
    }

    /** XHTMLRenderer user-agent that knows the {@literal resource:} protocol
     * <p>
     * This protocol locates resources using the system classloader. In this
     * way, one can use the url {@literal resources:css/style.css} to find
     * the file {@literal /resources/css/style.css} in the classpath and use
     * that as the referenced file.
     * <p>
     * XHTMLPanel's ClassLoader is used at the moment.
     */
    static public class ResourceUserAgent extends NaiveUserAgent {
	@Override
	public String resolveURI(String uri) {
	    // handle resource: protocol
	    if (uri!=null && uri.startsWith("resources:")) {
		uri = "/resources/"+uri.substring(10);
		uri = XHTMLPanel.class.getResource(uri).toExternalForm();
	    }
	    return super.resolveURI(uri);
	}
    }
}
