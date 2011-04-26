/**
 * LogWindow.java
 *
 * From the book "Logging in Java with the JDK 1.4 Logging API and Apache log4j"
 * http://www.apress.com/book/view/9781590590997

 * This is an extension of the JFrame class which is used to display the logging
 * information.
 */
package nl.nikhef.jgridstart.logging;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

/** Window showing log messages */
public class LogWindow extends JFrame {
    private JTextArea textArea = null;
    private JScrollPane pane = null;

    /** Create new log window */
    public LogWindow(String title, int width, int height) {
	super(title);
	setSize(width, height);
	textArea = new JTextArea();
	textArea.setLineWrap(false);
	textArea.setEditable(false);
	pane = new JScrollPane(textArea);
	pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	getContentPane().add(pane);
    }

    /** Append a line of text to the text area
     * 
     * @param data The string to log
     */
    public void showInfo(String data) {
	textArea.append(data);
	//this.getContentPane().validate();
    }
    
    /** Copy contents of complete log window contents to clipboard */
    public void copyToClipboard() {
	StringSelection data = new StringSelection(textArea.getText());
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	clipboard.setContents(data, data);	
    }
}
