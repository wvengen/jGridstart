package nl.nikhef.jgridstart.gui.wizard;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xhtmlrenderer.css.parser.property.PrimitivePropertyBuilders.Clear;
import org.xml.sax.SAXException;

/** Page of a request wizard.
 * <p>
 * This class returns an {@link IRequestWizard} for {@link #getWizard}, so
 * that methods specific to that class can be used without casting.
 */
public class RequestWizardPage extends TemplateWizardPage implements IRequestWizardPage {

    /** Create new page from {@linkplain URL}, which is loaded immediately.
     * 
     * @param id internal identifier
     * @todo load in background
     */
    public RequestWizardPage(String id, URL url) throws ParserConfigurationException, SAXException, IOException {
	super(id ,url);
    }
    
    /** Create new page from {@linkplain Document} */
    public RequestWizardPage(String id, Document template) {
	super(id, template);
    }
    
    @Override
    public IRequestWizard getWizard() {
	assert(wizard!=null);
	assert(wizard instanceof IRequestWizard);
	return (IRequestWizard)wizard;
    }
}
