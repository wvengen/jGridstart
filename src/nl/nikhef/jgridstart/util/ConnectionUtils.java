package nl.nikhef.jgridstart.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class ConnectionUtils {
    
    
    
    /** Return a Reader for a URL */
    public static Reader pageReader(URL url) throws IOException {
	return pageReader(url, (String)null, false);
    }
    /** Return the contents of a URL */
    public static String pageContents(URL url) throws IOException {
	return pageContents(url, (String)null, false);
    }
   
    /** Return a reader for a URL with pre/post data
     * 
     * @param url URL to submit to
     * @param data Array of "key=value" strings
     * @param post true to post data, false for get
     * @return reader for reading from the URL
     * @throws IOException 
     */
    public static Reader pageReader(URL url, String[] data, boolean post) throws IOException {
	return pageReader(url, createQueryString(data), post);
    }
    /** Return the contents of a URL with pre/post data
     * 
     * @param url URL to submit to
     * @param data Array of "key=value" strings
     * @param post true to post data, false for get
     * @return reader for reading from the URL
     * @throws IOException 
     */
    public static String pageContents(URL url, String[] data, boolean post) throws IOException {
	return pageContents(url, createQueryString(data), post);
    }
    
    /** Return the contents of a URL with pre or post data
     * 
     * @param url URL to submit to
     * @param data String of "key=value&amp;otherkey=othervalue" post data
     * @param post true to post data, false for get
     * @return data returned by server
     * @throws IOException
     */
    public static String pageContents(URL url, String data, boolean post) throws IOException {
	Reader reader = pageReader(url, data, post);
	StringBuffer result = new StringBuffer();
	BufferedReader breader = new BufferedReader(reader);
	String line = null;
	while ((line = breader.readLine()) != null) {
	    result.append(line);
	    result.append(System.getProperty("line.separator"));
	}
	return result.toString();
    }
    /** Return a Reader for a URL with pre or post data
     * 
     * @param url URL to submit to
     * @param data String of "key=value&amp;otherkey=othervalue" post data
     * @param post true to post data, false for get
     * @return data returned by server
     * @throws IOException
     */
    public static Reader pageReader(URL url, String data, boolean post) throws IOException {
	URLConnection conn = null;
	if (data != null) {
	    if (post) {
		// post: write data to stream
		conn = url.openConnection();
		// send post data if present
		if (post && data != null) {
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.close();
		}
	    } else {
		// pre: put data in URL
		url = new URL(url, "?" + data);
		conn = url.openConnection();
	    }
	} else
	    conn = url.openConnection();
	// return reader for response
	return new InputStreamReader(conn.getInputStream());
    }
    
    /** Return a query string from arguments for pre or post
     * 
     * @param data Array of "key=value" Strings
     * @return single String with urlencoded data
     * @throws UnsupportedEncodingException 
     */
    protected static String createQueryString(String[] data) throws UnsupportedEncodingException {
	String sdata = "";
	for (int i=0; i<data.length; i++)
	    sdata += "&amp;" + URLEncoder.encode(data[i], "UTF-8");
	sdata = sdata.substring("&amp;".length());
	return sdata;
    }
}
