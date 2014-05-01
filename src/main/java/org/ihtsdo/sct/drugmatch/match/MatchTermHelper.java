package org.ihtsdo.sct.drugmatch.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;

/**
 * @author dev-team@carecom.dk
 */
public final class MatchTermHelper {

	private static final Pattern REGEX_PATTERN_DIGIT = Pattern.compile("\\d");

	private MatchTermHelper() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempt to extract "Substance strength unit" groups from the given term.
	 * <p>
	 * Supports "Substance strength unit + Substance strength unit" & "Substance + Substance strength unit / strength unit" notation.
	 * @param componentTerm
	 * @return "Substance strength unit" groups
	 */
	public static String[] getComponentTermTokens(final String componentTerm) {
		String[] plusTokens = componentTerm.split("\\+");
		if (plusTokens.length > 1
				&& (plusTokens.length - 1) == StringUtils.countMatches(componentTerm, "/")) {
			String[] slashTokens = componentTerm.split("/"),
				result = new String[plusTokens.length];
			// attempt to group substance with strength & unit
			Matcher matcher;
			String substance;
			for (int i = 0; i < plusTokens.length; i++) {
				if (i == 0) {
					// first
					matcher = REGEX_PATTERN_DIGIT.matcher(slashTokens[i]);
					if (matcher.find()) {
						result[i] = plusTokens[i] + " " + slashTokens[i].substring(matcher.start());
					}
				} else if (i == (plusTokens.length - 1)) {
					// last
					int indexOf = plusTokens[i].indexOf('/');
					if (indexOf > -1) {
						substance = plusTokens[i].substring(0, indexOf);
						matcher = REGEX_PATTERN_DIGIT.matcher(substance);
						if (matcher.find()) {
							result[i] = substance.substring(0, matcher.start()) + " " + slashTokens[i];
						}
					}
				}
				// fallback, ie. not first or last token
				if (result[i] == null) {
					// default
					result[i] = plusTokens[i] + " " + slashTokens[i];
				}
			}
			return result;
		}
		return plusTokens;
	}

	public static MatchTermRule getMatchTermRuleEnglish(final String term,
			final Pharmaceutical pharmaceutical) {
		MatchTermRule rule = null;
		Pharmaceutical termPharmaceutical = MatchTermHelper.getTermPharmaceutical(term,
				true, // isEnglish
				pharmaceutical);
		if (pharmaceutical.components.size() == termPharmaceutical.components.size()) {
			Component component,
				termComponent;
			boolean unexpectedComponentOrder = false,
					componentPresent;
			for (int i = 0; i < pharmaceutical.components.size(); i++) {
				if (rule == null) {
					component = pharmaceutical.components.get(i);
					termComponent = termPharmaceutical.components.get(i);
					// substance
					if (!component.substance.nameEnglish.equalsIgnoreCase(termComponent.substance.nameEnglish)
							|| !component.strength.equalsIgnoreCase(termComponent.strength)
							|| !component.unit.equalsIgnoreCase(termComponent.unit)) {
						componentPresent = false;
						for (Component tc : termPharmaceutical.components) {
							if (component.substance.nameEnglish.equalsIgnoreCase(tc.substance.nameEnglish)
									&& component.strength.equalsIgnoreCase(tc.strength)
									&& component.unit.equalsIgnoreCase(tc.unit)) {
								componentPresent = true;
								break;
							}
						}
						if (componentPresent) {
							unexpectedComponentOrder = true;
						} else {
							if (!component.substance.nameEnglish.equalsIgnoreCase(termComponent.substance.nameEnglish)) {
								rule = MatchTermRule.MISSING_ENGLISH_SUBSTANCE;
							} else if (!component.strength.equalsIgnoreCase(termComponent.strength)) {
								rule = MatchTermRule.MISSING_ENGLISH_STRENGTH;
							} else if (!component.unit.equalsIgnoreCase(termComponent.unit)) {
								rule = MatchTermRule.MISSING_ENGLISH_UNIT;
							}
						}
					}
				}
			}
			// dose form
			if (termPharmaceutical.doseForm.nameEnglish == null) {
				rule = MatchTermRule.MISSING_ENGLISH_DOSE_FORM;
			}
			if (rule == null) {
				if (unexpectedComponentOrder) {
					rule = MatchTermRule.INCORRECT_COMPONENT_ORDER_ENGLISH;
				} else {
					rule = MatchTermRule.CASE_INSENSITIVE_ENGLISH_MATCH;
				}
			}
		}
		if (rule == null) {
			// fallback on broader text matching
			String englishTerm = pharmaceutical.getEnglishTerm();
			if (term.equals(englishTerm)) {
				rule = MatchTermRule.EXACT_ENGLISH_MATCH;
			} else if (term.equalsIgnoreCase(englishTerm)) {
				rule = MatchTermRule.CASE_INSENSITIVE_ENGLISH_MATCH;
			} else {
				boolean strengthPresent,
					unitPresent;
				String[] termTokens = term.split("\\s+");
				for (Component component : pharmaceutical.components) {
					if (!term.contains(component.getEnglish())) {
						// substance
						if (!term.toLowerCase(Locale.ENGLISH).contains(component.substance.nameEnglish.toLowerCase(Locale.ENGLISH))) {
							rule = MatchTermRule.MISSING_ENGLISH_SUBSTANCE;
							break;
						}
						// strength & unit
						strengthPresent = false;
						unitPresent = false;
						for (String termToken : termTokens) {
							if (component.strength.equalsIgnoreCase(termToken.trim())) {
								strengthPresent = true;
							} else if (component.unit.equalsIgnoreCase(termToken.trim())) {
								unitPresent = true;
							} else if (termToken.toLowerCase(Locale.ENGLISH).contains(component.strength.toLowerCase(Locale.ENGLISH) + component.unit.toLowerCase(Locale.ENGLISH))) {
								strengthPresent = true;
								unitPresent = true;
							}
						}
						if (!strengthPresent) {
							rule = MatchTermRule.MISSING_ENGLISH_STRENGTH;
							break;
						} else if (!unitPresent) {
							rule = MatchTermRule.MISSING_ENGLISH_UNIT;
							break;
						}
					}
				}
				// dose form
				if (rule == null) {
					if (!term.toLowerCase(Locale.ENGLISH).contains(pharmaceutical.doseForm.nameEnglish.toLowerCase(Locale.ENGLISH))) {
						rule = MatchTermRule.MISSING_ENGLISH_DOSE_FORM;
					} else {
						rule = MatchTermRule.INCORRECT_COMPONENT_ORDER_ENGLISH;
					}
				}
			}
		}
		return rule;
	}

	public static MatchTermRule getMatchTermRuleNational(final String term,
			final Pharmaceutical pharmaceutical) {
		MatchTermRule rule = null;
		Pharmaceutical termPharmaceutical = MatchTermHelper.getTermPharmaceutical(term,
				false, // isEnglish
				pharmaceutical);
		if (pharmaceutical.components.size() == termPharmaceutical.components.size()) {
			Component component,
				termComponent;
			boolean unexpectedComponentOrder = false,
				componentPresent;
			for (int i = 0; i < pharmaceutical.components.size(); i++) {
				if (rule == null) {
					component = pharmaceutical.components.get(i);
					termComponent = termPharmaceutical.components.get(i);
					// substance
					if (!component.substance.nameNational.equalsIgnoreCase(termComponent.substance.nameNational)
							|| !component.strength.equalsIgnoreCase(termComponent.strength)
							|| !component.unit.equalsIgnoreCase(termComponent.unit)) {
						componentPresent = false;
						for (Component tc : termPharmaceutical.components) {
							if (component.substance.nameNational.equalsIgnoreCase(tc.substance.nameNational)
									&& component.strength.equalsIgnoreCase(tc.strength)
									&& component.unit.equalsIgnoreCase(tc.unit)) {
								componentPresent = true;
								break;
							}
						}
						if (componentPresent) {
							unexpectedComponentOrder = true;
						} else {
							if (!component.substance.nameNational.equalsIgnoreCase(termComponent.substance.nameNational)) {
								rule = MatchTermRule.MISSING_NATIONAL_SUBSTANCE;
							} else if (!component.strength.equalsIgnoreCase(termComponent.strength)) {
								rule = MatchTermRule.MISSING_NATIONAL_STRENGTH;
							} else if (!component.unit.equalsIgnoreCase(termComponent.unit)) {
								rule = MatchTermRule.MISSING_NATIONAL_UNIT;
							}
						}
					}
				}
			}
			// dose form
			if (termPharmaceutical.doseForm.nameNational == null) {
				rule = MatchTermRule.MISSING_NATIONAL_DOSE_FORM;
			}
			if (rule == null) {
				if (unexpectedComponentOrder) {
					rule = MatchTermRule.INCORRECT_COMPONENT_ORDER_NATIONAL;
				} else {
					rule = MatchTermRule.CASE_INSENSITIVE_NATIONAL_MATCH;
				}
			}
		}
		if (rule == null) {
			// fallback on broader text matching
			String nationalTerm = pharmaceutical.getNationalTerm();
			if (term.equals(nationalTerm)) {
				rule = MatchTermRule.EXACT_NATIONAL_MATCH;
			} else if (term.equalsIgnoreCase(nationalTerm)) {
				rule = MatchTermRule.CASE_INSENSITIVE_NATIONAL_MATCH;
			} else {
				boolean strengthPresent,
					unitPresent;
				String[] termTokens = term.split("\\s+");
				for (Component component : pharmaceutical.components) {
					if (!term.contains(component.getNational())) {
						// substance
						if (!term.toLowerCase(Locale.ENGLISH).contains(component.substance.nameNational.toLowerCase(Locale.ENGLISH))) {
							rule = MatchTermRule.MISSING_NATIONAL_SUBSTANCE;
							break;
						}
						// strength & unit
						strengthPresent = false;
						unitPresent = false;
						for (String termToken : termTokens) {
							if (component.strength.equalsIgnoreCase(termToken.trim())) {
								strengthPresent = true;
							} else if (component.unit.equalsIgnoreCase(termToken.trim())) {
								unitPresent = true;
							} else if (termToken.toLowerCase(Locale.ENGLISH).contains(component.strength.toLowerCase(Locale.ENGLISH) + component.unit.toLowerCase(Locale.ENGLISH))) {
								strengthPresent = true;
								unitPresent = true;
							}
						}
						if (!strengthPresent) {
							rule = MatchTermRule.MISSING_NATIONAL_STRENGTH;
							break;
						} else if (!unitPresent) {
							rule = MatchTermRule.MISSING_NATIONAL_UNIT;
							break;
						}
					}
				}
				// dose form
				if (rule == null) {
					if (!term.toLowerCase(Locale.ENGLISH).contains(pharmaceutical.doseForm.nameNational.toLowerCase(Locale.ENGLISH))) {
						rule = MatchTermRule.MISSING_NATIONAL_DOSE_FORM;
					} else {
						rule = MatchTermRule.INCORRECT_COMPONENT_ORDER_NATIONAL;
					}
				}
			}
		}
		return rule;
	}

	/**
	 * Attempt to extract dose form from the given term, returning the first match following this approach:
	 * <ul>
	 * <li>ends with expectedDoseForm (case insensitive)</li>
	 * <li>tokenize expectedDoseForm, and attempt to match each token from the back of the term (case insensitive)</li>
	 * <li>locate the last unit, and return the trailing value (case insensitive)</li>
	 * </ul>
	 * @param term assumed to contain a desired dose form
	 * @param expectedComponents
	 * @param expectedDoseForm
	 * @return dose form candidate, or null
	 */
	public static String getTermDoseForm(final String term,
			final List<Component> expectedComponents,
			final String expectedDoseForm) {
		// attempt to match dose form
		String doseForm = null;
		if (term.toLowerCase(Locale.ENGLISH).endsWith(expectedDoseForm.toLowerCase(Locale.ENGLISH))) {
			doseForm = term.substring(term.toLowerCase(Locale.ENGLISH).lastIndexOf(expectedDoseForm.toLowerCase(Locale.ENGLISH)));
		} else {
			// match backwards one dose form token at a time
			String[] expectedDoseFormTokens = expectedDoseForm.split("\\s+"),
					termTokens = term.split("\\s+");
			if (expectedDoseFormTokens.length > 1
					&& termTokens.length >= expectedDoseFormTokens.length) {
				StringBuilder dfBuilder = new StringBuilder();
				int t = termTokens.length - 1;
				for (int d = expectedDoseFormTokens.length - 1; d >= 0; d--) {
					if (expectedDoseFormTokens[d].equalsIgnoreCase(termTokens[t])) {
						dfBuilder.insert(0,
								" " + termTokens[t]);
					} else {
						break;
					}
					t--;
				}
				if (dfBuilder.length() > 0) {
					doseForm = dfBuilder.toString().trim();
				}
			}
		}
		if (doseForm == null) {
			// attempt to deduct dose form, based on last unit
			int lastIndexOfUnit = -1,
				lastIndexOf;
			for (Component component : expectedComponents) {
				lastIndexOf = term.lastIndexOf(component.unit);
				if (lastIndexOf == -1) {
					lastIndexOf = term.toLowerCase(Locale.ENGLISH).lastIndexOf(component.unit.toLowerCase(Locale.ENGLISH));
				}
				if (lastIndexOf > -1
						&& lastIndexOf > lastIndexOfUnit) {
					lastIndexOfUnit = lastIndexOf + component.unit.length();
				}
			}
			if (lastIndexOfUnit > -1
					&& lastIndexOfUnit < term.length()) {
				doseForm = term.substring(lastIndexOfUnit).trim();
			}
		}
		return doseForm;
	}

	/**
	 * Attempt to generate {@link Pharmaceutical} representation based on a SNOMED CT term, following this approach:
	 * <ul>
	 * <li>attempt to extract dose form from term, see also {@link MatchTermHelper#getTermDoseForm(String, List, String)}</li>
	 * <li>attempt to extract {@link Component} tokens from term, see also {@link MatchTermHelper#getComponentTermTokens(String)}</li>
	 * <li>for each expected {@link Component} match substance (case insensitive), strength & unit.
	 * If matched, add to {@link Component}s and include in returned {@link Pharmaceutical}
	 * </li>
	 * </ul>
	 * @param term
	 * @param isEnglish
	 * @param expectedPharmaceutical
	 * @return {@link Pharmaceutical} based on the given term, excluding drugId & tradeName
	 */
	public static Pharmaceutical getTermPharmaceutical(final String term,
			final boolean isEnglish,
			final Pharmaceutical expectedPharmaceutical) {
		String componentTerm = term;
		// Remove dose form
		String doseForm = getTermDoseForm(componentTerm,
				expectedPharmaceutical.components,
				(isEnglish) ? expectedPharmaceutical.doseForm.nameEnglish : expectedPharmaceutical.doseForm.nameNational);
		if (doseForm != null) {
			componentTerm = componentTerm.substring(0,
					componentTerm.lastIndexOf(doseForm)).trim();
		}
		// extract components
		int indexOf;
		String componentTermToken,
			substanceName,
			strength,
			unit;
		List<Component> components = new ArrayList<>(expectedPharmaceutical.components.size());
		String[] componentTermTokens = getComponentTermTokens(componentTerm);
		for (Component component : expectedPharmaceutical.components) {
			for (int i = 0; i < componentTermTokens.length; i++) {
				componentTermToken = componentTermTokens[i];
				if (componentTermToken != null) {
					indexOf = componentTerm.toLowerCase(Locale.ENGLISH).indexOf((isEnglish) ? component.substance.nameEnglish.toLowerCase(Locale.ENGLISH) : component.substance.nameNational.toLowerCase(Locale.ENGLISH));
					substanceName = (indexOf > -1) ? componentTerm.substring(indexOf, (indexOf + ((isEnglish) ? component.substance.nameEnglish.length() : component.substance.nameNational.length()))).trim() : null;
					strength = null;
					unit = null;
					for (String token : componentTermToken.split("\\s+")) {
						if (component.strength.equalsIgnoreCase(token.trim())) {
							strength = token.trim();
						} else if (component.unit.equalsIgnoreCase(token.trim())) {
							unit = token.trim();
						} else {
							if (token.equalsIgnoreCase(component.strength + component.unit)) {
								indexOf = token.toLowerCase(Locale.ENGLISH).indexOf(component.strength.toLowerCase(Locale.ENGLISH));
								strength = (indexOf > -1) ? token.substring(indexOf, (indexOf + component.strength.length())).trim() : null;
								indexOf = token.toLowerCase(Locale.ENGLISH).indexOf(component.unit.toLowerCase(Locale.ENGLISH));
								unit = (indexOf > -1) ? token.substring(indexOf, (indexOf + component.unit.length())).trim() : null;
								break;
							}
						}
					}
					if (substanceName != null
							&& strength != null
							&& unit != null) {
						components.add(new Component((isEnglish) ? substanceName : null,
								(isEnglish) ? null : substanceName,
								strength,
								unit));
						componentTermTokens[i] = null; // excluding component match from the following components
						break;
					}
				}
			}
		}
		return new Pharmaceutical(components,
				(isEnglish) ? doseForm : null,
				(isEnglish) ? null : doseForm,
				null, // drugId
				null); // tradeName
	}
}
