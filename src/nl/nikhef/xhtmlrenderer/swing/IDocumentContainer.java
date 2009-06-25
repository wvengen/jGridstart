package nl.nikhef.xhtmlrenderer.swing;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.NamespaceHandler;

/** {@link XHTMLPanel} methods related to the {@link Document}. */
public interface IDocumentContainer {
    /**
     * Loads and renders a Document given a uri.
     * The uri is resolved by the UserAgentCallback
     *
     * @param uri
     */
    public void setDocument(String uri);
    
    /**
     * Renders an XML Document instance.
     * Make sure that no relative resources are needed
     *
     * @param doc The document to render.
     */
    public void setDocument(Document doc);
    
    /**
     * Renders a Document using a URL as a base URL for relative
     * paths.
     *
     * @param doc The new document value
     * @param url The new document value
     */
    public void setDocument(Document doc, String url);
    
    /**
     * Renders a Document read from an InputStream using a URL
     * as a base URL for relative paths.
     *
     * @param stream The stream to read the Document from.
     * @param url    The URL used to resolve relative path references.
     */
    // TODO: should throw more specific exception (PWW 25/07/2006)
    public void setDocument(InputStream stream, String url) throws Exception;

    public void setDocument(InputStream stream, String url, NamespaceHandler nsh);
    public void setDocumentFromString(String content, String url, NamespaceHandler nsh);
    public void setDocument(String url, NamespaceHandler nsh);

    /**
     * Reloads the document using the same base URL and namespace handler. Reloading will pick up changes to styles
     * within the document.
     *
     * @param URI A URI for the Document to load, for example, file.toURL().toExternalForm().
     */
    public void reloadDocument(String URI);

    /**
     * Reloads the document using the same base URL and namespace handler. Reloading will pick up changes to styles
     * within the document.
     *
     * @param doc The document to reload.
     */
    public void reloadDocument(Document doc);

    public URL getURL();
    public Document getDocument();
    
    /**
     * Returns the title as reported by the NamespaceHandler assigned to the SharedContext in this panel. For an HTML
     * document, this will be the contents of /html/head/title.
     *
     * @return the document title, or "" if the namespace handler cannot find a title, or if there is no current document
     * in the panel.
     */
    public String getDocumentTitle();
    
    /**
     * Renders a Document read from an InputStream using a URL
     * as a base URL for relative paths.
     *
     * @param file The file to read the Document from. Relative paths
     *             will be resolved based on the file's parent directory.
     */
    // TODO: should throw more specific exception (PWW 25/07/2006)
    public void setDocument(File file) throws Exception;

}
