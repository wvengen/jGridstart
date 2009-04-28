package nl.nikhef.jgridstart.gui.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.lang.reflect.Field;
import java.util.List;

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

class TemplatePrintable extends XHTMLPrintable {

    public TemplatePrintable(XHTMLPanel panel) {
	super(panel);
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) {
	if (g2r==null) {
            g2r = new Graphics2DRenderer();
            g2r.getSharedContext().setPrint(true);
            g2r.getSharedContext().setInteractive(false);
            g2r.getSharedContext().setDPI(72f);
            g2r.getSharedContext().getTextRenderer().setSmoothingThreshold(0);
            g2r.getSharedContext().setUserAgentCallback(panel.getSharedContext().getUserAgentCallback());
            g2r.setDocument(panel.getDocument(), panel.getSharedContext().getUac().getBaseURL());
            g2r.getSharedContext().setReplacedElementFactory(panel.getSharedContext().getReplacedElementFactory());
            fixPageInfo(g2r.getSharedContext().getCss(), pf, g2r.getSharedContext().getDPI());
            g2r.layout((Graphics2D)g, null);
            g2r.getPanel().assignPagePrintPositions((Graphics2D)g);
	}
	return super.print(g, pf, page);
    }
    
    @SuppressWarnings("unchecked")
    protected void fixPageInfo(StyleReference style, PageFormat pf, float dpi) {
	try {
	    final Field matcherField = StyleReference.class.getDeclaredField("_matcher");
	    matcherField.setAccessible(true);
	    final Matcher matcher = (Matcher) matcherField.get(style);
	    final Field pageRulesField = Matcher.class.getDeclaredField("_pageRules");
	    pageRulesField.setAccessible(true);
	    final List pageRules = (List) pageRulesField.get(matcher);
	    if (!pageRules.isEmpty()) {
		final PageRule pageRule = (PageRule) pageRules.get(0);
		final Ruleset ruleset = pageRule.getRuleset();
		final List<PropertyDeclaration> declarations = ruleset.getPropertyDeclarations();
		for (PropertyDeclaration declaration : declarations) {
		    final CSSPrimitiveValue value = declaration.getValue();
		    final Field floatValueField = PropertyValue.class.getDeclaredField("_floatValue");
		    floatValueField.setAccessible(true);
		    //if (declaration.getPropertyName().equals())
		    floatValueField.setFloat(value, (float)pf.getImageableX() / dpi); // FIXME margin-top != margin-left, ...
		}
	    }
	} catch (Exception e) { 
	    // TODO warn
	    e.printStackTrace();
	}
    }
}
