/**
 * 
 */
package org.ihtsdo.sct.drugmatch.parser.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.ParsingResult;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.ihtsdo.sct.drugmatch.parser.Parser;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author dev-team@carecom.dk
 *
 */
public class CSVParser implements Parser {

	private static Logger log = LoggerFactory.getLogger(CSVParser.class);

	private final DrugMatchProperties drugMatchProperties = new DrugMatchProperties();

	private File getInputFile() throws DrugMatchConfigurationException {
		String inputFilePath = this.drugMatchProperties.getInputFilePath();
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

	private char getInputFileContentSeparator() {
		String separatorSetting = this.drugMatchProperties.getFileContentSeparatorCharacter();
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
	private static String extractValue(String[] strings,
			int index) {
		String value = strings[index];
		if (value != null) {
			value = value.trim();
			if (value.isEmpty()) {
				return null;
			}
		}
		return value;
	}

	public ParsingResult parse() throws DrugMatchConfigurationException, IOException {
		log.info("Starting input parsing");
		CSVReader reader = null;
		try {
			// initialize reader
			String quoteCharacter = this.drugMatchProperties.getFileContentQuoteCharacter();
			if (quoteCharacter == null) {
				reader = new CSVReader(new FileReader(getInputFile()),
						getInputFileContentSeparator());
				log.info("Assuming unquoted input content");
			} else {
				reader = new CSVReader(new FileReader(getInputFile()),
						getInputFileContentSeparator(),
						quoteCharacter.charAt(0));
				log.debug("Using '{}' = '{}'", DrugMatchProperties.INPUT_FILE_QUOTE_CHARACTER, quoteCharacter);
			}
			// 1st line
			String includeFirstLine = this.drugMatchProperties.getInputFileIncludeFirstLine();
			int componentGroup = 0,
				componentIndex,
				lineNumber = 0;
			if (includeFirstLine == null
					|| !Boolean.parseBoolean(includeFirstLine)) {
				reader.readNext();
				lineNumber++;
			}
			// content
			List<Pharmaceutical> pharmaceuticals = new ArrayList<>();
			String[] columns;
			String drugId,
				tradeName,
				doseForm,
				
				substanceName_en,
				substanceName_national,
				strength,
				unit;
			Pharmaceutical pharmaceutical;
			List<Component> malformedComponents;
			while ((columns = reader.readNext()) != null) {
				lineNumber++;
				if (columns.length >= 6) { // ie. minimum 1 pharmaceutical with 1 component
					// pharmaceutical
					drugId = extractValue(columns, 0);
					if (drugId == null) {
						log.warn("SKIPPING LINE: {} CAUSE: empty drugId!", lineNumber);
						continue;
					} // else
					tradeName = extractValue(columns, 1);
					if (tradeName == null) {
						log.warn("SKIPPING LINE: {} CAUSE: empty tradeName!", lineNumber);
						continue;
					} // else
					doseForm = extractValue(columns, 2);
					if (doseForm == null) {
						log.warn("SKIPPING LINE: {} CAUSE: empty doseForm!", lineNumber);
						continue;
					} // else
					pharmaceutical = new Pharmaceutical(new ArrayList<Component>(),
							doseForm,
							drugId,
							tradeName);
					// components
					componentIndex = 3;
					malformedComponents = new ArrayList<>();
					while ((componentIndex + 4) <= columns.length) {
						componentGroup++;
						substanceName_en = extractValue(columns, (componentIndex));
						substanceName_national = extractValue(columns, (componentIndex + 1));
						strength = extractValue(columns, (componentIndex + 2));
						unit = extractValue(columns, (componentIndex + 3));
						if (substanceName_en == null
								&& substanceName_national == null
								&& strength == null
								&& unit == null) {
							log.debug("Skipping component group {}, cause: all columns in group are empty.", componentGroup);
						} else {
							if (substanceName_en == null
									|| substanceName_national == null
									|| strength == null
									|| unit == null) {
								// using raw values on purpose, to support debugging!
								malformedComponents.add(new Component(columns[componentIndex],
										columns[componentIndex + 1],
										columns[componentIndex + 2],
										columns[componentIndex + 3]));
							} else {
								pharmaceutical.components.add(new Component(substanceName_en,
										substanceName_national,
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
								lineNumber,
								drugId,
								malformedComponents.size(),
								malformedComponents);
					}
				} else {
					log.warn("SKIPPING LINE: {} CAUSE: insufficient data (less than 6 columns)!", lineNumber);
				}
			}
			log.info("Completed input parsing");
			return new ParsingResult(lineNumber, pharmaceuticals);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
}
