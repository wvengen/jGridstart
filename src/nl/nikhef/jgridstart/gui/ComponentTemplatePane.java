package nl.nikhef.jgridstart.gui;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.NoNamespaceHandler;

import nl.nikhef.jgridstart.gui.util.BareBonesBrowserLaunch;

/** An HTML JTextPane that reads from a file and substitutes variables that
 * are supplied as Properties.
 * 
 * TODO split in template and gui part
 * TODO add proper parser for more complex if statements
 * TODO add something that creates links automatically on relevant items
 * 
 * @author wvengen
 *
 */
public class ComponentTemplatePane extends /*JTextPane*/ XHTMLPanel {
    
    protected URL src = null;
    protected Properties p = null;
    
    /** Create a new template pane */
    public ComponentTemplatePane() {
	super();
	p = new Properties();
	setPreferredSize(new Dimension(400, 200));
	//setContentType("text/html");
	//setEditable(false);
	setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
	/*addHyperlinkListener(new HyperlinkListener() {
	    public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		    BareBonesBrowserLaunch.openURL(e.getURL());
		}
	    }
	});*/
    }

    /** Create a new template pane from a template; it will not be shown until
     * a call to refresh() or setProperties(). */
    public ComponentTemplatePane(URL src) {
	this();
	setTemplate(src);
    }
    
    /** Set the template. It will not be shown until a call to refresh() or
     * setProperties(). */
    public void setTemplate(URL src) {
	this.src = src;
    }
    
    /** Set the properties for the template and refresh. This will replace any
     * previously set Properties object, and changes to the object will be
     * reflected in the output after a refresh(). */
    public void setProperties(Properties p) {
	this.p = p;
	refresh();
    }
    
    /** refresh the information on the template */
    public void refresh() {
	String doc = "";
	try {
	    // read data
	    InputStream in = src.openStream();
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String line;
	    while ((line = br.readLine()) != null)
		doc += line;
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	// do substitutions 
	StringBuffer dstbuf = new StringBuffer();
	StringBuffer nullbuf = new StringBuffer();
	StringBuffer curbuf = dstbuf;
	final Pattern pat = Pattern.compile("(\\$\\{(.*?)\\})", Pattern.MULTILINE|Pattern.DOTALL);
	Matcher match = pat.matcher(doc);
	ArrayList<Boolean> conditionals = new ArrayList<Boolean>();
	while (match.find()) {
	    String key = match.group(2).trim();
	    // handle conditionals
	    if (key.startsWith("if ")) {
		match.appendReplacement(curbuf, "");
		key = key.substring(2).trim();
		if (key.startsWith("!")) {
		    key = key.substring(1).trim();
		    conditionals.add(p.getProperty(key)==null);
		} else {
		    conditionals.add(p.getProperty(key)!=null);
		}
		if (conditionals.contains(Boolean.FALSE))
		    curbuf = nullbuf;
		else
		    curbuf = dstbuf;
	    } else if (key.equals("endif")) {
		match.appendReplacement(curbuf, "");
		conditionals.remove(conditionals.size()-1);
		if (conditionals.contains(Boolean.FALSE))
		    curbuf = nullbuf;
		else
		    curbuf = dstbuf;
	    } else {
		// output
		String sub = p.getProperty(key);
		if (sub==null) sub="";
		match.appendReplacement(curbuf, sub);
	    }
	}
	match.appendTail(dstbuf);

	// and update user-interface
	//setText(dstbuf.toString());
	//setDocumentFromString(dstbuf.toString(), "", new NoNamespaceHandler());
	try {
	    setDocument(new StringBufferInputStream(dstbuf.toString()), "");
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
