package org.ihtsdo.sct.drugmatch.check;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.ihtsdo.sct.drugmatch.check.extension.CheckValidationHelper;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.ihtsdo.sct.drugmatch.model.Substance;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.verification.service.VerificationService;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author dev-team@carecom.dk
 * 
 */
public class Check {

	private static final Logger log = LoggerFactory.getLogger(Check.class);

	private final CheckValidation checkValidation;

	private final DrugMatchProperties drugMatchProperties;

	/**
	 * {@link Substance} to names to matches (this construct enables direct link between English and national term)
	 */
	private final SortedMap<Substance, Map<String, List<ConceptSearchResultDescriptor>>> substances = new TreeMap<>();

	/**
	 * unit name to matches
	 */
	private final SortedMap<String, Map<CheckLocale, List<ConceptSearchResultDescriptor>>> doseForms = new TreeMap<>(),
			units = new TreeMap<>();

	/**
	 * used to generate the report
	 */
	private final List<Pharmaceutical> pharmaceuticals;

	private final String isoNow;

	private final VerificationService service;

	public Check(DrugMatchProperties drugMatchProperties,
			List<Pharmaceutical> pharmaceuticals,
			String isoNow,
			VerificationService service) {
		this.checkValidation = CheckValidationHelper.getCheckValidation(drugMatchProperties.getNationalNamespaceId());
		this.drugMatchProperties = drugMatchProperties;
		// extract pharmaceutical components in preparation for execution
		Map<String, List<ConceptSearchResultDescriptor>> substanceNameToMatches;
		for (Pharmaceutical pharmaceutical : pharmaceuticals) {
			this.doseForms.put(pharmaceutical.doseForm, new HashMap<CheckLocale, List<ConceptSearchResultDescriptor>>());
			for (Component component : pharmaceutical.components) {
				substanceNameToMatches = new HashMap<>();
				substanceNameToMatches.put(component.substance.nameEnglish, null);
				substanceNameToMatches.put(component.substance.nameNational, null);
				this.substances.put(component.substance, substanceNameToMatches);
				this.units.put(component.unit, new HashMap<CheckLocale, List<ConceptSearchResultDescriptor>>());
			}
		}
		this.pharmaceuticals = pharmaceuticals;
		this.isoNow = isoNow;
		this.service = service;
	}

	public char getOutputFileContentSeparator() {
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
	private void checkDoseForms() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Dose form \"Check\" ({} Dose forms)", this.doseForms.size());
		List<ConceptSearchResultDescriptor> matches;
		for (Map.Entry<String, Map<CheckLocale, List<ConceptSearchResultDescriptor>>> entry : this.doseForms.entrySet()) {
			// "check" national
			matches = this.service.getDoseFormExactNationalPreferredTermMatch(entry.getKey());
			entry.getValue().put(CheckLocale.NATIONAL, matches);
			if (matches.isEmpty()) {
				// "check" English, when unmatched national
				entry.getValue().put(CheckLocale.ENGLISH,
						this.service.getDoseFormExactEnglishPreferredTermMatch(entry.getKey()));
			}
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
	private void checkSubstances() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Substance \"Check\" ({} Substances)", this.substances.size());
		List<ConceptSearchResultDescriptor> matches;
		for (Map.Entry<Substance, Map<String, List<ConceptSearchResultDescriptor>>> entry : this.substances.entrySet()) {
			// "check" national
			matches = this.service.getSubstanceExactNationalPreferredTermMatch(entry.getKey().nameNational);
			entry.getValue().put(entry.getKey().nameNational, matches);
			if (matches.isEmpty()) {
				// "check" English, when unmatched national
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
	private void checkUnits() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Unit \"Check\" ({} Units)", this.units.size());
		List<ConceptSearchResultDescriptor> matches;
		for (Map.Entry<String, Map<CheckLocale, List<ConceptSearchResultDescriptor>>> entry : this.units.entrySet()) {
			// "check" national
			matches = this.service.getUnitExactNationalPreferredTermMatch(entry.getKey());
			entry.getValue().put(CheckLocale.NATIONAL, matches);
			if (matches.isEmpty()) {
				// "check" English, when unmatched national
				entry.getValue().put(CheckLocale.ENGLISH,
						this.service.getUnitExactEnglishPreferredTermMatch(entry.getKey()));
			}
		}
		log.info("Completed Unit \"Check\"");
		log.info("Starting Unit \"Check\" report");
		report(CheckComponent.UNIT,
				"unit");
		log.info("Completed Unit \"Check\" report");
	}

	/**
	 * Extract {@link CheckRule#CASE_INSENSITIVE_MATCH} & {@link CheckRule#EXACT_MATCH} matches from prospects
	 * 
	 * @param prospects
	 */
	private Map<String, Long> getMatches(Map<String, Map<CheckLocale, List<ConceptSearchResultDescriptor>>> prospects) {
		Map<String, Long> result = new HashMap<>(prospects.size());
		List<ConceptSearchResultDescriptor> englishDescriptors,
		matchDescriptors,
		nationalDescriptors;
		CheckRule checkRule;
		for (Map.Entry<String, Map<CheckLocale, List<ConceptSearchResultDescriptor>>> prospectEntry : prospects.entrySet()) {
			englishDescriptors = prospectEntry.getValue().get(CheckLocale.ENGLISH);
			nationalDescriptors = prospectEntry.getValue().get(CheckLocale.NATIONAL);
			matchDescriptors = getMatchDescriptors(englishDescriptors, nationalDescriptors);
			checkRule = getRule(prospectEntry.getKey(),
					nationalDescriptors,
					matchDescriptors); 
			if (matchDescriptors.size() == 1
					&& (CheckRule.CASE_INSENSITIVE_MATCH.equals(checkRule)
							|| CheckRule.EXACT_MATCH.equals(checkRule)
							|| CheckRule.CONCATENATION_MATCH.equals(checkRule)
							|| CheckRule.INFLECTION_MATCH.equals(checkRule)
							|| CheckRule.TRANSLATION_MISSING.equals(checkRule))) {
				result.put(prospectEntry.getKey(),
						Long.valueOf(matchDescriptors.iterator().next().conceptCode));
			}
		}
		return result;
	}

	public void execute() throws IOException, DrugMatchConfigurationException {
		log.info("Starting \"Check\"");
		checkDoseForms();
		checkSubstances();
		checkUnits();
		log.info("Completed \"Check\"");
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public Map<String, Long> getDoseForm2Id() {
		return getMatches(this.doseForms);
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public Map<String, Long> getSubstance2Id() {
		Map<String, Map<CheckLocale, List<ConceptSearchResultDescriptor>>> prospects = new HashMap<>(this.substances.size());
		List<ConceptSearchResultDescriptor> matches;
		String substanceName;
		for (Map.Entry<Substance, Map<String, List<ConceptSearchResultDescriptor>>> entry : this.substances.entrySet()) {
			substanceName = entry.getKey().nameNational;
			matches = entry.getValue().get(substanceName);
			if (matches.isEmpty()) {
				substanceName = entry.getKey().nameEnglish;
				prospects.put(substanceName,
						Collections.singletonMap(CheckLocale.ENGLISH, entry.getValue().get(substanceName)));
			} else {
				prospects.put(substanceName,
						Collections.singletonMap(CheckLocale.NATIONAL, matches));
			}
		}
		return getMatches(prospects);
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public Map<String, Long> getUnit2Id() {
		return getMatches(this.units);
	}

	private void report(CheckComponent checkComponent,
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
			List<ConceptSearchResultDescriptor> englishDescriptors,
				nationalDescriptors;
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
							englishDescriptors = (substance2Descriptors == null) ? null : substance2Descriptors.get(componentName);
							nationalDescriptors = null;
							break;
						case SUBSTANCE_NATIONAL:
							componentName = component.substance.nameNational;
							substance2Descriptors = this.substances.get(component.substance);
							englishDescriptors = null;
							nationalDescriptors = (substance2Descriptors == null) ? null : substance2Descriptors.get(componentName);
							break;
						case UNIT:
							componentName = component.unit;
							englishDescriptors = this.units.get(componentName).get(CheckLocale.ENGLISH);
							nationalDescriptors = this.units.get(componentName).get(CheckLocale.NATIONAL);
							break;
						default:
							componentName = null;
							englishDescriptors = null;
							nationalDescriptors = null;
							log.debug("Unable to generate check report for component {} with Drug ID: {}", component, pharmaceutical.drugId);
							break;
						}
						pharmaceuticalLines.add(getColumns(pharmaceutical.drugId,
								componentName,
								nationalDescriptors,
								getMatchDescriptors(englishDescriptors, nationalDescriptors)));
					}
				} else {
					switch (checkComponent) {
					case DOSE_FORM:
						componentName = pharmaceutical.doseForm;
						englishDescriptors = this.doseForms.get(componentName).get(CheckLocale.ENGLISH);
						nationalDescriptors = this.doseForms.get(componentName).get(CheckLocale.NATIONAL);
						break;
					default:
						componentName = null;
						englishDescriptors = null;
						nationalDescriptors = null;
						log.debug("Unable to generate check report for {}", pharmaceutical);
						break;
					}
					pharmaceuticalLines = Collections.singletonList(getColumns(pharmaceutical.drugId,
							componentName,
							nationalDescriptors,
							getMatchDescriptors(englishDescriptors, nationalDescriptors)));
				}
				writer.writeAll(pharmaceuticalLines);
			}
			writer.flush();
			log.info("Created {}", fullFileName);
		}
	}

	/**
	 * @param englishDescriptors
	 * @param nationalDescriptors
	 * @return nationalDescriptors if not empty, otherwise englishDescriptors
	 */
	public static List<ConceptSearchResultDescriptor> getMatchDescriptors(List<ConceptSearchResultDescriptor> englishDescriptors,
			List<ConceptSearchResultDescriptor> nationalDescriptors) {
		if (nationalDescriptors != null
				&& nationalDescriptors.size() > 0) {
			return nationalDescriptors;
		}
		return englishDescriptors;
	}

	public String[] getColumns(String drugId,
			String componentName,
			List<ConceptSearchResultDescriptor> nationalDescriptors,
			List<ConceptSearchResultDescriptor> matchDescriptors) {
		String[] columns = new String[6];
		columns[0] = drugId;
		columns[1] = componentName;
		if (matchDescriptors != null
				&& matchDescriptors.size() == 1) {
				ConceptSearchResultDescriptor descriptor = matchDescriptors.iterator().next();
				columns[2] = descriptor.conceptCode;
				columns[3] = descriptor.healthtermDescriptionId.toString();
				columns[4] = descriptor.descriptionTerm;
		}
		columns[5] = this.checkValidation.getCheckRuleViolationMessage(getRule(componentName,
				nationalDescriptors,
				matchDescriptors));
		return columns;
	}

	/**
	 * @param descriptors
	 * @return {@link CheckRule} matched
	 */
	public CheckRule getRule(String componentName,
			List<ConceptSearchResultDescriptor> nationalDescriptors,
			List<ConceptSearchResultDescriptor> matchDescriptors) {
		// generic checks
		if (nationalDescriptors != null
				&& nationalDescriptors.isEmpty()) {
			return CheckRule.TRANSLATION_MISSING;
		} // else
		if (matchDescriptors == null) {
			return CheckRule.UNCHECKED;
		} // else
		if (matchDescriptors.isEmpty()) {
			return CheckRule.ZERO_MATCH;
		} // else
		if (matchDescriptors.size() == 1) {
			// custom or generic check
			return this.checkValidation.getRule(componentName,
					matchDescriptors.iterator().next());
		} // else
		return CheckRule.AMBIGUOUS_MATCH;
	}

	private enum CheckComponent {
		DOSE_FORM,
		SUBSTANCE_ENGLISH,
		SUBSTANCE_NATIONAL,
		UNIT;
	}

	private enum CheckLocale {
		ENGLISH,
		NATIONAL;
	}
}
