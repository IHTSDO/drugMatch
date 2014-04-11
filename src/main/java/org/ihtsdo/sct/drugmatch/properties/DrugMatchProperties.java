package org.ihtsdo.sct.drugmatch.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.ihtsdo.sct.drugmatch.enumeration.ReturnCode;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author dev-team@carecom.dk
 *
 */
public class DrugMatchProperties {

	private static final Logger log = LoggerFactory.getLogger(DrugMatchProperties.class);

	public static final String 
			ATTRIBUTE_ID_HAS_ACTIVE_INGREDIENT = "attribute_id.has_active_ingredient",
			ATTRIBUTE_ID_HAS_DOSE_FORM = "attribute_id.has_dose_form",
			
			CONSTRAINT_ID_DOSE_FORM = "constraint_id.dose_form",
			CONSTRAINT_ID_SUBSTANCE = "constraint_id.substance",
			CONSTRAINT_ID_UNIT = "constraint_id.unit",
			
			INPUT_FILE = "input.file",
			INPUT_FILE_INCLUDE_FIRST_LINE = "input.file.includeFirstLine",
			INPUT_FILE_QUOTE_CHARACTER = "file.content.quoteCharacter",
			FILE_CONTENT_SEPARATOR_CHARACTER = "file.content.separatorCharacter",
			
			NATIONAL_NAMESPACE_ID = "national.namespace_id",
			
			OUTPUT_DIR = "output.dir",
			
			SETTING_FILE = "setting.file",
			
			STRICT_MODE = "strict_mode",
			
			VERIFICATION_LOGIN = "verification.login",
			VERIFICATION_PASSWORD = "verification.password",
			VERIFICATION_SERVICE = "verification.service";

	private static Properties properties;

	public DrugMatchProperties() {
		if (properties == null) {
			loadProperties();
		}
	}

	/**
	 * Attempt to load the configuration file, if
	 * it cannot be loaded a shutdown is performed.
	 */
	private static synchronized void loadProperties() {
		if (properties == null) { // check condition again, to avoid unneeded execution
			String settingFilePath = System.getProperty(SETTING_FILE);
			if (settingFilePath == null) {
				log.error("Settings not set: {}={}", SETTING_FILE, settingFilePath);
				System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
			} else {
				File propertyFile = new File(settingFilePath);
				if (propertyFile.exists()) {
					try (FileInputStream fis = new FileInputStream(propertyFile)) {
						properties = new Properties();
						properties.load(fis);
					} catch (FileNotFoundException e) {
						log.error("Unable to locate file: {}", propertyFile);
						System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
					} catch (IOException e) {
						log.error("Unable to read file: {}", propertyFile);
						System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
					}
				} else {
					log.error("Settings not found: {}={}", SETTING_FILE, propertyFile);
					System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
				}
			}
		}
	}

	/**
	 * @param propertyName
	 * @return trimmed value, or null if missing or empty
	 */
	protected String getStringProperty(String propertyName) {
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

	public Long getAttributeIdHasActiveIngredient() {
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

	public Long getAttributeIdHasDoseForm() {
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

	public Long getConstraintIdDoseForm() {
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

	public Long getConstraintIdSubstance() {
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

	public Long getConstraintIdUnit() {
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

	public String getFileContentQuoteCharacter() {
		return getStringProperty(INPUT_FILE_QUOTE_CHARACTER);
	}

	public String getFileContentSeparatorCharacter() {
		return getStringProperty(FILE_CONTENT_SEPARATOR_CHARACTER);
	}

	public String getInputFilePath() {
		return getStringProperty(INPUT_FILE);
	}

	public String getInputFileIncludeFirstLine() {
		return getStringProperty(INPUT_FILE_INCLUDE_FIRST_LINE);
	}

	public String getNationalNamespaceId() {
		return getStringProperty(NATIONAL_NAMESPACE_ID);
	}

	public File getOutputDirectory() throws DrugMatchConfigurationException {
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

	public String getVerificationLogin() {
		return getStringProperty(VERIFICATION_LOGIN);
	}

	public String getVerificationPassword() {
		return getStringProperty(VERIFICATION_PASSWORD);
	}

	/**
	 * @return verification service host URL, without trailing /.
	 */
	public String getVerificationService() {
		String s = getStringProperty(VERIFICATION_SERVICE);
		if (s != null) {
			if (s.endsWith("/")) {
				return s.substring(0, (s.length() - 1));
			}
		}
		return s;
	}

	/**
	 * Treat warnings as errors
	 */
	public boolean isStrictMode() {
		return Boolean.parseBoolean(getStringProperty(STRICT_MODE));
	}
}
