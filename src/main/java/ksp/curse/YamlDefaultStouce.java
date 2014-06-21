package ksp.curse;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.martiansoftware.jsap.DefaultSource;
import com.martiansoftware.jsap.Defaults;
import com.martiansoftware.jsap.ExceptionMap;
import com.martiansoftware.jsap.IDMap;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

public class YamlDefaultStouce implements DefaultSource {
	protected String yamlFile;

	public YamlDefaultStouce(String yamlFile) {
		this.yamlFile = yamlFile;
	}

	protected Properties getProperties(JSAPResult result) throws IOException {
		Properties props = new Properties();

		File fileToUpload = result.getFile("file");
		if (fileToUpload != null) {
			//System.out.println("User wanted to upload "
			//		+ fileToUpload.getName());
			
			// props.put("key", yamlApiKey);
		}

		return props;
	}

	/**
	 * Mostly taken from PropertyDefaultSource
	 */
	public Defaults getDefaults(IDMap idMap, ExceptionMap exceptionMap) {
		Defaults defaults = new Defaults();
		try {
			Properties properties = getProperties((JSAPResult) exceptionMap);
			for (Enumeration enumeration = properties.propertyNames(); enumeration
					.hasMoreElements();) {

				String thisName = (String) enumeration.nextElement();
				if (idMap.idExists(thisName)) {
					defaults.addDefault(thisName,
							properties.getProperty(thisName));
				} else {
					String paramID = idMap.getIDByLongFlag(thisName);
					if (paramID != null) {
						defaults.addDefault(paramID,
								properties.getProperty(thisName));
					} else if (thisName.length() == 1) {
						paramID = idMap.getIDByShortFlag(thisName.charAt(0));
						if (paramID != null) {
							defaults.addDefault(paramID,
									properties.getProperty(thisName));
						} else {
							exceptionMap.addException(null, new JSAPException(
									"Unknown parameter: " + thisName));
						}
					} else {
						exceptionMap.addException(null, new JSAPException(
								"Unknown parameter: " + thisName));
					}
				}
			}
		} catch (IOException ioe) {
			exceptionMap.addException(null, new JSAPException(
					"Unable to load properties.", ioe));
		}
		return (defaults);
	}

}
