package org.ihtsdo.sct.drugmatch.check;

import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;

/**
 * Generic "Check" validation
 * 
 * @author dev-team@carecom.dk
 *
 */
public class CheckValidationImpl implements CheckValidation {

	public CheckRule getRule(String componentName,
			ConceptSearchResultDescriptor conceptSearchResultDescriptor) {
		if (componentName.equals(conceptSearchResultDescriptor.descriptionTerm)) {
			return CheckRule.EXACT_MATCH;
		} // else
		return CheckRule.CASE_INSENSITIVE_MATCH;
	}

	public String getCheckRuleViolationMessage(CheckRule checkRule) {
		return checkRule.toString();
	}
}
