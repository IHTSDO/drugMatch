package org.ihtsdo.sct.drugmatch.match;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ihtsdo.sct.drugmatch.check.Check;
import org.ihtsdo.sct.drugmatch.comparator.DescriptionDescriptorTypeComparator;
import org.ihtsdo.sct.drugmatch.constant.Constant;
import org.ihtsdo.sct.drugmatch.constant.rf1.DescriptionType;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchStrictModeViolationException;
import org.ihtsdo.sct.drugmatch.match.extension.MatchRuleUtil;
import org.ihtsdo.sct.drugmatch.match.model.GenericMatch;
import org.ihtsdo.sct.drugmatch.match.model.PharmaceuticalMatch;
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
 * DrugMatch "Match".
 * @author dev-team@carecom.dk
 */
public class Match {

	private static final Logger log = LoggerFactory.getLogger(Match.class);

	/**
	 * Implicit "Check" dependency.
	 */
	private final Check check;

	/**
	 * {@link Map}(Component name, SNOMED CT Concept ID).
	 */
	private Map<String, Long> doseForm2Id,
			substance2Id,
			unit2Id;

	private final MatchRuleHelper matchRuleHelper;

	private Map<Pharmaceutical, Pair<PharmaceuticalMatch, GenericMatch>> pharmaceutical2TermMatches;

	/**
	 * Used to generate the report.
	 */
	private final List<Pharmaceutical> pharmaceuticals;

	/**
	 * YYYY-MM-DD HH.MM.SS.
	 */
	private final String isoNow;

	private static final String[] REPORT_HEADER = new String[] {
				"Drug ID",
				"Trade name",
				"SCT Concept ID",
				"SCT Description ID",
				"SCT Term",
				"Rule match",
				"Ambiguous matches"
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

	/**
	 * Execute DrugMatch "Match" and implicit dependency "Check".
	 * @param matchAttributeReport
	 * @throws DrugMatchConfigurationException
	 * @throws DrugMatchStrictModeViolationException
	 * @throws IOException
	 */
	public final void execute(final boolean matchAttributeReport) throws DrugMatchConfigurationException, DrugMatchStrictModeViolationException, IOException {
		log.info("Starting \"Match\"");
		// implicit "Check" dependency
		this.check.execute();
		// retrieve Component name to Concept ID
		this.doseForm2Id = this.check.getDoseForm2Id();
		this.substance2Id = this.check.getSubstance2Id();
		this.unit2Id = this.check.getUnit2Id();
		// attribute match
		Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> pharmaceutical2AttributeMatch = matchAttributes();
		if (matchAttributeReport) {
			reportAttributeMatches(pharmaceutical2AttributeMatch);
		}
		// term match
		this.pharmaceutical2TermMatches = matchTerms(pharmaceutical2AttributeMatch);
		reportTermMatches(pharmaceutical2AttributeMatch);
		log.info("Completed \"Match\"");
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public final Map<String, Long> getDoseForm2Id() {
		if (this.doseForm2Id == null) {
			throw new IllegalStateException("Blocked attempt to retrieve doseForm2Id prior to execute() invocation!");
		}
		return this.doseForm2Id;
	}

	/**
	 * @return {@link Map}({@link Pharmaceutical}, {@link Pair}({@link PharmaceuticalMatch}, {@link GenericMatch}))
	 */
	public final Map<Pharmaceutical, Pair<PharmaceuticalMatch, GenericMatch>> getPharmaceutical2TermMatches() {
		if (this.pharmaceutical2TermMatches == null) {
			throw new IllegalStateException("Blocked attempt to retrieve pharmaceutical2TermMatches prior to execute() invocation!");
		}
		return this.pharmaceutical2TermMatches;
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public final Map<String, Long> getSubstance2Id() {
		if (this.substance2Id == null) {
			throw new IllegalStateException("Blocked attempt to retrieve substance2Id prior to execute() invocation!");
		}
		return this.substance2Id;
	}

	/**
	 * @return {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	public final Map<String, Long> getUnit2Id() {
		if (this.unit2Id == null) {
			throw new IllegalStateException("Blocked attempt to retrieve unit2Id prior to execute() invocation!");
		}
		return this.unit2Id;
	}

	/**
	 * Retrieve best attribute relationship "Match".
	 * @return {@link Map}({@link Pharmaceutical}, {@link Pair}({@link MatchAttributeRule}, {@link List}({@link ConceptSearchResultDescriptor})))
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> matchAttributes() throws DrugMatchConfigurationException, IOException {
		log.info("Starting attribute \"Match\" ({} pharmaceuticals)", String.valueOf(this.pharmaceuticals.size()));
		Long attributeIdHasActiveIngredient = DrugMatchProperties.getAttributeIdHasActiveIngredient();
		if (attributeIdHasActiveIngredient == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.ATTRIBUTE_ID_HAS_ACTIVE_INGREDIENT + "' isn't set!");
		} // else
		Long attributeIdHasDoseForm = DrugMatchProperties.getAttributeIdHasDoseForm();
		if (attributeIdHasDoseForm == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.ATTRIBUTE_ID_HAS_DOSE_FORM + "' isn't set!");
		} // else
		Set<Long> allAttributeIds = new HashSet<>(),
			attributeIdsHasActiveIngredient = Collections.singleton(attributeIdHasActiveIngredient),
			valueIds;
		allAttributeIds.add(attributeIdHasActiveIngredient);
		allAttributeIds.add(attributeIdHasDoseForm);
		Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> result = new LinkedHashMap<>(this.pharmaceuticals.size());
		Long doseFormId,
			substanceId;
		MatchAttributeRule rule;
		List<ConceptSearchResultDescriptor> matches;
		for (Pharmaceutical pharmaceutical : this.pharmaceuticals) {
			valueIds = new HashSet<>();
			rule = null;
			doseFormId = (pharmaceutical.doseForm.nameNational == null) ? null : this.doseForm2Id.get(pharmaceutical.doseForm.nameNational);
			if (doseFormId == null) {
				doseFormId = (pharmaceutical.doseForm.nameEnglish == null) ? null : this.doseForm2Id.get(pharmaceutical.doseForm.nameEnglish);
			}
			if (doseFormId == null) {
				rule = MatchAttributeRule.DOSE_FORM_MISSING_CHECK_CONCEPT;
				log.debug("Skipping pharmaceutical, cause: dose form wasn't available from \"Check\" [doseForm={}, drugId={}]", pharmaceutical.doseForm, pharmaceutical.drugId);
			} else {
				valueIds.add(doseFormId);
				components : for (Component component : pharmaceutical.components) {
					substanceId = (component.substance.nameNational == null) ? null : this.substance2Id.get(component.substance.nameNational);
					if (substanceId == null) {
						substanceId = (component.substance.nameEnglish == null) ? null : this.substance2Id.get(component.substance.nameEnglish);
						if (substanceId == null) {
							rule = MatchAttributeRule.SUBSTANCE_MISSING_CHECK_CONCEPT;
							log.debug("Skipping pharmaceutical, cause: substance wasn't available from \"Check\" [substance={}, drugId={}]", component.substance, pharmaceutical.drugId);
							break components;
						}
					}
					valueIds.add(substanceId);
				}
			}
			if (rule == null) {
				// exact attributes
				matches = this.service.getAttributeExactMatch(allAttributeIds,
						valueIds);
				if (matches.isEmpty()) {
					// exact substances only
					valueIds.remove(doseFormId);
					matches = this.service.getAttributeExactMatch(attributeIdsHasActiveIngredient,
							valueIds);
					if (matches.isEmpty()) {
						rule = MatchAttributeRule.ZERO_MATCH;
					} else if (matches.size() == 1) {
						rule = MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM;
					} else {
						rule = MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM;
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

	/**
	 * Retrieve best term "Match".
	 * @param pharmaceutical2Match
	 * @return
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private Map<Pharmaceutical, Pair<PharmaceuticalMatch, GenericMatch>> matchTerms(final Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> pharmaceutical2Match) throws DrugMatchConfigurationException, IOException {
		log.info("Starting term \"Match\" ({} pharmaceuticals)", String.valueOf(pharmaceutical2Match.size()));
		Map<Pharmaceutical, Pair<PharmaceuticalMatch, GenericMatch>> result = new LinkedHashMap<>(pharmaceutical2Match.size());
		// "Match" terms
		Set<Long> conceptIds;
		Pharmaceutical pharmaceutical;
		List<ConceptSearchResultDescriptor> attributeMatches;
		List<DescriptionDescriptor> termMatches;
		Map<MatchTermRule, List<DescriptionDescriptor>> termRule2Matches;
		String matchTerm,
			nationalNamespaceId = DrugMatchProperties.getNationalNamespaceId();
		MatchAttributeRule attributeRule;
		MatchTermRule termRule = null;
		for (Map.Entry<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> entry : pharmaceutical2Match.entrySet()) {
			pharmaceutical = entry.getKey();
			attributeRule = entry.getValue().getKey();
			attributeMatches = entry.getValue().getValue();
			if (attributeMatches == null
					|| attributeMatches.isEmpty()) {
				result.put(pharmaceutical,
						Pair.of(new PharmaceuticalMatch(Collections.<DescriptionDescriptor>emptyList(),
										null,
										MatchTermRule.ZERO_ATTRIBUTE_MATCH),
								new GenericMatch(Collections.<DescriptionDescriptor>emptyList(),
										null,
										MatchTermRule.ZERO_ATTRIBUTE_MATCH)));
			} else {
				conceptIds = new HashSet<>(attributeMatches.size());
				for (ConceptSearchResultDescriptor attributeMatch : attributeMatches) {
					conceptIds.add(attributeMatch.healthtermConceptId);
				}
				termRule2Matches = new HashMap<>();
				for (ConceptDescriptor conceptDescriptor : this.service.getConceptsByIds(conceptIds)) {
					for (DescriptionDescriptor descriptionDescriptor : conceptDescriptor.descriptionDescriptor) {
						termRule = null;
						matchTerm = descriptionDescriptor.descriptionTerm;
						if (DescriptionType.FULLY_SPECIFIED_NAME.getId() == descriptionDescriptor.descriptionType.intValue()) {
							matchTerm = matchTerm.substring(0, matchTerm.lastIndexOf('(') - 1); // remove embedded hierarchy
						}
						// format excessive whitespace to follow expected convention
						matchTerm = StringUtils.normalizeSpace(matchTerm);
						// national
						if (nationalNamespaceId.equals(descriptionDescriptor.getNamespaceId())) {
							termRule = MatchTermHelper.getMatchPharmaceuticalRuleNational(attributeRule,
									matchTerm,
									pharmaceutical);
							if (termRule == null) {
								termRule = MatchTermHelper.getMatchTermRuleNational(attributeRule,
										matchTerm,
										pharmaceutical);
							}
						}
						// generic English
						if (termRule == null
								&& descriptionDescriptor.descriptionLocale.startsWith("en")) {
							termRule = MatchTermHelper.getMatchPharmaceuticalRuleEnglish(attributeRule,
									matchTerm,
									pharmaceutical);
							if (termRule == null) {
								termRule = MatchTermHelper.getMatchTermRuleEnglish(attributeRule,
										matchTerm,
										pharmaceutical);
							}
						}
						// fall back
						if (termRule == null) {
							termRule = MatchTermRule.ZERO_TERM_MATCH;
						}
						termMatches = termRule2Matches.get(termRule);
						if (termMatches == null) {
							termMatches = new ArrayList<>();
							termRule2Matches.put(termRule, termMatches);
						}
						termMatches.add(descriptionDescriptor);
					}
				}
				result.put(pharmaceutical,
						getMatch(termRule2Matches));
			}
		}
		log.info("Completed term \"Match\"");
		return result;
	}

	/**
	 * Extract the "best" {@link PharmaceuticalMatch} and {@link GenericMatch} from the given argument.
	 * @param termRule2Matches
	 * @return {@link Pair}({@link PharmaceuticalMatch}, {@link GenericMatch})
	 */
	public static Pair<PharmaceuticalMatch, GenericMatch> getMatch(final Map<MatchTermRule, List<DescriptionDescriptor>> termRule2Matches) {
		List<DescriptionDescriptor> termMatches;
		DescriptionDescriptor descriptor = null;
		MatchTermRule rule = null;
		Map<Long, SortedSet<DescriptionDescriptor>> conceptId2DescriptionDescriptors;
		SortedSet<DescriptionDescriptor> descriptionDescriptors;
		GenericMatch genericMatch = null;
		PharmaceuticalMatch pharmaceuticalMatch = null;
		// iterate over weighted rules
		for (MatchTermRule matchTermRule : Constant.WEIGHTED_RULES) {
			termMatches = termRule2Matches.get(matchTermRule);
			if (termMatches != null) {
				if (termMatches.size() == 1) {
					descriptor = termMatches.iterator().next();
					rule = matchTermRule;
				} else if (termMatches.size() > 1) {
					// group by Concept ID
					conceptId2DescriptionDescriptors = new HashMap<>();
					for (DescriptionDescriptor descriptionDescriptor : termMatches) {
						descriptionDescriptors = conceptId2DescriptionDescriptors.get(descriptionDescriptor.conceptId);
						if (descriptionDescriptors == null) {
							descriptionDescriptors = new TreeSet<>(new DescriptionDescriptorTypeComparator());
							conceptId2DescriptionDescriptors.put(descriptionDescriptor.conceptId, descriptionDescriptors);
						}
						descriptionDescriptors.add(descriptionDescriptor);
					}
					if (conceptId2DescriptionDescriptors.size() == 1) {
						descriptor = conceptId2DescriptionDescriptors.values().iterator().next().first();
					} else {
						descriptor = null;
					}
					rule = matchTermRule;
				} else if (MatchTermRule.ZERO_TERM_MATCH.equals(matchTermRule)) {
					rule = MatchTermRule.ZERO_TERM_MATCH;
				}
				// evaluate rule
				if (rule != null) {
					if (rule.isGeneric()
							&& genericMatch == null) {
						// only save first match!
						genericMatch = new GenericMatch(termMatches,
								descriptor,
								rule);
					} else if (!rule.isGeneric()
							&& pharmaceuticalMatch == null) {
						// only save first match!
						pharmaceuticalMatch = new PharmaceuticalMatch(termMatches,
								descriptor,
								rule);
					}
				}
			}
			if (genericMatch != null
					&& pharmaceuticalMatch != null) {
				break;
			}
		}
		return Pair.of(pharmaceuticalMatch, genericMatch);
	}

	/**
	 * Export attribute relationship "Match" report.
	 * @param pharmaceutical2Match
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void reportAttributeMatches(final Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> pharmaceutical2Match) throws DrugMatchConfigurationException, IOException {
		log.info("Starting attribute \"Match\" report");
		String fullFileName = DrugMatchProperties.getReportDirectory().getPath() + File.separator + "match_attribute_" + this.isoNow + ".csv";
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
			SortedSet<Long> conceptIds;
			for (Map.Entry<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> entry : pharmaceutical2Match.entrySet()) {
				columns = new String[7];
				columns[0] = entry.getKey().drugId;
				columns[1] = entry.getKey().tradeName;
				matches = entry.getValue().getValue();
				if (matches != null) {
					if (matches.size() == 1) {
						descriptor = matches.iterator().next();
						columns[2] = descriptor.conceptCode;
						columns[3] = descriptor.healthtermDescriptionId.toString();
						columns[4] = descriptor.descriptionTerm;
					} else if (matches.size() > 1) {
						conceptIds = new TreeSet<>();
						for (ConceptSearchResultDescriptor match : matches) {
							conceptIds.add(match.healthtermConceptId);
						}
						columns[6] = this.service.getConceptsByIdsUrl(conceptIds);
					}
				}
				columns[5] = entry.getValue().getKey().toString();
				writer.writeNext(columns);
			}
			writer.flush();
			log.info("Created {}", fullFileName);
		}
		log.info("Completed attribute \"Match\" report");
	}

	/**
	 * Export term "Match" report.
	 * @param pharmaceutical2AttributeMatch
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void reportTermMatches(final Map<Pharmaceutical, Pair<MatchAttributeRule, List<ConceptSearchResultDescriptor>>> pharmaceutical2AttributeMatch) throws DrugMatchConfigurationException, IOException {
		log.info("Starting term \"Match\" report");
		String fullFileName = DrugMatchProperties.getReportDirectory().getPath() + File.separator + "match_term_" + this.isoNow + ".csv";
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
			PharmaceuticalMatch pharmaceuticalMatch;
			GenericMatch genericMatch;
			DescriptionDescriptor descriptor;
			MatchTermRule rule;
			List<DescriptionDescriptor> ambiguousDescriptors;
			SortedSet<Long> conceptIds;
			for (Map.Entry<Pharmaceutical, Pair<PharmaceuticalMatch, GenericMatch>> entry : getPharmaceutical2TermMatches().entrySet()) {
				columns = new String[7];
				columns[0] = entry.getKey().drugId;
				columns[1] = entry.getKey().tradeName;
				// SCT columns
				pharmaceuticalMatch = entry.getValue().getKey();
				genericMatch = entry.getValue().getValue();
				if (pharmaceuticalMatch == null
						|| pharmaceuticalMatch.descriptor == null) {
					if (genericMatch == null
							|| genericMatch.descriptor == null) {
						descriptor = null;
					} else {
						descriptor = genericMatch.descriptor;
					}
				} else {
					descriptor = pharmaceuticalMatch.descriptor;
				}
				if (descriptor != null) {
					columns[2] = descriptor.conceptId.toString();
					columns[3] = descriptor.descriptionId.toString();
					columns[4] = descriptor.descriptionTerm;
				}
				// rule
				if (pharmaceuticalMatch == null
						|| pharmaceuticalMatch.rule == null) {
					if (genericMatch == null
							|| genericMatch.rule == null) {
						rule = null;
					} else {
						rule = genericMatch.rule;
					}
				} else {
					rule = pharmaceuticalMatch.rule;
				}
				if (MatchTermRule.ZERO_ATTRIBUTE_MATCH.equals(rule)) {
					columns[5] = this.matchRuleHelper.getMessage(pharmaceutical2AttributeMatch.get(entry.getKey()).getKey());
				} else {
					if (descriptor == null) {
						columns[5] = this.matchRuleHelper.getAmbiguousMessage(rule);
						// ambiguous
						if (pharmaceuticalMatch == null) {
							if (genericMatch == null) {
								ambiguousDescriptors = null;
							} else {
								ambiguousDescriptors = genericMatch.ambiguousDescriptors;
							}
						} else {
							ambiguousDescriptors = pharmaceuticalMatch.ambiguousDescriptors;
						}
						if (ambiguousDescriptors != null) {
							conceptIds = new TreeSet<>();
							for (DescriptionDescriptor ambiguousDescriptor : ambiguousDescriptors) {
								conceptIds.add(ambiguousDescriptor.conceptId);
							}
							columns[6] = this.service.getConceptsByIdsUrl(conceptIds);
						}
					} else {
						columns[5] = this.matchRuleHelper.getMessage(rule);
					}
				}
				writer.writeNext(columns);
			}
			writer.flush();
			log.info("Created {}", fullFileName);
		}
		log.info("Completed term \"Match\" report");
	}
}
