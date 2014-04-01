package org.ihtsdo.sct.drugmatch.check;

import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.ihtsdo.sct.drugmatch.model.Substance;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.verification.service.VerificationService;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.impl.VerificationServiceImpl;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author dev-team@carecom.dk
 * 
 */
public class Check {

	private static Logger log = LoggerFactory.getLogger(Check.class);

	private final DrugMatchProperties drugMatchProperties = new DrugMatchProperties();

	/**
	 * {@link Substance} to names to matches (this construct enables direct link between English and national term)
	 */
	private final SortedMap<Substance, Map<String, List<ConceptSearchResultDescriptor>>> substances = new TreeMap<>();

	/**
	 * unit name to matches
	 */
	private final SortedMap<String, List<ConceptSearchResultDescriptor>> doseForms = new TreeMap<>(),
			units = new TreeMap<>();

	private final List<Pharmaceutical> pharmaceuticals;

	private final String isoNow;

	private final VerificationService service;

	public Check(List<Pharmaceutical> pharmaceuticals,
			String isoNow) throws KeyManagementException, NoSuchAlgorithmException, DrugMatchConfigurationException, IOException {
		// extract pharmaceutical components in preparation for execution
		Map<String, List<ConceptSearchResultDescriptor>> substanceNameToMatches;
		for (Pharmaceutical pharmaceutical : pharmaceuticals) {
			this.doseForms.put(pharmaceutical.doseForm, null);
			for (Component component : pharmaceutical.components) {
				substanceNameToMatches = new HashMap<>();
				substanceNameToMatches.put(component.substance.nameEnglish, null);
				substanceNameToMatches.put(component.substance.nameNational, null);
				this.substances.put(component.substance, substanceNameToMatches);
				this.units.put(component.unit, null);
			}
		}
		this.pharmaceuticals = pharmaceuticals;
		this.isoNow = isoNow;
		this.service = new VerificationServiceImpl();
	}

	private char getOutputFileContentSeparator() {
		String separatorSetting = this.drugMatchProperties.getFileContentSeparatorCharacter();
		if (separatorSetting == null) {
			separatorSetting = ";";
		}
		log.debug("Using '{}' = '{}'", DrugMatchProperties.FILE_CONTENT_SEPARATOR_CHARACTER, separatorSetting);
		return separatorSetting.charAt(0);
	}

	/**
	 * "Check" dose forms
	 * @throws DrugMatchConfigurationException 
	 * @throws IOException 
	 */
	public void checkDoseForms() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Dose form \"Check\" ({} Dose forms)", this.doseForms.size());
		for (String doseForm : this.doseForms.keySet()) {
			this.doseForms.put(doseForm, this.service.getDoseFormExactEnglishPreferredTermMatch(doseForm));
		}
		log.info("Completed Dose form \"Check\"");
		log.info("Starting Dose form \"Check\" report");
		report(CheckComponent.DOSE_FORM,
				"dose_form");
		log.info("Completed Dose form \"Check\" report");
	}

	/**
	 * "Check" substances
	 * @throws DrugMatchConfigurationException 
	 * @throws IOException 
	 */
	public void checkSubstances() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Substance \"Check\" ({} Substances)", this.substances.size());
		List<ConceptSearchResultDescriptor> matches;
		for (Map.Entry<Substance, Map<String, List<ConceptSearchResultDescriptor>>> entry : this.substances.entrySet()) {
			// "check" substances national
			matches = this.service.getSubstanceExactNationalPreferredTermMatch(entry.getKey().nameNational);
			entry.getValue().put(entry.getKey().nameNational, matches);
			if (matches.isEmpty()) {
				// "check" substances English, that didn't match the national
				entry.getValue().put(entry.getKey().nameEnglish,
						this.service.getSubstanceExactEnglishPreferredTermMatch(entry.getKey().nameEnglish));
			}
		}
		log.info("Completed Substance \"Check\"");
		log.info("Starting Substance \"Check\" english report");
		// substance english report
		report(CheckComponent.SUBSTANCE_ENGLISH,
				"substance_english");
		log.info("Completed Substance \"Check\" english report");
		log.info("Starting Substance \"Check\" national report");
		// substance national report
		report(CheckComponent.SUBSTANCE_NATIONAL,
				"substance_national");
		log.info("Completed Substance \"Check\" national report");
	}

	/**
	 * "Check" units
	 * @throws DrugMatchConfigurationException 
	 * @throws IOException 
	 */
	public void checkUnits() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Unit \"Check\" ({} Units)", this.units.size());
		for (String unit : this.units.keySet()) {
			this.units.put(unit, this.service.getUnitExactEnglishPreferredTermMatch(unit));
		}
		log.info("Completed Unit \"Check\"");
		log.info("Starting Unit \"Check\" report");
		report(CheckComponent.UNIT,
				"unit");
		log.info("Completed Unit \"Check\" report");
	}

	public void execute() throws IOException, DrugMatchConfigurationException {
		log.info("Starting \"Check\"");
		checkDoseForms();
		checkSubstances();
		checkUnits();
		log.info("Completed \"Check\"");
	}

	public void report(CheckComponent checkComponent,
			String fileName) throws IOException, DrugMatchConfigurationException {
		String fullFileName = this.drugMatchProperties.getOutputDirectory().getPath() + "/check_" + fileName + "_" + this.isoNow + ".csv";
		String quoteCharacter = this.drugMatchProperties.getFileContentQuoteCharacter();
		char quoteChar = (quoteCharacter == null) ? CSVWriter.NO_QUOTE_CHARACTER : quoteCharacter.charAt(0);
		try (CSVWriter writer = new CSVWriter(new FileWriter(fullFileName),
				getOutputFileContentSeparator(),
				quoteChar)) {
			// header
			String[] columns = new String[] {
					"Drug ID",
					"Component name",
					"SCT Concept ID",
					"SCT Description ID",
					"SCT Term",
					"Rule match"
			};
			writer.writeNext(columns);
			// content
			String componentName;
			List<ConceptSearchResultDescriptor> descriptors;
			boolean isComponent;
			List<String[]> pharmaceuticalLines;
			Map<String, List<ConceptSearchResultDescriptor>> substance2Descriptors;
			for (Pharmaceutical pharmaceutical : this.pharmaceuticals) {
				switch (checkComponent) {
				case DOSE_FORM:
					isComponent = false;
					break;
				default:
					isComponent = true;
					break;
				}
				if (isComponent) {
					pharmaceuticalLines = new ArrayList<>(pharmaceutical.components.size());
					for (Component component : pharmaceutical.components) {
						switch (checkComponent) {
						case SUBSTANCE_ENGLISH:
							componentName = component.substance.nameEnglish;
							substance2Descriptors = this.substances.get(component.substance);
							descriptors = (substance2Descriptors == null) ? null : substance2Descriptors.get(componentName);
							break;
						case SUBSTANCE_NATIONAL:
							componentName = component.substance.nameNational;
							substance2Descriptors = this.substances.get(component.substance);
							descriptors = (substance2Descriptors == null) ? null : substance2Descriptors.get(componentName);
							break;
						case UNIT:
							componentName = component.unit;
							descriptors = this.units.get(componentName);
							break;
						default:
							componentName = null;
							descriptors = null;
							log.debug("Unable to generate check report for component {} with Drug ID: {}", component, pharmaceutical.drugId);
							break;
						}
						pharmaceuticalLines.add(getColumns(pharmaceutical.drugId,
								componentName,
								descriptors));
					}
				} else {
					switch (checkComponent) {
					case DOSE_FORM:
						componentName = pharmaceutical.doseForm;
						descriptors = this.doseForms.get(componentName);
						break;
					default:
						componentName = null;
						descriptors = null;
						log.debug("Unable to generate check report for {}", pharmaceutical);
						break;
					}
					pharmaceuticalLines = Collections.singletonList(getColumns(pharmaceutical.drugId,
							componentName,
							descriptors));
				}
				writer.writeAll(pharmaceuticalLines);
			}
			writer.flush();
			log.info("Created {}", fullFileName);
		}
	}

	private static String[] getColumns(String drugId,
			String componentName,
			List<ConceptSearchResultDescriptor> descriptors) {
		String[] columns = new String[6];
		columns[0] = drugId;
		columns[1] = componentName;
		if (descriptors == null) {
			columns[5] = "UNCHECKED";
		} else if (descriptors.isEmpty()) {
			columns[5] = "ZERO_MATCH";
		} else {
			if (descriptors.size() == 1) {
				ConceptSearchResultDescriptor descriptor = descriptors.iterator().next();
				columns[2] = descriptor.conceptCode;
				columns[3] = descriptor.healthtermDescriptionId.toString();
				columns[4] = descriptor.descriptionTerm;
				if (componentName.equals(descriptor.descriptionTerm)) {
					columns[5] = "EXACT_MATCH";
				} else {
					columns[5] = "CASE_INSENSITIVE_MATCH";
				}
			} else {
				columns[4] = "AMBIGUOUS_MATCH";
			}
		}
		return columns;
	}

	private enum CheckComponent {
		DOSE_FORM,
		SUBSTANCE_ENGLISH,
		SUBSTANCE_NATIONAL,
		UNIT;
	}
}
