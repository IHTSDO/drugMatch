package org.ihtsdo.sct.drugmatch.parser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.CharEncoding;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchStrictModeViolationException;
import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.ihtsdo.sct.drugmatch.parser.Parser;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author dev-team@carecom.dk
 */
public class CSVParser implements Parser {

	private static final Logger log = LoggerFactory.getLogger(CSVParser.class);

	/**
	 * @return input file.
	 * @throws DrugMatchConfigurationException
	 */
	private static File getInputFile() throws DrugMatchConfigurationException {
		String inputFilePath = DrugMatchProperties.getInputFilePath();
		if (inputFilePath == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.INPUT_FILE + "' isn't set!");
		}
		File input = new File(inputFilePath);
		if (!input.exists()
				|| input.isDirectory()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.INPUT_FILE + "' doesn't point to a file!");
		}
		if (!input.canRead()) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.INPUT_FILE + "' doesn't point to a readable file!");
		}
		log.info("Using '{}' = '{}'", DrugMatchProperties.INPUT_FILE, input);
		return input;
	}

	/**
	 * @return Input file content separator.
	 * @throws DrugMatchConfigurationException
	 */
	private static char getInputFileContentSeparator() throws DrugMatchConfigurationException {
		String separatorSetting = DrugMatchProperties.getFileContentSeparatorCharacter();
		if (separatorSetting == null) {
			separatorSetting = ";";
		}
		log.debug("Using '{}' = '{}'", DrugMatchProperties.FILE_CONTENT_SEPARATOR_CHARACTER, separatorSetting);
		return separatorSetting.charAt(0);
	}

	/**
	 * @param strings
	 * @param index
	 * @return trimmed value or <code>null</code> if missing or empty
	 */
	private static String extractValue(final String[] strings,
			final int index) {
		String value = strings[index];
		if (value != null) {
			value = value.trim();
			if (value.isEmpty()) {
				return null;
			}
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<Pharmaceutical> parse() throws DrugMatchConfigurationException, DrugMatchStrictModeViolationException, IOException {
		log.info("Starting input parsing");
		CSVReader reader = null;
		try (
				InputStreamReader inputReader = new InputStreamReader(new FileInputStream(getInputFile()),
				CharEncoding.UTF_8);
		) {
			// initialize reader
			String quoteCharacter = DrugMatchProperties.getFileContentQuoteCharacter();
			if (quoteCharacter == null) {
				reader = new CSVReader(inputReader,
						getInputFileContentSeparator());
				log.info("Assuming unquoted input content");
			} else {
				reader = new CSVReader(inputReader,
						getInputFileContentSeparator(),
						quoteCharacter.charAt(0));
				log.debug("Using '{}' = '{}'", DrugMatchProperties.FILE_QUOTE_CHARACTER, quoteCharacter);
			}
			// 1st line
			int componentGroup = 0,
				componentIndex,
				lineNumber = 0;
			if (!Boolean.parseBoolean(DrugMatchProperties.getInputFileIncludeFirstLine())) {
				reader.readNext();
				lineNumber++;
			}
			// content
			List<Pharmaceutical> pharmaceuticals = new ArrayList<>();
			String[] columns;
			String drugId,
				tradeName,
				doseFormEnglish,
				doseFormNational,
				substanceNameEnglish,
				substanceNameNational,
				strength,
				unit;
			Pharmaceutical pharmaceutical;
			List<Component> malformedComponents;
			while ((columns = reader.readNext()) != null) {
				lineNumber++;
				if (columns.length >= 7) { // ie. minimum 1 pharmaceutical with 1 component
					// pharmaceutical
					drugId = extractValue(columns, 0);
					if (drugId == null) {
						log.warn("SKIPPING LINE: {} CAUSE: empty drugId!", String.valueOf(lineNumber));
						continue;
					} // else
					tradeName = extractValue(columns, 1);
					if (tradeName == null) {
						log.warn("SKIPPING LINE: {}, drug ID: {} CAUSE: empty tradeName!",
								String.valueOf(lineNumber),
								drugId);
						continue;
					} // else
					doseFormEnglish = extractValue(columns, 2);
					if (doseFormEnglish == null) {
						log.warn("SKIPPING LINE: {}, drug ID: {} CAUSE: empty doseFormEnglish!",
								String.valueOf(lineNumber),
								drugId);
						continue;
					} // else
					doseFormNational = extractValue(columns, 3);
					if (doseFormNational == null) {
						log.warn("SKIPPING LINE: {}, drug ID: {} CAUSE: empty doseFormNational!",
								String.valueOf(lineNumber),
								drugId);
						continue;
					} // else
					pharmaceutical = new Pharmaceutical(new ArrayList<Component>(),
							doseFormEnglish,
							doseFormNational,
							drugId,
							tradeName);
					// components
					componentIndex = 4;
					malformedComponents = new ArrayList<>();
					while ((componentIndex + 4) <= columns.length) {
						componentGroup++;
						substanceNameEnglish = extractValue(columns, (componentIndex));
						substanceNameNational = extractValue(columns, (componentIndex + 1));
						strength = extractValue(columns, (componentIndex + 2));
						unit = extractValue(columns, (componentIndex + 3));
						if (substanceNameEnglish == null
								&& substanceNameNational == null
								&& strength == null
								&& unit == null) {
							log.debug("Skipping component group: {} in line: {}, drug ID: {} cause: all columns in component group are empty.",
									String.valueOf(componentGroup),
									String.valueOf(lineNumber),
									drugId);
						} else {
							if (substanceNameEnglish == null
									|| substanceNameNational == null
									|| strength == null
									|| unit == null) {
								// using raw values on purpose, to support debugging!
								malformedComponents.add(new Component(columns[componentIndex],
										columns[componentIndex + 1],
										columns[componentIndex + 2],
										columns[componentIndex + 3]));
							} else {
								pharmaceutical.components.add(new Component(substanceNameEnglish,
										substanceNameNational,
										strength,
										unit));
							}
						}
						componentIndex += 4;
					}
					if (malformedComponents.isEmpty()) {
						pharmaceuticals.add(pharmaceutical);
					} else {
						log.warn("SKIPPING LINE: {}, drug ID: {} CAUSE: {} component(s) is malformed! {}",
								String.valueOf(lineNumber),
								drugId,
								String.valueOf(malformedComponents.size()),
								malformedComponents);
					}
				} else {
					log.warn("SKIPPING LINE: {} CAUSE: insufficient data (less than 6 columns)!", String.valueOf(lineNumber));
				}
			}
			log.info("Completed input parsing");
			// strict mode check
			if (DrugMatchProperties.isStrictMode()) {
				int parseCount = (Boolean.parseBoolean(DrugMatchProperties.getInputFileIncludeFirstLine())) ? lineNumber : (lineNumber - 1);
				if (parseCount != pharmaceuticals.size()) {
					throw new DrugMatchStrictModeViolationException("FLOW ABORTED, CAUSE: STRICT MODE, VIOLATION(S) DETECTED DURING PARSING: " +
						parseCount +" PARSED " +
						pharmaceuticals.size() + " VALID!");
				}
			} // else
			return pharmaceuticals;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
}
