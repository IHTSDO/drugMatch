package org.ihtsdo.sct.drugmatch.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author dev-team@carecom.dk
 */
public final class DrugMatchProperties {

	private static final Logger log = LoggerFactory.getLogger(DrugMatchProperties.class);

	public static final String
			ATTRIBUTE_ID_HAS_ACTIVE_INGREDIENT = "attribute_id.has_active_ingredient",
			ATTRIBUTE_ID_HAS_DOSE_FORM = "attribute_id.has_dose_form",

			CONSTRAINT_ID_DOSE_FORM = "constraint_id.dose_form",
			CONSTRAINT_ID_SUBSTANCE = "constraint_id.substance",
			CONSTRAINT_ID_UNIT = "constraint_id.unit",

			GENERIC_REPORT = "generic_report",

			FILE_CONTENT_SEPARATOR_CHARACTER = "file.content.separatorCharacter",
			INPUT_FILE = "input.file",
			INPUT_FILE_INCLUDE_FIRST_LINE = "input.file.includeFirstLine",
			INPUT_FILE_QUOTE_CHARACTER = "file.content.quoteCharacter",

			NATIONAL_NAMESPACE_ID = "national.namespace_id",

			OUTPUT_DIR = "output.dir",

			SETTING_FILE = "setting.file",

			STRICT_MODE = "strict_mode",

			VERIFICATION_LOGIN = "verification.login",
			VERIFICATION_PASSWORD = "verification.password",
			VERIFICATION_SERVICE = "verification.service";

	private static Properties properties;

	private DrugMatchProperties() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempt to load the configuration file.
	 * @throws DrugMatchConfigurationException
	 */
	private static synchronized void loadProperties() throws DrugMatchConfigurationException {
		if (properties == null) { // check condition again, to avoid unneeded execution
			String settingFilePath = System.getProperty(SETTING_FILE);
			if (settingFilePath == null) {
				throw new DrugMatchConfigurationException("Settings not set: " + SETTING_FILE);
			} // else
			File propertyFile = new File(settingFilePath);
			if (propertyFile.exists()) {
				try (FileInputStream fis = new FileInputStream(propertyFile)) {
					properties = new Properties();
					properties.load(fis);
				} catch (FileNotFoundException e) {
					throw new DrugMatchConfigurationException("Unable to locate file: " + propertyFile);
				} catch (IOException e) {
					throw new DrugMatchConfigurationException("Unable to read file: " + propertyFile);
				}
			} else {
				throw new DrugMatchConfigurationException("Settings not found: " + SETTING_FILE + "=" + propertyFile);
			}
		}
	}

	/**
	 * @param propertyName
	 * @return trimmed value, or null if missing or empty
	 * @throws DrugMatchConfigurationException
	 */
	private static String getStringProperty(final String propertyName) throws DrugMatchConfigurationException {
		if (properties == null) {
			loadProperties();
		}
		String propertyValue = properties.getProperty(propertyName);
		if (propertyValue == null) {
			log.debug("Property: {} not set!", propertyName);
			return null;
		} // else
		propertyValue = propertyValue.trim();
		if (propertyValue.length() == 0) {
			log.debug("Property: {} empty value!", propertyName);
			return null;
		} // else
		return propertyValue;
	}

	/**
	 * Use generic messages in reports.
	 * @throws DrugMatchConfigurationException
	 */
	public static boolean createGenericReport() throws DrugMatchConfigurationException {
		return Boolean.parseBoolean(getStringProperty(GENERIC_REPORT));
	}

	public static Long getAttributeIdHasActiveIngredient() throws DrugMatchConfigurationException {
		String id = getStringProperty(ATTRIBUTE_ID_HAS_ACTIVE_INGREDIENT);
		try {
			if (id != null) {
				return Long.valueOf(id);
			}
		} catch (NumberFormatException e) {
			log.debug("Unable to parse value: {} for: {}", id, ATTRIBUTE_ID_HAS_ACTIVE_INGREDIENT);
		}
		return null;
	}

	public static Long getAttributeIdHasDoseForm() throws DrugMatchConfigurationException {
		String id = getStringProperty(ATTRIBUTE_ID_HAS_DOSE_FORM);
		try {
			if (id != null) {
				return Long.valueOf(id);
			}
		} catch (NumberFormatException e) {
			log.debug("Unable to parse value: {} for: {}", id, ATTRIBUTE_ID_HAS_DOSE_FORM);
		}
		return null;
	}

	public static Long getConstraintIdDoseForm() throws DrugMatchConfigurationException {
		String id = getStringProperty(CONSTRAINT_ID_DOSE_FORM);
		try {
			if (id != null) {
				return Long.valueOf(id);
			}
		} catch (NumberFormatException e) {
			log.debug("Unable to parse value: {} for: {}", id, CONSTRAINT_ID_DOSE_FORM);
		}
		return null;
	}

	public static Long getConstraintIdSubstance() throws DrugMatchConfigurationException {
		String id = getStringProperty(CONSTRAINT_ID_SUBSTANCE);
		try {
			if (id != null) {
				return Long.valueOf(id);
			}
		} catch (NumberFormatException e) {
			log.debug("Unable to parse value: {} for: {}", id, CONSTRAINT_ID_SUBSTANCE);
		}
		return null;
	}

	public static Long getConstraintIdUnit() throws DrugMatchConfigurationException {
		String id = getStringProperty(CONSTRAINT_ID_UNIT);
		try {
			if (id != null) {
				return Long.valueOf(id);
			}
		} catch (NumberFormatException e) {
			log.debug("Unable to parse value: {} for: {}", id, CONSTRAINT_ID_UNIT);
		}
		return null;
	}

	public static String getFileContentQuoteCharacter() throws DrugMatchConfigurationException {
		return getStringProperty(INPUT_FILE_QUOTE_CHARACTER);
	}

	public static String getFileContentSeparatorCharacter() throws DrugMatchConfigurationException {
		return getStringProperty(FILE_CONTENT_SEPARATOR_CHARACTER);
	}

	public static String getInputFilePath() throws DrugMatchConfigurationException {
		return getStringProperty(INPUT_FILE);
	}

	public static String getInputFileIncludeFirstLine() throws DrugMatchConfigurationException {
		return getStringProperty(INPUT_FILE_INCLUDE_FIRST_LINE);
	}

	public static String getNationalNamespaceId() throws DrugMatchConfigurationException {
		return getStringProperty(NATIONAL_NAMESPACE_ID);
	}

	public static File getOutputDirectory() throws DrugMatchConfigurationException {
		File outputDir = new File(getStringProperty(OUTPUT_DIR));
		if (!outputDir.exists()) {
			if (!outputDir.mkdirs()) {
				throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.OUTPUT_DIR + "'='" + outputDir + "' isn't writeable");
			}
			log.debug("Output directory: {} created", outputDir);
		}
		if (!outputDir.canWrite()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.OUTPUT_DIR + "'='" + outputDir + "' isn't writeable");
		}
		return outputDir;
	}

	public static String getVerificationLogin() throws DrugMatchConfigurationException {
		return getStringProperty(VERIFICATION_LOGIN);
	}

	public static String getVerificationPassword() throws DrugMatchConfigurationException {
		return getStringProperty(VERIFICATION_PASSWORD);
	}

	/**
	 * @return verification service host URL, without trailing /.
	 * @throws DrugMatchConfigurationException
	 */
	public static String getVerificationService() throws DrugMatchConfigurationException {
		String s = getStringProperty(VERIFICATION_SERVICE);
		if (s != null
				&& s.endsWith("/")) {
			return s.substring(0, (s.length() - 1));
		}
		return s;
	}

	/**
	 * Treat warnings as errors.
	 * @return true or false
	 * @throws DrugMatchConfigurationException
	 */
	public static boolean isStrictMode() throws DrugMatchConfigurationException {
		return Boolean.parseBoolean(getStringProperty(STRICT_MODE));
	}
}
