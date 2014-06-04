package org.ihtsdo.sct.drugmatch.match.extension.danish;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ihtsdo.sct.drugmatch.match.MatchAttributeRule;
import org.ihtsdo.sct.drugmatch.match.MatchRuleHelper;
import org.ihtsdo.sct.drugmatch.match.MatchTermRule;

/**
 * @author dev-team@carecom.dk
 */
public class MatchRuleHelperDanishImpl implements MatchRuleHelper {

	/**
	 * Mapping between generic {@link MatchAttributeRule} and Danish Match violation messages.
	 * @see "Rules for substances dose form units and product generics.docx"
	 */
	private static final Map<MatchAttributeRule, String> MATCH_ATTRIBUTE_RULE_2_VIOLATION_MESSAGE;

	/**
	 * Mapping between generic {@link MatchTermRule} and Danish Match violation messages.
	 * @see "Rules for substances dose form units and product generics.docx"
	 */
	private static final Map<MatchTermRule, String> MATCH_TERM_RULE_2_VIOLATION_MESSAGE;

	static {
		Map<MatchAttributeRule, String> tmpAttributeRuleResult = new HashMap<>();

		tmpAttributeRuleResult.put(MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM_AND_UNIT, "Product with substances found");

		tmpAttributeRuleResult.put(MatchAttributeRule.EXACT_MATCH_EXCLUDING_UNIT, "Product with substances and dose form found");

		MATCH_ATTRIBUTE_RULE_2_VIOLATION_MESSAGE = Collections.unmodifiableMap(tmpAttributeRuleResult);


		Map<MatchTermRule, String> tmpTermRuleResult = new HashMap<>();
		tmpTermRuleResult.put(MatchTermRule.GENERIC_EXACT_NATIONAL_MATCH, "Exact");

		tmpTermRuleResult.put(MatchTermRule.GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH, "Product – all attributes found – Translation error");
		tmpTermRuleResult.put(MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL, "Product – all attributes found – Translation error");

		tmpTermRuleResult.put(MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH, "Product – all attributes found - Translation missing");
		tmpTermRuleResult.put(MatchTermRule.GENERIC_EXACT_ENGLISH_MATCH, "Product – all attributes found - Translation missing");
		tmpTermRuleResult.put(MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH, "Product – all attributes found - Translation missing");

		tmpTermRuleResult.put(MatchTermRule.ZERO_TERM_MATCH, "Product missing");

		MATCH_TERM_RULE_2_VIOLATION_MESSAGE = Collections.unmodifiableMap(tmpTermRuleResult);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getMessage(final MatchAttributeRule matchAttributeRule) {
		String violationMessage = MATCH_ATTRIBUTE_RULE_2_VIOLATION_MESSAGE.get(matchAttributeRule);
		return (violationMessage == null) ? "ATTRIBUTE_" + matchAttributeRule.toString() : violationMessage;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getMessage(final MatchTermRule matchTermRule) {
		String violationMessage = MATCH_TERM_RULE_2_VIOLATION_MESSAGE.get(matchTermRule);
		return (violationMessage == null) ? matchTermRule.toString() : violationMessage;
	}
}
