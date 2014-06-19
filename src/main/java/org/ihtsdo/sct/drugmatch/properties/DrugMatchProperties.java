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
			ATTRIBUTE_ID_HAS_ACTIVE_INGREDIENT = "sct.attribute_id.has_active_ingredient",
			ATTRIBUTE_ID_HAS_DOSE_FORM = "sct.attribute_id.has_dose_form",

			CONSTRAINT_ID_DOSE_FORM = "sct.constraint_id.dose_form",
			CONSTRAINT_ID_SUBSTANCE = "sct.constraint_id.substance",
			CONSTRAINT_ID_UNIT = "sct.constraint_id.unit",

			GENERIC_REPORT = "generic_report",

			FILE_CONTENT_SEPARATOR_CHARACTER = "file.content.separator_character",
			FILE_QUOTE_CHARACTER = "file.content.quote_character",

			INPUT_FILE = "input.file",
			INPUT_FILE_INCLUDE_FIRST_LINE = "input.file.include_first_line",

			MODULE_ID = "sct.module_id",

			EXTENSION_LANGUAGE_CODE = "sct.extension.language_code",
			EXTENSION_NAMESPACE_ID = "sct.extension.namespace_id",

			OUTPUT_DIR = "output.dir",

			SCT_ID_SERVICE = "sct.id.service",
			SCT_QUANTITY_REFERENCE_SET_ID = "sct.quantity_reference_set_id",
			SCT_RELEASE_ID = "sct.release_id",

			SETTING_FILE = "setting.file",

			STRICT_MODE = "strict_mode",

			VERIFICATION_LOGIN = "verification.login",
			VERIFICATION_PASSWORD = "verification.password",
			VERIFICATION_SERVICE = "verification.service";

	private static Properties properties;

	/**
	 * DON'T INSTANTIATE A STATIC HELPER!
	 */
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
	 * @return use generic messages in reports, otherwise false.
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
		return getStringProperty(FILE_QUOTE_CHARACTER);
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

	public static File getMappingDirectory() throws DrugMatchConfigurationException {
		File mappingDir = new File(getOutputDirectory().getPath() + File.separator + "mapping");
		if (!mappingDir.exists()) {
			if (!mappingDir.mkdirs()) {
				throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + mappingDir + "' isn't writeable");
			}
			log.debug("Mapping directory: {} created", mappingDir);
		}
		if (!mappingDir.canWrite()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + mappingDir + "' isn't writeable");
		}
		return mappingDir;
	}

	public static String getModuleId() throws DrugMatchConfigurationException {
		return getStringProperty(MODULE_ID);
	}

	public static String getNationalLanguageCode() throws DrugMatchConfigurationException {
		return getStringProperty(EXTENSION_LANGUAGE_CODE);
	}

	public static String getNationalNamespaceId() throws DrugMatchConfigurationException {
		return getStringProperty(EXTENSION_NAMESPACE_ID);
	}

	/**
	 * Note, trims trailing file separator from path
	 * @return {@link File}
	 * @throws DrugMatchConfigurationException
	 */
	public static File getOutputDirectory() throws DrugMatchConfigurationException {
		String path = getStringProperty(OUTPUT_DIR);
		if (path == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.OUTPUT_DIR + "' isn't set!");
		} // else
		// trim trailing file separator
		if (path.endsWith(File.separator)) {
			path = path.substring(0, (path.length() - 1));
		}
		File outputDir = new File(path);
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

	public static String getQuantityReferenceSetId() throws DrugMatchConfigurationException {
		return getStringProperty(SCT_QUANTITY_REFERENCE_SET_ID);
	}

	public static File getReferenceSetContentDirectory() throws DrugMatchConfigurationException {
		File refSetContentDir = new File(getReferenceSetDirectory().getPath() + File.separator + "Content");
		if (!refSetContentDir.exists()) {
			if (!refSetContentDir.mkdirs()) {
				throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + refSetContentDir + "' isn't writeable");
			}
			log.debug("Reference Set Content directory: {} created", refSetContentDir);
		}
		if (!refSetContentDir.canWrite()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + refSetContentDir + "' isn't writeable");
		}
		return refSetContentDir;
	}

	public static File getReferenceSetDirectory() throws DrugMatchConfigurationException {
		File refSetDir = new File(getReleaseFormat2Directory().getPath() + File.separator + "Refset");
		if (!refSetDir.exists()) {
			if (!refSetDir.mkdirs()) {
				throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + refSetDir + "' isn't writeable");
			}
			log.debug("Reference Set directory: {} created", refSetDir);
		}
		if (!refSetDir.canWrite()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + refSetDir + "' isn't writeable");
		}
		return refSetDir;
	}

	public static File getReferenceSetLanguageDirectory() throws DrugMatchConfigurationException {
		File refSetLanguageDir = new File(getReferenceSetDirectory().getPath() + File.separator + "Language");
		if (!refSetLanguageDir.exists()) {
			if (!refSetLanguageDir.mkdirs()) {
				throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + refSetLanguageDir + "' isn't writeable");
			}
			log.debug("Reference Set Language directory: {} created", refSetLanguageDir);
		}
		if (!refSetLanguageDir.canWrite()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + refSetLanguageDir + "' isn't writeable");
		}
		return refSetLanguageDir;
	}

	public static File getReleaseFormat2Directory() throws DrugMatchConfigurationException {
		File rf2Dir = new File(getOutputDirectory().getPath() + File.separator + "RF2");
		if (!rf2Dir.exists()) {
			if (!rf2Dir.mkdirs()) {
				throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + rf2Dir + "' isn't writeable");
			}
			log.debug("Release Format 2 directory: {} created", rf2Dir);
		}
		if (!rf2Dir.canWrite()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + rf2Dir + "' isn't writeable");
		}
		return rf2Dir;
	}

	public static File getReportDirectory() throws DrugMatchConfigurationException {
		File reportDir = new File(getOutputDirectory().getPath() + File.separator + "report");
		if (!reportDir.exists()) {
			if (!reportDir.mkdirs()) {
				throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + reportDir + "' isn't writeable");
			}
			log.debug("Report directory: {} created", reportDir);
		}
		if (!reportDir.canWrite()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + reportDir + "' isn't writeable");
		}
		return reportDir;
	}

	/**
	 * @return SNOMED CT ID service host URL, without trailing /.
	 * @throws DrugMatchConfigurationException
	 */
	public static String getSctIdService() throws DrugMatchConfigurationException {
		String s = getStringProperty(SCT_ID_SERVICE);
		if (s != null
				&& s.endsWith("/")) {
			return s.substring(0, (s.length() - 1));
		}
		return s;
	}

	/**
	 * @return SNOMED CT release ID
	 * @throws DrugMatchConfigurationException
	 */
	public static String getSctReleaseId() throws DrugMatchConfigurationException {
		return getStringProperty(SCT_RELEASE_ID);
	}

	public static File getTerminologyDirectory() throws DrugMatchConfigurationException {
		File terminologyDir = new File(getReleaseFormat2Directory().getPath() + File.separator + "Terminology");
		if (!terminologyDir.exists()) {
			if (!terminologyDir.mkdirs()) {
				throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + terminologyDir + "' isn't writeable");
			}
			log.debug("Terminology directory: {} created", terminologyDir);
		}
		if (!terminologyDir.canWrite()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + terminologyDir + "' isn't writeable");
		}
		return terminologyDir;
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
