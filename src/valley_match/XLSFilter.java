package valley_match;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/* 
 * Valley Match Services
 * XLSFilter
 * (c) 2016 Matthew R. Manzi
 * 
 * Supplemental class used to only allow the admin to import '.xls' files
 * into the program
 * @author matteomanzi
 * @version 1.0 --- Jan 25, 2016
 * @version 1.0.1 --- Jan 30, 2016 -- Allows admin to navigate into folders
 * 									  to select '.xls' files
 * 
 */
public class XLSFilter extends FileFilter {

	private final String acceptedExtension = "xls";
	
	public XLSFilter() {	}
	
	@Override
	public boolean accept(File file) {
		
		if (file.getName().endsWith(acceptedExtension) ||
				file.isDirectory()) {						// Update 1.0.1 -- Added "|| file.isDirectory()" to allow folders to be opened
			return true;
		}
		
		return false;
	}

	@Override
	public String getDescription() {
		return ".xls files";
	}
	
}
