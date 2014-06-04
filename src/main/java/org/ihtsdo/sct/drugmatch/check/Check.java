package org.ihtsdo.sct.drugmatch.check;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.codec.CharEncoding;
import org.ihtsdo.sct.drugmatch.check.extension.CheckValidationHelper;
import org.ihtsdo.sct.drugmatch.comparator.StringArrayComparator;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchStrictModeViolationException;
import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.DoseForm;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.ihtsdo.sct.drugmatch.model.Substance;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.verification.service.VerificationService;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * DrugMatch "Check".
 * @author dev-team@carecom.dk
 */
public class Check {

	private static final Logger log = LoggerFactory.getLogger(Check.class);

	private static final StringArrayComparator STRING_ARRAY_COMPARATOR = new StringArrayComparator();

	private final CheckValidation checkValidation;

	/**
	 * {@link DoseForm} to names to matches (this construct enables direct link between English and national term).
	 */
	private final SortedMap<DoseForm, Map<Locale, List<ConceptSearchResultDescriptor>>> doseForms = new TreeMap<>();

	/**
	 * {@link Substance} to names to matches (this construct enables direct link between English and national term).
	 */
	private final SortedMap<Substance, Map<Locale, List<ConceptSearchResultDescriptor>>> substances = new TreeMap<>();

	/**
	 * Unit name to matches.
	 */
	private final SortedMap<String, Map<Locale, List<ConceptSearchResultDescriptor>>> units = new TreeMap<>();

	/**
	 * used to generate the report.
	 */
	private final List<Pharmaceutical> pharmaceuticals;

	private final String isoNow;

	private final VerificationService service;

	/**
	 * @param pharmaceuticals
	 * @param isoNow
	 * @param service
	 * @throws DrugMatchConfigurationException
	 */
	public Check(final List<Pharmaceutical> pharmaceuticals,
			final String isoNow,
			final VerificationService service) throws DrugMatchConfigurationException {
		this.checkValidation = (DrugMatchProperties.createGenericReport()) ? new CheckValidationImpl() : CheckValidationHelper.getCheckValidation(DrugMatchProperties.getNationalNamespaceId());
		// extract unique pharmaceutical attributes in preparation for execution
		for (Pharmaceutical pharmaceutical : pharmaceuticals) {
			this.doseForms.put(pharmaceutical.doseForm, new HashMap<Locale, List<ConceptSearchResultDescriptor>>());
			for (Component component : pharmaceutical.components) {
				this.substances.put(component.substance, new HashMap<Locale, List<ConceptSearchResultDescriptor>>());
				this.units.put(component.unit, new HashMap<Locale, List<ConceptSearchResultDescriptor>>());
			}
		}
		this.pharmaceuticals = pharmaceuticals;
		this.isoNow = isoNow;
		this.service = service;
	}

	/**
	 * @return output file content separator
	 * @throws DrugMatchConfigurationException
	 */
	public static char getOutputFileContentSeparator() throws DrugMatchConfigurationException {
		String separatorSetting = DrugMatchProperties.getFileContentSeparatorCharacter();
		if (separatorSetting == null) {
			separatorSetting = ";";
		}
		log.debug("Using '{}' = '{}'", DrugMatchProperties.FILE_CONTENT_SEPARATOR_CHARACTER, separatorSetting);
		return separatorSetting.charAt(0);
	}

	/**
	 * "Check" dose forms.
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void checkDoseForms() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Dose form \"Check\" ({} Dose forms)", String.valueOf(this.doseForms.size()));
		List<ConceptSearchResultDescriptor> matches;
		for (Map.Entry<DoseForm, Map<Locale, List<ConceptSearchResultDescriptor>>> entry : this.doseForms.entrySet()) {
			// "check" national
			matches = this.service.getDoseFormExactNationalPreferredTermMatch(entry.getKey().nameNational);
			entry.getValue().put(Locale.NATIONAL, matches);
			if (matches.isEmpty()) {
				// "check" English, when unmatched national
				entry.getValue().put(Locale.ENGLISH,
						this.service.getDoseFormExactEnglishPreferredTermMatch(entry.getKey().nameEnglish));
			}
		}
		log.info("Completed Dose form \"Check\"");
		log.info("Starting Dose form \"Check\" English report");
		// dose form English report
		report(Report.DOSE_FORM_ENGLISH,
				"dose_form_english");
		log.info("Completed Dose form \"Check\" English report");
		log.info("Starting Dose form \"Check\" national report");
		// dose form national report
		report(Report.DOSE_FORM_NATIONAL,
				"dose_form_national");
		log.info("Completed Dose form \"Check\" national report");
	}

	/**
	 * "Check" substances.
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void checkSubstances() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Substance \"Check\" ({} Substances)", String.valueOf(this.substances.size()));
		List<ConceptSearchResultDescriptor> matches;
		for (Map.Entry<Substance, Map<Locale, List<ConceptSearchResultDescriptor>>> entry : this.substances.entrySet()) {
			// "check" national
			matches = this.service.getSubstanceExactNationalPreferredTermMatch(entry.getKey().nameNational);
			entry.getValue().put(Locale.NATIONAL, matches);
			if (matches.isEmpty()) {
				// "check" English, when unmatched national
				entry.getValue().put(Locale.ENGLISH,
						this.service.getSubstanceExactEnglishPreferredTermMatch(entry.getKey().nameEnglish));
			}
		}
		log.info("Completed Substance \"Check\"");
		log.info("Starting Substance \"Check\" English report");
		// substance English report
		report(Report.SUBSTANCE_ENGLISH,
				"substance_english");
		log.info("Completed Substance \"Check\" English report");
		log.info("Starting Substance \"Check\" national report");
		// substance national report
		report(Report.SUBSTANCE_NATIONAL,
				"substance_national");
		log.info("Completed Substance \"Check\" national report");
	}

	/**
	 * "Check" units.
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void checkUnits() throws IOException, DrugMatchConfigurationException {
		log.info("Starting Unit \"Check\" ({} Units)", String.valueOf(this.units.size()));
		List<ConceptSearchResultDescriptor> matches;
		for (Map.Entry<String, Map<Locale, List<ConceptSearchResultDescriptor>>> entry : this.units.entrySet()) {
			// "check" national
			matches = this.service.getUnitExactNationalPreferredTermMatch(entry.getKey());
			entry.getValue().put(Locale.NATIONAL, matches);
			if (matches.isEmpty()) {
				// "check" English, when unmatched national
				entry.getValue().put(Locale.ENGLISH,
						this.service.getUnitExactEnglishPreferredTermMatch(entry.getKey()));
			}
		}
		log.info("Completed Unit \"Check\"");
		log.info("Starting Unit \"Check\" report");
		report(Report.UNIT,
				"unit");
		log.info("Completed Unit \"Check\" report");
	}

	/**
	 * Extract {@link CheckRule#CASE_INSENSITIVE_MATCH} & {@link CheckRule#EXACT_MATCH} matches from prospects.
	 * @param prospects
	 * @return unmodifiable {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	private static Map<String, Long> getMatches(final Map<String, Map<Locale, List<ConceptSearchResultDescriptor>>> prospects) {
		Map<String, Long> result = new HashMap<>(prospects.size());
		List<ConceptSearchResultDescriptor> englishDescriptors,
		matchDescriptors,
		nationalDescriptors;
		CheckRule checkRule;
		for (Map.Entry<String, Map<Locale, List<ConceptSearchResultDescriptor>>> prospectEntry : prospects.entrySet()) {
			englishDescriptors = prospectEntry.getValue().get(Locale.ENGLISH);
			nationalDescriptors = prospectEntry.getValue().get(Locale.NATIONAL);
			matchDescriptors = getMatchDescriptors(englishDescriptors, nationalDescriptors);
			checkRule = getRule(prospectEntry.getKey(),
					nationalDescriptors,
					matchDescriptors);
			if (matchDescriptors.size() == 1
					&& (CheckRule.CASE_INSENSITIVE_MATCH.equals(checkRule)
							|| CheckRule.EXACT_MATCH.equals(checkRule)
							|| CheckRule.TRANSLATION_MISSING.equals(checkRule))) {
				result.put(prospectEntry.getKey(),
						Long.valueOf(matchDescriptors.iterator().next().conceptCode));
			}
		}
		return Collections.unmodifiableMap(result);
	}

	/**
	 * Execute DrugMatch "Check".
	 * @throws DrugMatchConfigurationException
	 * @throws DrugMatchStrictModeViolationException
	 * @throws IOException
	 */
	public final void execute() throws DrugMatchConfigurationException, DrugMatchStrictModeViolationException, IOException {
		log.info("Starting \"Check\"");
		checkDoseForms();
		checkSubstances();
		checkUnits();
		log.info("Completed \"Check\"");
		if (DrugMatchProperties.isStrictMode()) {
			int missingDoseForms = this.doseForms.size() - getDoseForm2Id().size();
			int missingSubstances = this.substances.size() - getSubstance2Id().size();
			int missingUnits = this.units.size() - getUnit2Id().size();
			if (missingDoseForms > 0
					|| missingSubstances > 0
					|| missingUnits > 0) {
				StringBuilder msg = new StringBuilder("FLOW ABORTED, CAUSE: STRICT MODE, VIOLATION(S) DETECTED DURING \"Check\":");
				if (missingDoseForms > 0) {
					msg.append(" MISSING ");
					msg.append(missingDoseForms);
					msg.append(" DOSE FORM(S),");
				}
				if (missingSubstances > 0) {
					msg.append(" MISSING ");
					msg.append(missingSubstances);
					msg.append(" SUBSTANCE(S),");
				}
				if (missingUnits > 0) {
					msg.append(" MISSING ");
					msg.append(missingUnits);
					msg.append(" UNIT(S)!");
				}
				if (msg.charAt(msg.length() - 1) == ',') {
					msg.setCharAt(msg.length() - 1, '!');
				}
				throw new DrugMatchStrictModeViolationException(msg.toString());
			}
		}
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public final Map<String, Long> getDoseForm2Id() {
		Map<String, Map<Locale, List<ConceptSearchResultDescriptor>>> prospects = new HashMap<>(this.doseForms.size());
		List<ConceptSearchResultDescriptor> matches;
		String doseFormName;
		for (Map.Entry<DoseForm, Map<Locale, List<ConceptSearchResultDescriptor>>> entry : this.doseForms.entrySet()) {
			doseFormName = entry.getKey().nameNational;
			matches = entry.getValue().get(Locale.NATIONAL);
			if (matches.isEmpty()) {
				doseFormName = entry.getKey().nameEnglish;
				prospects.put(doseFormName,
						Collections.singletonMap(Locale.ENGLISH, entry.getValue().get(Locale.ENGLISH)));
			} else {
				prospects.put(doseFormName,
						Collections.singletonMap(Locale.NATIONAL, matches));
			}
		}
		return getMatches(prospects);
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public final Map<String, Long> getSubstance2Id() {
		Map<String, Map<Locale, List<ConceptSearchResultDescriptor>>> prospects = new HashMap<>(this.substances.size());
		List<ConceptSearchResultDescriptor> matches;
		String substanceName;
		for (Map.Entry<Substance, Map<Locale, List<ConceptSearchResultDescriptor>>> entry : this.substances.entrySet()) {
			substanceName = entry.getKey().nameNational;
			matches = entry.getValue().get(Locale.NATIONAL);
			if (matches.isEmpty()) {
				substanceName = entry.getKey().nameEnglish;
				prospects.put(substanceName,
						Collections.singletonMap(Locale.ENGLISH, entry.getValue().get(Locale.ENGLISH)));
			} else {
				prospects.put(substanceName,
						Collections.singletonMap(Locale.NATIONAL, matches));
			}
		}
		return getMatches(prospects);
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public final Map<String, Long> getUnit2Id() {
		return getMatches(this.units);
	}

	/**
	 * Export report.
	 * @param report type
	 * @param fileName destination
	 * @throws IOException
	 * @throws DrugMatchConfigurationException
	 */
	private void report(final Report report,
			final String fileName) throws IOException, DrugMatchConfigurationException {
		String fullFileName = DrugMatchProperties.getReportDirectory().getPath() + File.separator + "check_" + fileName + "_" + this.isoNow + ".csv";
		String quoteCharacter = DrugMatchProperties.getFileContentQuoteCharacter();
		char quoteChar = (quoteCharacter == null) ? CSVWriter.NO_QUOTE_CHARACTER : quoteCharacter.charAt(0);
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fullFileName),
						CharEncoding.UTF_8),
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
			List<String[]> pharmaceuticalLines;
			Set<String[]> uniquePharmaceuticalLines;
			for (Pharmaceutical pharmaceutical : this.pharmaceuticals) {
				if (Report.DOSE_FORM_ENGLISH.equals(report)
						|| Report.DOSE_FORM_NATIONAL.equals(report)) {
					switch (report) {
					case DOSE_FORM_ENGLISH:
						componentName = pharmaceutical.doseForm.nameEnglish;
						englishDescriptors = this.doseForms.get(pharmaceutical.doseForm).get(Locale.ENGLISH);
						nationalDescriptors = null;
						break;
					case DOSE_FORM_NATIONAL:
						componentName = pharmaceutical.doseForm.nameNational;
						englishDescriptors = null;
						nationalDescriptors = this.doseForms.get(pharmaceutical.doseForm).get(Locale.NATIONAL);
						break;
					default:
						componentName = null;
						englishDescriptors = null;
						nationalDescriptors = null;
						log.debug("Unable to generate check report for dose form {} with Drug ID: {}", pharmaceutical.doseForm, pharmaceutical.drugId);
						break;
					}
					pharmaceuticalLines = Collections.singletonList(getColumns(pharmaceutical.drugId,
							componentName,
							nationalDescriptors,
							getMatchDescriptors(englishDescriptors, nationalDescriptors)));
				} else if (Report.UNIT.equals(report)) {
					// "compress" unit output, excluding duplicated rows
					uniquePharmaceuticalLines = new TreeSet<>(STRING_ARRAY_COMPARATOR);
					for (Component component : pharmaceutical.components) {
						componentName = component.unit;
						englishDescriptors = this.units.get(componentName).get(Locale.ENGLISH);
						nationalDescriptors = this.units.get(componentName).get(Locale.NATIONAL);
						uniquePharmaceuticalLines.add(getColumns(pharmaceutical.drugId,
							componentName,
							nationalDescriptors,
							getMatchDescriptors(englishDescriptors, nationalDescriptors)));
					}
					pharmaceuticalLines = new ArrayList<>(uniquePharmaceuticalLines);
				} else {
					pharmaceuticalLines = new ArrayList<>(pharmaceutical.components.size());
					for (Component component : pharmaceutical.components) {
						switch (report) {
						case SUBSTANCE_ENGLISH:
							componentName = component.substance.nameEnglish;
							englishDescriptors = this.substances.get(component.substance).get(Locale.ENGLISH);
							nationalDescriptors = null;
							break;
						case SUBSTANCE_NATIONAL:
							componentName = component.substance.nameNational;
							englishDescriptors = null;
							nationalDescriptors = this.substances.get(component.substance).get(Locale.NATIONAL);
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
	public static List<ConceptSearchResultDescriptor> getMatchDescriptors(final List<ConceptSearchResultDescriptor> englishDescriptors,
			final List<ConceptSearchResultDescriptor> nationalDescriptors) {
		if (nationalDescriptors != null
				&& (nationalDescriptors.size() > 0
						|| englishDescriptors == null)) {
			return nationalDescriptors;
		}
		return englishDescriptors;
	}

	/**
	 * @param drugId
	 * @param componentName
	 * @param nationalDescriptors
	 * @param matchDescriptors
	 * @return report columns
	 */
	public final String[] getColumns(final String drugId,
			final String componentName,
			final List<ConceptSearchResultDescriptor> nationalDescriptors,
			final List<ConceptSearchResultDescriptor> matchDescriptors) {
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
		columns[5] = this.checkValidation.getMessage(getRule(componentName,
				nationalDescriptors,
				matchDescriptors));
		return columns;
	}

	/**
	 * @param componentName
	 * @param nationalDescriptors
	 * @param matchDescriptors
	 * @return {@link CheckRule} matched
	 */
	public static CheckRule getRule(final String componentName,
			final List<ConceptSearchResultDescriptor> nationalDescriptors,
			final List<ConceptSearchResultDescriptor> matchDescriptors) {
		// generic checks
		if (matchDescriptors == null) {
			return CheckRule.UNCHECKED;
		} // else
		if (nationalDescriptors != null
				&& nationalDescriptors.isEmpty()
				&& matchDescriptors.size() > 0) {
			return CheckRule.TRANSLATION_MISSING;
		} // else
		if (matchDescriptors.isEmpty()) {
			return CheckRule.ZERO_MATCH;
		} // else
		if (matchDescriptors.size() == 1) {
			ConceptSearchResultDescriptor conceptSearchResultDescriptor = matchDescriptors.iterator().next();
			if (componentName.equals(conceptSearchResultDescriptor.descriptionTerm)) {
				return CheckRule.EXACT_MATCH;
			} // else
			if (componentName.equalsIgnoreCase(conceptSearchResultDescriptor.descriptionTerm)) {
				return CheckRule.CASE_INSENSITIVE_MATCH;
			} // else
			return CheckRule.COMPONENT_AND_TERM_MISMATCH;
		} // else
		return CheckRule.AMBIGUOUS_MATCH;
	}

	/**
	 * "Check" report types.
	 */
	private enum Report {
		DOSE_FORM_ENGLISH,
		DOSE_FORM_NATIONAL,
		SUBSTANCE_ENGLISH,
		SUBSTANCE_NATIONAL,
		UNIT;
	}

	/**
	 * "Check" locale.
	 */
	private enum Locale {
		ENGLISH,
		NATIONAL;
	}
}
