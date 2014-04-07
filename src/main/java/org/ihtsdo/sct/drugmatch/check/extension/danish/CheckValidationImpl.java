package org.ihtsdo.sct.drugmatch.check.extension.danish;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ihtsdo.sct.drugmatch.check.CheckRule;
import org.ihtsdo.sct.drugmatch.check.CheckValidation;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;

/**
 * Danish "Check" validation
 * <p>
 * "Rules for check of:"
 * <ul>
 * <li>Dose form</li>
 * <li>Substance</li>
 * <li>Unit</li>
 * </ul>
 * <p>
 * 
 * @author dev-team@carecom.dk
 * @see "Rules for substances dose form units and product generics.docx"
 * @see mail "Checks, Kell Greibe [keg@ssi.dk], Thursday, April 03, 2014 13:32"
 */
public class CheckValidationImpl implements CheckValidation {

	/**
	 * Mapping between generic Check and Danish Check violation messages.
	 * 
	 * @see "Rules for substances dose form units and product generics.docx"
	 */
	private static final Map<CheckRule, String> checkRule2ViolationMessage;

	static {
		Map<CheckRule, String> tmpResult = new HashMap<>();
		tmpResult.put(CheckRule.CASE_INSENSITIVE_MATCH, "Case error");
		tmpResult.put(CheckRule.CONCATENATION_MATCH, "Concatenation error");
		tmpResult.put(CheckRule.EXACT_MATCH, "Exact");
		tmpResult.put(CheckRule.INFLECTION_MATCH, "Inflection error");
		tmpResult.put(CheckRule.TRANSLATION_MISSING, "Translation missing");
		tmpResult.put(CheckRule.ZERO_MATCH, "Missing");
		checkRule2ViolationMessage = Collections.unmodifiableMap(tmpResult);
	}

	public CheckRule getRule(String componentName,
			List<ConceptSearchResultDescriptor> matchDescriptors) {
		String term = matchDescriptors.iterator().next().descriptionTerm;
		if (term.toLowerCase().equals(term)) {
			return CheckRule.EXACT_MATCH;
		} // else
		return CheckRule.CASE_INSENSITIVE_MATCH;
	}

	public String getCheckRuleViolationMessage(CheckRule checkRule) {
		String violationMessage = checkRule2ViolationMessage.get(checkRule);
		return (violationMessage == null) ? checkRule.toString() : violationMessage;
	}
}
