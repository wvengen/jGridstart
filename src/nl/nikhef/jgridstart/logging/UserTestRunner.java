package nl.nikhef.jgridstart.logging;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import nl.nikhef.jgridstart.util.ConnectionUtils;
import nl.nikhef.jgridstart.util.FileUtils;

import org.apache.commons.lang.StringUtils;

/** GUI test program that runs a command and uploads the output */
public class UserTestRunner {
    
    /** GUI test program that runs unit tests */
    public static void main(String[] args) {
	new TesterFrame().setVisible(true);
    }
    
    /** Simple GUI for running and submitting tests.
     * <p>
     * To enable diagnostics on the user's computer, the tests are packaged
     * in a single testing package. A GUI is provided so that the user can
     * easily run the tests, and submit them to the jGridstart developers.
     * This allows us to gather test results on a variety of platforms.
     * 
     * @author wvengen
     */
    static class TesterFrame extends JFrame {
	/** JUnit suite to run */
	final String testclass = "nl.nikhef.jgridstart.AllTests";
	/** Where to post test result data to */
	final String url = "http://jgridstart.nikhef.nl/tests/upload.php";
	
	/** tempdir for jars */
	File tmpdir = null;
	/** Frame title */
	final String title = "jGridstart Testing Program";
	/** Label with user message */
	JLabel msg;
	/** Testing output display */
	JTextArea outputpane;
	/** Testing output display scrolled area */
	JScrollPane outputscroll;
	/** Allow upload checkbox */
	JCheckBox uploadCheck;
	/** Action button (run or upload) */
	JButton actionBtn;
	/** Quit action, stops testing process as well */
	Action quitAction;
	/** Run action */
	Action runAction;
	/** Upload action */
	Action uploadAction;
	/** Testing process */
	Process proc = null;
	/** Testing output */
	StringBuffer output;
	
	final String linesep = System.getProperty("line.separator");
	
	public TesterFrame() {
	    setTitle(title);
	    setSize(new Dimension(650, 400));
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosed(WindowEvent e) {
		    if (proc!=null) proc.destroy();
		    if (tmpdir!=null) FileUtils.recursiveDelete(tmpdir);
		    System.exit(0);
		}
	    });
	    JPanel panel = new JPanel();
	    panel.setLayout(new BorderLayout(2, 2));
	    panel.add((msg = new JLabel()), BorderLayout.NORTH);
	    JPanel cpanel = new JPanel(new BorderLayout());
	    outputpane = new JTextArea();
	    outputscroll = new JScrollPane(outputpane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    outputpane.setEditable(false);
	    cpanel.add(outputscroll, BorderLayout.CENTER);
	    uploadCheck = new JCheckBox("Submit results to developers when the tests are finished.");
	    cpanel.add(uploadCheck, BorderLayout.SOUTH);
	    uploadCheck.setSelected(true);
	    panel.add(cpanel, BorderLayout.CENTER);
	    JPanel btnpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    runAction = new AbstractAction("Run tests") {
		public void actionPerformed(ActionEvent event) {
		    runTests();
		}
	    };
	    btnpanel.add((actionBtn = new JButton(runAction)));
	    getRootPane().setDefaultButton(actionBtn);      
	    uploadAction = new AbstractAction("Upload result") {
		public void actionPerformed(ActionEvent event) {
		    doUpload();
		}
	    };
	    btnpanel.add(Box.createHorizontalStrut(5));
	    quitAction = new AbstractAction("Quit") {
		public void actionPerformed(ActionEvent event) {
		    dispose();
		}
	    };
	    btnpanel.add(new JButton(quitAction));
	    panel.add(btnpanel, BorderLayout.SOUTH);
	    setContentPane(panel);
	    setMessage("Thank you for running the jGridstart testing program. Your cooperation enables us to " +
		       "improve the software. Please press the button 'Run tests' below, then wait while the " +
		       "tests are running, until a message appears about them being done.\n" +
		       "\n" +
		       "By default, the test results shown here will be sent to the jGridstart developers. If you " +
		       "don't want this, feel free to disable it below. You will be able to upload it later.");
	}
	
	/** Set the message area to a string */
	void setMessage(String txt) {
	    msg.setText("<html><body>" +
		    "<h1>"+title+"</h1>" +		    
		    txt.replaceAll("\n", "<br>")+"<br><br></html></body>");
	}
	
	/** Start the tests */
	void runTests() {
	    if (proc!=null) return;
	    
	    try {
		setMessage("Please <i>don't touch</i> your mouse or keyboard while the tests are running...\n" +
			   "(the graphical user-interface tests require this to function)");
		runAction.setEnabled(false);
		// find java first
		String java = new File(new File(new File(System.getProperty("java.home")), "bin"), "java").getPath();
		if (System.getProperty("os.name").startsWith("Windows")) java = java + ".exe";
		// if in a jar, extract files
		String classpath = System.getProperty("java.class.path");
		URL testjars = getClass().getResource("testjars.txt");
		if (testjars != null && testjars.toString().startsWith("jar:")) {
		    tmpdir = FileUtils.createTempDir("jgridstart-testrun-");
		    BufferedReader jars = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("testjars.txt")));
		    String line;
		    classpath = "";
		    while ( (line=jars.readLine())!=null ) {
			line = StringUtils.trim(line);
			if (line.equals("")) continue;
			InputStream jarin = getClass().getResourceAsStream("/"+line);
			if (jarin==null) throw new Exception("Incomplete tests jar file: "+line);
			File jaroutfile = new File(tmpdir, line);
			jaroutfile.deleteOnExit();
			FileOutputStream jarout = new FileOutputStream(jaroutfile);
			byte[] b = new byte[1024];
			int len;
			while ( (len=jarin.read(b)) != -1) jarout.write(b, 0, len);
			jarout.close();
			jarin.close();
			classpath += jaroutfile.getAbsolutePath() + System.getProperty("path.separator");
		    }
		    // remove trailing ':'
		    classpath = classpath.substring(0, classpath.length()-System.getProperty("path.separator").length());
		    tmpdir.deleteOnExit();
		}
		// run!
		String[] cmd = new String[] {
			java,
			"-cp",
			classpath,
			"org.junit.runner.JUnitCore",
			testclass,
			"logtestlistenerevents=true"
		};
		System.out.println(StringUtils.join(cmd, " "));
		proc = Runtime.getRuntime().exec(cmd);
		
		new CaptureThread(proc.getInputStream()).start();
		new CaptureThread(proc.getErrorStream()).start();
		
	    } catch (Exception e) {
		e.printStackTrace(); // TODO finish
	    }
	}

	/** Update gui to signal that tests are done */
	void signalTestsDone() {
	    setMessage("The tests have finished.");
	    actionBtn.setAction(uploadAction);
	    if (uploadCheck.isSelected()) doUpload();
	}
	
	/** Upload test results */
	void doUpload() {
	    if (!uploadAction.isEnabled()) return;
	    uploadAction.setEnabled(false);
	    uploadCheck.setEnabled(false);
	    outputpane.append("-- Uploading data to developers"+linesep);
	    new Thread() {
		final String txt = outputpane.getText();
		@Override
		public void run() {
		    try {
			final String ret = ConnectionUtils.pageContents(
				new URL(url),
				new String[] { "testresult", txt },
				true);
			if (ret.charAt(0) == 'E')
			    throw new Exception("Upload server error: "+ret.substring(1));
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				outputpane.append(ret+linesep);
				signalUploadDone();
			    }
			});
		    } catch (final Exception e) {
			e.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				outputpane.append("-- Submission of test results failed"+linesep);
				outputpane.append(e.getLocalizedMessage()+linesep);
				setMessage("Submission of test results failed, sorry.");
				uploadAction.setEnabled(true);
			    }
			});
		    }
		}
	    }.start();
	}
	
	/** Update gui to signal that upload is done */
	void signalUploadDone() {
	    uploadAction.setEnabled(false);
	    setMessage("The test results have been uploaded. Thank you for participating!\n" +
		       "You can now close this window.");
	}
	
	/** Thread to capture an {@linkplain InputStream} and put it in the textarea */
	class CaptureThread extends Thread {
	    BufferedReader reader;
	    public CaptureThread(InputStream r) {
		reader = new BufferedReader(new InputStreamReader(r));
	    }
	    @Override
	    public void run() {
		final StringBuffer buf = new StringBuffer(128);
		final char sc = linesep.charAt(linesep.length()-1);
		try {
		    while (true) {
			int c = reader.read();
			// handle eof
			if (c < 0) break;
			// append character
			buf.append((char)c);
			// and newline appends to textarea
			if ( sc == (char)c && buf.toString().endsWith(linesep)) {
			    SwingUtilities.invokeLater(new Runnable() {
				String str = buf.toString();
				public void run() {
				    outputpane.append(str);
				}
			    });
			    buf.setLength(0);
			}
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try { reader.close(); } catch (Exception e) { }
		    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    signalTestsDone();
			}
		    });
		}
	    }
	}
    }
}
