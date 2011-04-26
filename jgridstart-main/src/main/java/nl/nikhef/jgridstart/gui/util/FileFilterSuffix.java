package nl.nikhef.jgridstart.gui.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/** {@link FileFilter} that works with suffixes.
 * <p>
 * It is easily created with a description and comma-separated list of
 * extensions. The description shown in the dialog then shows the
 * original description with a list of extensions appended. E.g:
 * <code>
 *   new FileFilterSuffix("Foo File", ".foo,.bar");
 * </code>
 * Yields {@literal "Foo File (*.foo, *.bar)"} as the description in the
 * {@linkplain javax.swing.JFileChooser JFileChooser}.
 * 
 * @author wvengen
 */
public class FileFilterSuffix extends FileFilter {
    private String description = null;
    private String[] extensions = {};

    /** Create new {@linkplain FileFilterSuffix}
     * 
     * @param desc Description
     * @param ext Comma-separated list of extensions
     */
    public FileFilterSuffix(String desc, String ext) {
	description = desc;
	extensions = ext.split(",");
    }

    @Override
    public boolean accept(File f) {
	// need to be able to select dirs or the user wouldn't be able to browse them!
	if (f.isDirectory())
	    return true;

	String lcname = f.getName().toLowerCase();
	for (int i = 0; i < extensions.length; i++)
	    if (lcname.endsWith(extensions[i]))
		return true;

	return false;
    }

    @Override
    public String getDescription() {
	String desc = description;
	desc += " (";
	for (int i = 0; i < extensions.length; i++)
	    desc += "*" + extensions[i] + ", ";
	desc = desc.substring(0, desc.length() - 2) + ")";
	return desc;
    }
    
    public String[] getExtensions() {
	return extensions;
    }
}
