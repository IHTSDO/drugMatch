package org.ihtsdo.sct.drugmatch.match;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.tuple.Pair;
import org.ihtsdo.sct.drugmatch.check.Check;
import org.ihtsdo.sct.drugmatch.enumeration.DescriptionType;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.match.extension.MatchRuleUtil;
import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.verification.service.VerificationService;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptDescriptor;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.DescriptionDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author dev-team@carecom.dk
 */
public class Match {

	private static final Logger log = LoggerFactory.getLogger(Match.class);

	private final Check check;

	private final DescriptionDescriptorTypeComparator descriptionDescriptorTypeComparator = new DescriptionDescriptorTypeComparator();

	private final MatchRuleHelper matchRuleHelper;

	/**
	 * used to generate the report.
	 */
	private final List<Pharmaceutical> pharmaceuticals;

	private final String isoNow;

	private static final String[] REPORT_HEADER = new String[] {
				"Drug ID",
				"Drug name",
				"SCT Concept ID",
				"SCT Description ID",
				"SCT Term",
				"Rule match"
		};

	private final VerificationService service;

	public Match(final List<Pharmaceutical> pharmaceuticals,
			final String isoNow,
			final VerificationService service) throws DrugMatchConfigurationException {
		this.matchRuleHelper = (DrugMatchProperties.createGenericReport()) ? new MatchRuleHelperImpl() : MatchRuleUtil.getMatchRuleHelper(DrugMatchProperties.getNationalNamespaceId());
		this.pharmaceuticals = pharmaceuticals;
		this.isoNow = isoNow;
		this.service = service;
		this.check = new Check(this.pharmaceuticals,
				this.isoNow,
				this.service);
	}

	public final void execute(final boolean matchAttributeReport) throws IOException, DrugMatchConfigurationException {
		log.info("Starting \"Match\"");
		// implicit "Check" dependency
		this.check.execute();
		// attribute match
		Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> pharmaceutical2AttributeMatch = matchAttributes();
		if (matchAttributeReport) {
			reportAttributeMatches(pharmaceutical2AttributeMatch);
		}
		// term match
		reportTermMatches(pharmaceutical2AttributeMatch,
				matchTerms(pharmaceutical2AttributeMatch));
		log.info("Completed \"Match\"");
	}

	private Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> matchAttributes() throws DrugMatchConfigurationException, IOException {
		log.info("Starting attribute \"Match\" ({} pharmaceuticals)", this.pharmaceuticals.size());
		Long attributeIdHasActiveIngredient = DrugMatchProperties.getAttributeIdHasActiveIngredient();
		if (attributeIdHasActiveIngredient == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.ATTRIBUTE_ID_HAS_ACTIVE_INGREDIENT + "' isn't set!");
		} // else
		Long attributeIdHasDoseForm = DrugMatchProperties.getAttributeIdHasDoseForm();
		if (attributeIdHasDoseForm == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.ATTRIBUTE_ID_HAS_DOSE_FORM + "' isn't set!");
		} // else
		Set<Long> allValueIds,
			attributeIds = new HashSet<>(),
			valueIds,
			unitIds;
		attributeIds.add(attributeIdHasActiveIngredient);
		attributeIds.add(attributeIdHasDoseForm);
		// retrieve Component name to Concept ID
		Map<String, Long> doseForm2Id = this.check.getDoseForm2Id(),
			substance2Id = this.check.getSubstance2Id(),
			unit2Id = this.check.getUnit2Id();
		Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> result = new LinkedHashMap<>(this.pharmaceuticals.size());
		Long doseFormId,
			substanceId,
			unitId;
		MatchAttributeRule rule;
		List<ConceptSearchResultDescriptor> matches;
		for (Pharmaceutical pharmaceutical : this.pharmaceuticals) {
			valueIds = new HashSet<>();
			unitIds = new HashSet<>();
			rule = null;
			doseFormId = (pharmaceutical.doseForm.nameNational == null) ? null : doseForm2Id.get(pharmaceutical.doseForm.nameNational);
			if (doseFormId == null) {
				doseFormId = (pharmaceutical.doseForm.nameEnglish == null) ? null : doseForm2Id.get(pharmaceutical.doseForm.nameEnglish);
			}
			if (doseFormId == null) {
				rule = MatchAttributeRule.DOSE_FORM_MISSING_CHECK_CONCEPT;
				log.debug("Skipping pharmaceutical, cause: dose form wasn't available from \"Check\" [doseForm={}, drugId={}]", pharmaceutical.doseForm, pharmaceutical.drugId);
			} else {
				valueIds.add(doseFormId);
				components : for (Component component : pharmaceutical.components) {
					substanceId = (component.substance.nameNational == null) ? null : substance2Id.get(component.substance.nameNational);
					if (substanceId == null) {
						substanceId = (component.substance.nameEnglish == null) ? null : substance2Id.get(component.substance.nameEnglish);
						if (substanceId == null) {
							rule = MatchAttributeRule.SUBSTANCE_MISSING_CHECK_CONCEPT;
							log.debug("Skipping pharmaceutical, cause: substance wasn't available from \"Check\" [substance={}, drugId={}]", component.substance, pharmaceutical.drugId);
							break components;
						}
					}
					valueIds.add(substanceId);
					unitId = (component.unit == null) ? null : unit2Id.get(component.unit);
					if (unitId == null) {
						rule = MatchAttributeRule.UNIT_MISSING_CHECK_CONCEPT;
						log.debug("Skipping pharmaceutical, cause: unit wasn't available from \"Check\" [unit={}, drugId={}]", component.unit, pharmaceutical.drugId);
						break components;
					}
					unitIds.add(unitId);
				}
			}
			if (rule == null) {
				// exact attributes
				allValueIds = new HashSet<>(valueIds);
				allValueIds.addAll(unitIds);
				matches = this.service.getAttributeExactMatch(attributeIds, allValueIds);
				if (matches.isEmpty()) {
					// exact attributes minus unit(s)
					matches = this.service.getAttributeExactMatch(attributeIds, valueIds);
					if (matches.isEmpty()) {
						// exact substances only
						valueIds.remove(doseFormId);
						matches = this.service.getAttributeExactMatch(attributeIds, valueIds);
						if (matches.isEmpty()) {
							rule = MatchAttributeRule.ZERO_MATCH;
						} else if (matches.size() == 1) {
							rule = MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM_AND_UNIT;
						} else {
							rule = MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM_AND_UNIT;
						}
					} else if (matches.size() == 1) {
						rule = MatchAttributeRule.EXACT_MATCH_EXCLUDING_UNIT;
					} else {
						rule = MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_UNIT;
					}
				} else if (matches.size() == 1) {
					rule = MatchAttributeRule.EXACT_MATCH;
				} else {
					rule = MatchAttributeRule.AMBIGUOUS_MATCH;
				}
			} else {
				matches = Collections.emptyList();
			}
			result.put(pharmaceutical,
					Pair.of(rule, matches));
		}
		log.info("Completed attribute \"Match\"");
		return result;
	}

	private Map<Pharmaceutical, Pair<MatchTermRule, DescriptionDescriptor>> matchTerms(final Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> pharmaceutical2Match) throws DrugMatchConfigurationException, IOException {
		log.info("Starting term \"Match\" ({} pharmaceuticals)", pharmaceutical2Match.size());
		// order rules by weight, for later use
		List<MatchTermRule> rules = new ArrayList<>(Arrays.asList(MatchTermRule.values()));
		Collections.sort(rules, new MatchTermRuleWeightComparator());
		rules = Collections.unmodifiableList(rules);
		// "Match" terms
		Map<Pharmaceutical, Pair<MatchTermRule, DescriptionDescriptor>> result = new LinkedHashMap<>(pharmaceutical2Match.size());
		Set<Long> conceptIds;
		Pharmaceutical pharmaceutical;
		List<ConceptSearchResultDescriptor> attributeMatches;
		List<DescriptionDescriptor> termMatches;
		DescriptionDescriptor descriptor;
		Map<MatchTermRule, List<DescriptionDescriptor>> termRule2Matches;
		String matchTerm,
			nationalNamespaceId = DrugMatchProperties.getNationalNamespaceId();
		MatchTermRule rule = null;
		Map<Long, SortedSet<DescriptionDescriptor>> conceptId2DescriptionDescriptors;
		SortedSet<DescriptionDescriptor> descriptionDescriptors;
		for (Map.Entry<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> entry : pharmaceutical2Match.entrySet()) {
			descriptor = null;
			pharmaceutical = entry.getKey();
			attributeMatches = entry.getValue().getValue();
			if (attributeMatches != null
					&& !attributeMatches.isEmpty()) {
				conceptIds = new HashSet<>();
				for (ConceptSearchResultDescriptor attributeMatch : attributeMatches) {
					conceptIds.add(attributeMatch.healthtermConceptId);
				}
				termRule2Matches = new HashMap<>();
				for (ConceptDescriptor conceptDescriptor : this.service.getConceptsByIds(conceptIds)) {
					for (DescriptionDescriptor descriptionDescriptor : conceptDescriptor.descriptionDescriptor) {
						rule = null;
						matchTerm = descriptionDescriptor.descriptionTerm;
						if (DescriptionType.FULLY_SPECIFIED_NAME.getId() == descriptionDescriptor.descriptionType.intValue()) {
							matchTerm = matchTerm.substring(0, matchTerm.lastIndexOf('(') - 1); // remove embedded hierarchy
						}
						// national
						if (nationalNamespaceId.equals(descriptionDescriptor.getNamespaceId())) {
							rule = MatchTermHelper.getMatchTermRuleNational(matchTerm,
									pharmaceutical);
						}
						// English
						if (rule == null
								&& descriptionDescriptor.descriptionLocale.startsWith("en")) {
							rule = MatchTermHelper.getMatchTermRuleEnglish(matchTerm,
									pharmaceutical);
						}
						// fall back
						if (rule == null) {
							rule = MatchTermRule.ZERO_TERM_MATCH;
						}
						termMatches = termRule2Matches.get(rule);
						if (termMatches == null) {
							termMatches = new ArrayList<>();
							termRule2Matches.put(rule, termMatches);
						}
						termMatches.add(descriptionDescriptor);
					}
				}
				// iterate over sorted rules, and return first match
				for (MatchTermRule matchTermRule : rules) {
					termMatches = termRule2Matches.get(matchTermRule);
					if (termMatches != null
							&& !termMatches.isEmpty()) {
						if (termMatches.size() == 1) {
							descriptor = termMatches.iterator().next();
							rule = matchTermRule;
						} else if (termMatches.size() > 1) {
							// group by Concept ID
							conceptId2DescriptionDescriptors = new HashMap<>();
							for (DescriptionDescriptor descriptionDescriptor : termMatches) {
								descriptionDescriptors = conceptId2DescriptionDescriptors.get(descriptionDescriptor.conceptId);
								if (descriptionDescriptors == null) {
									descriptionDescriptors = new TreeSet<>(this.descriptionDescriptorTypeComparator);
									conceptId2DescriptionDescriptors.put(descriptionDescriptor.conceptId, descriptionDescriptors);
								}
								descriptionDescriptors.add(descriptionDescriptor);
							}
							if (conceptId2DescriptionDescriptors.size() == 1) {
								descriptor = conceptId2DescriptionDescriptors.values().iterator().next().first();
								rule = matchTermRule;
							} else {
								rule = MatchTermRule.AMBIGUOUS_MATCH;
							}
						} else {
							if (MatchTermRule.ZERO_TERM_MATCH.equals(matchTermRule)) {
								rule = MatchTermRule.ZERO_TERM_MATCH;
							}
						}
						break;
					}
				}
				result.put(pharmaceutical,
						Pair.of(rule, descriptor));
			} else {
				result.put(pharmaceutical,
						Pair.of(MatchTermRule.ZERO_ATTRIBUTE_MATCH, descriptor));
			}
		}
		log.info("Completed term \"Match\"");
		return result;
	}

	private void reportAttributeMatches(final Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> pharmaceutical2Match) throws DrugMatchConfigurationException, IOException {
		log.info("Starting attribute \"Match\" report");
		String fullFileName = DrugMatchProperties.getOutputDirectory().getPath() + File.separator + "match_attribute_" + this.isoNow + ".csv";
		String quoteCharacter = DrugMatchProperties.getFileContentQuoteCharacter();
		char quoteChar = (quoteCharacter == null) ? CSVWriter.NO_QUOTE_CHARACTER : quoteCharacter.charAt(0);
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fullFileName),
						CharEncoding.UTF_8),
				Check.getOutputFileContentSeparator(),
				quoteChar)) {
			// header
			writer.writeNext(REPORT_HEADER);
			// content
			String[] columns;
			List<ConceptSearchResultDescriptor> matches;
			ConceptSearchResultDescriptor descriptor;
			for (Map.Entry<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> entry : pharmaceutical2Match.entrySet()) {
				columns = new String[6];
				columns[0] = entry.getKey().drugId;
				columns[1] = entry.getKey().tradeName;
				matches = entry.getValue().getValue();
				if (matches != null
						&& matches.size() == 1) {
					descriptor = matches.iterator().next();
					columns[2] = descriptor.conceptCode;
					columns[3] = descriptor.healthtermDescriptionId.toString();
					columns[4] = descriptor.descriptionTerm;
				}
				columns[5] = entry.getValue().getKey().toString();
				writer.writeNext(columns);
			}
			writer.flush();
			log.info("Created {}", fullFileName);
		}
		log.info("Completed attribute \"Match\" report");
	}

	private void reportTermMatches(final Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> pharmaceutical2AttributeMatch,
			final Map<Pharmaceutical, Pair<MatchTermRule, DescriptionDescriptor>> pharmaceutical2TermMatch) throws DrugMatchConfigurationException, IOException {
		log.info("Starting term \"Match\" report");
		String fullFileName = DrugMatchProperties.getOutputDirectory().getPath() + File.separator + "match_term_" + this.isoNow + ".csv";
		String quoteCharacter = DrugMatchProperties.getFileContentQuoteCharacter();
		char quoteChar = (quoteCharacter == null) ? CSVWriter.NO_QUOTE_CHARACTER : quoteCharacter.charAt(0);
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fullFileName),
						CharEncoding.UTF_8),
				Check.getOutputFileContentSeparator(),
				quoteChar)) {
			// header
			writer.writeNext(REPORT_HEADER);
			// content
			String[] columns;
			DescriptionDescriptor descriptor;
			for (Map.Entry<Pharmaceutical, Pair<MatchTermRule, DescriptionDescriptor>> entry : pharmaceutical2TermMatch.entrySet()) {
				columns = new String[6];
				columns[0] = entry.getKey().drugId;
				columns[1] = entry.getKey().tradeName;
				descriptor = entry.getValue().getValue();
				if (descriptor != null) {
					columns[2] = descriptor.conceptId.toString();
					columns[3] = descriptor.descriptionId.toString();
					columns[4] = descriptor.descriptionTerm;
				}
				if (MatchTermRule.ZERO_ATTRIBUTE_MATCH.equals(entry.getValue().getKey())) {
					columns[5] = this.matchRuleHelper.getMessage(pharmaceutical2AttributeMatch.get(entry.getKey()).getKey());
				} else {
					columns[5] = this.matchRuleHelper.getMessage(entry.getValue().getKey());
				}
				writer.writeNext(columns);
			}
			writer.flush();
			log.info("Created {}", fullFileName);
		}
		log.info("Completed term \"Match\" report");
	}
}