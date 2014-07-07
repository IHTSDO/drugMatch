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

	private static final Pattern REGEX_DIGIT = Pattern.compile("\\d");

	/**
	 * DON'T INSTANTIATE A STATIC HELPER!
	 */
	private MatchTermHelper() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempt to extract "Substance strength unit" groups from the given term.
	 * <p>
	 * Supports "Substance A strength unit + Substance B strength unit", "Substance A strength unit / Substance B strength unit" & "Substance A + Substance B strength unit / strength unit" notation.
	 * @param componentTerm
	 * @return "Substance strength unit" groups
	 */
	public static String[] getComponentTermTokens(final String componentTerm) {
		int plusCount = StringUtils.countMatches(componentTerm, "+"),
			slashCount = StringUtils.countMatches(componentTerm, "/");
		if (plusCount > 1
				&& plusCount == slashCount) {
			String[] plusTokens = componentTerm.split("\\+"),
				slashTokens = componentTerm.split("/"),
				result = new String[plusTokens.length];
			// attempt to group substance with strength & unit
			Matcher matcher;
			String substance;
			for (int i = 0; i < plusTokens.length; i++) {
				if (i == 0) {
					// first
					matcher = REGEX_DIGIT.matcher(slashTokens[i]);
					if (matcher.find()) {
						result[i] = plusTokens[i] + " " + slashTokens[i].substring(matcher.start());
					}
				} else if (i == (plusTokens.length - 1)) {
					// last
					int indexOf = plusTokens[i].indexOf('/');
					if (indexOf > -1) {
						substance = plusTokens[i].substring(0, indexOf);
						matcher = REGEX_DIGIT.matcher(substance);
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
		} // else
		if (plusCount == 0
				&& slashCount > 0) {
			return componentTerm.split("/");
		} // else
		if (plusCount > 0) {
			return componentTerm.split("\\+");
		} // else
		return new String[] { componentTerm };
	}

	/**
	 * Attempt to generate {@link Pharmaceutical} representation based on the SNOMED CT term, if successful match against {@link MatchTermRule}s:
	 * <ul>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_ENGLISH_DOSE_FORM}</li>
	 * <li>{@link MatchTermRule#GENERIC_PARTIAL_ENGLISH_DOSE_FORM}</li>
	 * <li>{@link MatchTermRule#GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH}</li>
	 * <li>{@link MatchTermRule#GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH}</li>
	 * </ul>
	 * @param matchAttributeRule
	 * @param term prospect
	 * @param pharmaceutical reference
	 * @return {@link MatchTermRule} applicable to term or <code>null</code>
	 * @see #getTermPharmaceutical(String, boolean, Pharmaceutical)
	 */
	public static MatchTermRule getMatchPharmaceuticalRuleEnglish(final MatchAttributeRule matchAttributeRule,
			final String term,
			final Pharmaceutical pharmaceutical) {
		Pharmaceutical termPharmaceutical = MatchTermHelper.getTermPharmaceutical(term,
				true, // isEnglish
				pharmaceutical);
		if (pharmaceutical.components.size() == termPharmaceutical.components.size()) {
			Component component,
				termComponent;
			boolean unexpectedComponentOrder = false,
				componentPresent;
			for (int i = 0; i < pharmaceutical.components.size(); i++) {
				component = pharmaceutical.components.get(i);
				termComponent = termPharmaceutical.components.get(i);
				if (!component.substance.getNormalizedNameEnglish().equalsIgnoreCase(termComponent.substance.nameEnglish)
						|| !Component.getStrengthEnglish(component.strength).equalsIgnoreCase(termComponent.strength)
						|| !component.unit.equalsIgnoreCase(termComponent.unit)) {
					componentPresent = false;
					for (Component tc : termPharmaceutical.components) {
						if (component.substance.getNormalizedNameEnglish().equalsIgnoreCase(tc.substance.nameEnglish)
								&& Component.getStrengthEnglish(component.strength).equalsIgnoreCase(tc.strength)
								&& component.unit.equalsIgnoreCase(tc.unit)) {
							componentPresent = true;
							break;
						}
					}
					if (componentPresent) {
						unexpectedComponentOrder = true;
					}
				}
			}
			// dose form
			if (MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM.equals(matchAttributeRule)
					|| MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM.equals(matchAttributeRule)) {
				if (termPharmaceutical.doseForm.nameEnglish == null) {
					return MatchTermRule.GENERIC_MISSING_ENGLISH_DOSE_FORM;
				} // else
				if (!pharmaceutical.doseForm.getNormalizedNameEnglish().equalsIgnoreCase(termPharmaceutical.doseForm.nameEnglish)) {
					return MatchTermRule.GENERIC_PARTIAL_ENGLISH_DOSE_FORM;
				} // else
			}
			if (unexpectedComponentOrder) {
				return MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH;
			} // else
			return MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH;
		} // else
		// unable to extract expected number of components from term
		return null;
	}

	/**
	 * Attempt to generate {@link Pharmaceutical} representation based on the SNOMED CT term, if successful match against {@link MatchTermRule}s:
	 * <ul>
	 * <li>{@link MatchTermRule#PHARMACEUTICAL_EXACT_NATIONAL_MATCH}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_NATIONAL_DOSE_FORM}</li>
	 * <li>{@link MatchTermRule#GENERIC_PARTIAL_NATIONAL_DOSE_FORM}</li>
	 * <li>{@link MatchTermRule#GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL}</li>
	 * <li>{@link MatchTermRule#PHARMACEUTICAL_INCORRECT_COMPONENT_ORDER_NATIONAL}</li>
	 * <li>{@link MatchTermRule#GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH}</li>
	 * <li>{@link MatchTermRule#PHARMACEUTICAL_CASE_INSENSITIVE_NATIONAL_MATCH}</li>
	 * <li>{@link MatchTermRule#PHARMACEUTICAL_PARTIAL_TRADE_NAME_NATIONAL}</li>
	 * </ul>
	 * @param matchAttributeRule
	 * @param term prospect
	 * @param pharmaceutical reference
	 * @return {@link MatchTermRule} applicable to term or <code>null</code>
	 * @see #getTermPharmaceutical(String, boolean, Pharmaceutical)
	 */
	public static MatchTermRule getMatchPharmaceuticalRuleNational(final MatchAttributeRule matchAttributeRule,
			final String term,
			final Pharmaceutical pharmaceutical) {
		if (pharmaceutical.getNationalPharmaceuticalTerm().equals(term)) {
			return MatchTermRule.PHARMACEUTICAL_EXACT_NATIONAL_MATCH;
		} // else
		Pharmaceutical termPharmaceutical = MatchTermHelper.getTermPharmaceutical(term,
				false, // isEnglish
				pharmaceutical);
		if (pharmaceutical.components.size() == termPharmaceutical.components.size()) {
			Component component,
				termComponent;
			boolean unexpectedComponentOrder = false,
				componentPresent;
			for (int i = 0; i < pharmaceutical.components.size(); i++) {
				component = pharmaceutical.components.get(i);
				termComponent = termPharmaceutical.components.get(i);
				// substance
				if (!component.substance.getNormalizedNameNational().equalsIgnoreCase(termComponent.substance.nameNational)
						|| !component.strength.equalsIgnoreCase(termComponent.strength)
						|| !component.unit.equalsIgnoreCase(termComponent.unit)) {
					componentPresent = false;
					for (Component tc : termPharmaceutical.components) {
						if (component.substance.getNormalizedNameNational().equalsIgnoreCase(tc.substance.nameNational)
								&& component.strength.equalsIgnoreCase(tc.strength)
								&& component.unit.equalsIgnoreCase(tc.unit)) {
							componentPresent = true;
							break;
						}
					}
					if (componentPresent) {
						unexpectedComponentOrder = true;
					}
				}
			}
			// dose form
			if (MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM.equals(matchAttributeRule)
					|| MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM.equals(matchAttributeRule)) {
				if (termPharmaceutical.doseForm.nameNational == null) {
					return MatchTermRule.GENERIC_MISSING_NATIONAL_DOSE_FORM;
				} // else
				if (!pharmaceutical.doseForm.getNormalizedNameNational().equalsIgnoreCase(termPharmaceutical.doseForm.nameNational)) {
					return MatchTermRule.GENERIC_PARTIAL_NATIONAL_DOSE_FORM;
				} // else
			}
			if (unexpectedComponentOrder) {
				if (termPharmaceutical.tradeName == null) {
					return MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL;
				} // else
				return MatchTermRule.PHARMACEUTICAL_INCORRECT_COMPONENT_ORDER_NATIONAL;
			} // else
			if (termPharmaceutical.tradeName == null) {
				return MatchTermRule.GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH;
			} // else
			if (pharmaceutical.getNormalizedTradeName().equalsIgnoreCase(termPharmaceutical.tradeName)) {
				return MatchTermRule.PHARMACEUTICAL_CASE_INSENSITIVE_NATIONAL_MATCH;
			} // else
			return MatchTermRule.PHARMACEUTICAL_PARTIAL_TRADE_NAME_NATIONAL;
		} // else
		// unable to extract expected number of components from term
		return null;
	}

	/**
	 * "Broad" text matching, WARNING doesn't take term component order into consideration!
	 * <ul>
	 * <li>{@link MatchTermRule#GENERIC_EXACT_ENGLISH_MATCH}</li>
	 * <li>{@link MatchTermRule#GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_ENGLISH_SUBSTANCE}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_ENGLISH_STRENGTH}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_ENGLISH_UNIT}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_ENGLISH_DOSE_FORM}</li>
	 * <li>{@link MatchTermRule#GENERIC_PARTIAL_ENGLISH_DOSE_FORM}</li>
	 * <li>{@link MatchTermRule#GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH}</li>
	 * </ul>
	 * @param matchAttributeRule
	 * @param term prospect
	 * @param pharmaceutical reference
	 * @return {@link MatchTermRule} applicable to term or <code>null</code>
	 */
	public static MatchTermRule getMatchTermRuleEnglish(final MatchAttributeRule matchAttributeRule,
			final String term,
			final Pharmaceutical pharmaceutical) {
		String englishTerm = pharmaceutical.getEnglishTerm();
		if (term.equals(englishTerm)) {
			return MatchTermRule.GENERIC_EXACT_ENGLISH_MATCH;
		} // else
		if (term.equalsIgnoreCase(englishTerm)) {
			return MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH;
		} // else
		boolean strengthPresent,
			unitPresent;
		String[] termTokens = term.split(" ");
		for (Component component : pharmaceutical.components) {
			if (!term.contains(component.getEnglish())) {
				// substance
				if (!term.toLowerCase(Locale.ENGLISH).contains(component.substance.getNormalizedNameEnglish().toLowerCase(Locale.ENGLISH))) {
					return MatchTermRule.GENERIC_MISSING_ENGLISH_SUBSTANCE;
				} // else
				// strength & unit
				strengthPresent = false;
				unitPresent = false;
				for (String termToken : termTokens) {
					if (Component.getStrengthEnglish(component.strength).equalsIgnoreCase(termToken.trim())) {
						strengthPresent = true;
					} else if (component.unit.equalsIgnoreCase(termToken.trim())) {
						unitPresent = true;
					} else if (termToken.toLowerCase(Locale.ENGLISH).contains(Component.getStrengthEnglish(component.strength).toLowerCase(Locale.ENGLISH) + component.unit.toLowerCase(Locale.ENGLISH))) {
						strengthPresent = true;
						unitPresent = true;
					}
					if (strengthPresent
							&& unitPresent) {
						break;
					}
				}
				if (!strengthPresent) {
					return MatchTermRule.GENERIC_MISSING_ENGLISH_STRENGTH;
				} // else
				if (!unitPresent) {
					return MatchTermRule.GENERIC_MISSING_ENGLISH_UNIT;
				} // else
			}
		}
		// dose form
		if (MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM.equals(matchAttributeRule)
				|| MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM.equals(matchAttributeRule)) {
			String doseForm = getTermDoseForm(term,
					pharmaceutical.components,
					pharmaceutical.doseForm.getNormalizedNameEnglish());
			if (doseForm == null) {
				return MatchTermRule.GENERIC_MISSING_ENGLISH_DOSE_FORM;
			} // else
			if (!pharmaceutical.doseForm.getNormalizedNameEnglish().equalsIgnoreCase(doseForm)) {
				return MatchTermRule.GENERIC_PARTIAL_ENGLISH_DOSE_FORM;
			} // else
		}
		return MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH;
	}

	/**
	 * "Broad" text matching, WARNING doesn't take term component order into consideration!
	 * <ul>
	 * <li>{@link MatchTermRule#GENERIC_EXACT_NATIONAL_MATCH}</li>
	 * <li>{@link MatchTermRule#GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_NATIONAL_SUBSTANCE}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_NATIONAL_STRENGTH}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_NATIONAL_UNIT}</li>
	 * <li>{@link MatchTermRule#GENERIC_MISSING_NATIONAL_DOSE_FORM}</li>
	 * <li>{@link MatchTermRule#GENERIC_PARTIAL_NATIONAL_DOSE_FORM}</li>
	 * <li>{@link MatchTermRule#GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL}</li>
	 * </ul>
	 * @param matchAttributeRule
	 * @param term prospect
	 * @param pharmaceutical reference
	 * @return {@link MatchTermRule} applicable to term or <code>null</code>
	 */
	public static MatchTermRule getMatchTermRuleNational(final MatchAttributeRule matchAttributeRule,
			final String term,
			final Pharmaceutical pharmaceutical) {
		String nationalTerm = pharmaceutical.getNationalTerm();
		if (term.equals(nationalTerm)) {
			return MatchTermRule.GENERIC_EXACT_NATIONAL_MATCH;
		} // else
		if (term.equalsIgnoreCase(nationalTerm)) {
			return MatchTermRule.GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH;
		} // else
		boolean strengthPresent,
			unitPresent;
		String[] termTokens = term.split(" ");
		for (Component component : pharmaceutical.components) {
			if (!term.contains(component.getNational())) {
				// substance
				if (!term.toLowerCase(Locale.ENGLISH).contains(component.substance.getNormalizedNameNational().toLowerCase(Locale.ENGLISH))) {
					return MatchTermRule.GENERIC_MISSING_NATIONAL_SUBSTANCE;
				} // else
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
					if (strengthPresent
							&& unitPresent) {
						break;
					}
				}
				if (!strengthPresent) {
					return MatchTermRule.GENERIC_MISSING_NATIONAL_STRENGTH;
				} // else
				if (!unitPresent) {
					return MatchTermRule.GENERIC_MISSING_NATIONAL_UNIT;
				} // else
			}
		}
		// dose form
		if (MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM.equals(matchAttributeRule)
				|| MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM.equals(matchAttributeRule)) {
			String doseForm = getTermDoseForm(term,
					pharmaceutical.components,
					pharmaceutical.doseForm.getNormalizedNameNational());
			if (doseForm == null) {
				return MatchTermRule.GENERIC_MISSING_NATIONAL_DOSE_FORM;
			} // else
			if (!pharmaceutical.doseForm.getNormalizedNameNational().equalsIgnoreCase(doseForm)) {
				return MatchTermRule.GENERIC_PARTIAL_NATIONAL_DOSE_FORM;
			} // else
		}
		return MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL;
	}

	/**
	 * Attempt to extract dose form from the given term, returning the first match following this approach:
	 * <ul>
	 * <li>ends with expectedDoseForm (case insensitive)</li>
	 * <li>tokenize expectedDoseForm, and attempt to match each token from the back of the term (case insensitive)</li>
	 * <li>locate the last unit, and return the trailing value (case insensitive)</li>
	 * </ul>
	 * @param term assumed to contain a desired dose form (normalized whitespace formatting is assumed)
	 * @param expectedComponents
	 * @param expectedDoseForm (normalized whitespace formatting is assumed)
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
			String[] expectedDoseFormTokens = expectedDoseForm.split(" ");
			if (expectedDoseFormTokens.length > 1) {
				String[] termTokens = term.split(" ");
				if (termTokens.length >= expectedDoseFormTokens.length) {
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
		return (doseForm == null) ? null : StringUtils.normalizeSpace(doseForm);
	}

	/**
	 * Attempt to generate {@link Pharmaceutical} representation based on a SNOMED CT term, that matches the given reference {@link Pharmaceutical}, following this approach:
	 * <ul>
	 * <li>attempt to extract (partial) trade name from national term, otherwise leave empty</li>
	 * <li>attempt to extract (partial) dose form from term, see also {@link MatchTermHelper#getTermDoseForm(String, List, String)}</li>
	 * <li>attempt to extract {@link Component} tokens from term, see also {@link MatchTermHelper#getComponentTermTokens(String)}</li>
	 * <li>for each expected {@link Component} match substance (case insensitive), strength & unit.
	 * If matched, add to {@link Component}s and include in returned {@link Pharmaceutical}
	 * </li>
	 * </ul>
	 * <p>
	 * NOTE, whitespace is normalized in-order to provide a predictable comparable result.
	 * @param normalizedTerm (normalized whitespace formatting is assumed)
	 * @param isEnglish
	 * @param expectedPharmaceutical
	 * @return {@link Pharmaceutical} based on the given term, excluding drugId
	 */
	public static Pharmaceutical getTermPharmaceutical(final String normalizedTerm,
			final boolean isEnglish,
			final Pharmaceutical expectedPharmaceutical) {
		String term = normalizedTerm;
		int indexOf;
		// extract & remove leading national trade name (case-insensitive)
		String tradeName = null;
		if (!isEnglish) {
			String normalizedExpectedTradeName = expectedPharmaceutical.getNormalizedTradeName();
			indexOf = term.toLowerCase(Locale.ENGLISH).indexOf(normalizedExpectedTradeName.toLowerCase(Locale.ENGLISH));
			if (indexOf > -1) {
				tradeName = term.substring(indexOf, normalizedExpectedTradeName.length());
			} else {
				// match one trade name token at a time
				String[] expectedTradeNameTokens = normalizedExpectedTradeName.split(" ");
				if (expectedTradeNameTokens.length > 1) {
					String[] termTokens = term.split(" ");
					if (termTokens.length >= expectedTradeNameTokens.length) {
						StringBuilder tnBuilder = new StringBuilder();
						for (int i = 0; i < expectedTradeNameTokens.length; i++) {
							if (expectedTradeNameTokens[i].equalsIgnoreCase(termTokens[i])) {
								tnBuilder.append(termTokens[i])
									.append(" ");
							} else {
								break;
							}
						}
						if (tnBuilder.length() > 0) {
							tradeName = tnBuilder.toString().trim();
						}
					}
				}
			}
			if (tradeName != null) {
				term = term.substring(tradeName.length()).trim();
			}
		}
		// extract & remove dose form (case-insensitive)
		String doseForm = getTermDoseForm(term,
				expectedPharmaceutical.components,
				(isEnglish) ? expectedPharmaceutical.doseForm.getNormalizedNameEnglish() : expectedPharmaceutical.doseForm.getNormalizedNameNational());
		if (doseForm != null) {
			term = term.substring(0,
					term.lastIndexOf(doseForm)).trim();
		}
		// extract components
		String componentTermToken,
			expectedStrength,
			expectedSubstanceName,
			substanceName,
			strength,
			unit;
		List<Component> components = new ArrayList<>(expectedPharmaceutical.components.size());
		String[] componentTermTokens = getComponentTermTokens(term);
		for (int i = 0; i < componentTermTokens.length; i++) {
			componentTermToken = componentTermTokens[i];
			if (componentTermToken != null) {
				for (Component component : expectedPharmaceutical.components) {
					expectedStrength = (isEnglish) ? Component.getStrengthEnglish(component.strength) : component.strength;
					expectedSubstanceName = ((isEnglish) ? component.substance.getNormalizedNameEnglish() : component.substance.getNormalizedNameNational()).toLowerCase(Locale.ENGLISH);
					indexOf = componentTermToken.toLowerCase(Locale.ENGLISH).indexOf(expectedSubstanceName);
					substanceName = (indexOf > -1) ? componentTermToken.substring(indexOf, (indexOf + expectedSubstanceName.length())) : null;
					strength = null;
					unit = null;
					for (String token : componentTermToken.split(" ")) {
						if (expectedStrength.equalsIgnoreCase(token)) {
							strength = token;
						} else if (component.unit.equalsIgnoreCase(token)) {
							unit = token;
						} else {
							if (token.equalsIgnoreCase(expectedStrength + component.unit)) {
								indexOf = token.toLowerCase(Locale.ENGLISH).indexOf(expectedStrength.toLowerCase(Locale.ENGLISH));
								strength = (indexOf > -1) ? token.substring(indexOf, (indexOf + expectedStrength.length())) : null;
								indexOf = token.toLowerCase(Locale.ENGLISH).indexOf(component.unit.toLowerCase(Locale.ENGLISH));
								unit = (indexOf > -1) ? token.substring(indexOf, (indexOf + component.unit.length())) : null;
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
				tradeName); // tradeName
	}
}
