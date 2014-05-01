package org.ihtsdo.sct.drugmatch.check;

import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;

/**
 * Generic "Check" validation.
 * @author dev-team@carecom.dk
 */
public class CheckValidationImpl implements CheckValidation {

	/**
	 * {@inheritDoc}
	 */
	public final CheckRule getRule(final String componentName,
			final ConceptSearchResultDescriptor conceptSearchResultDescriptor) {
		if (componentName.equals(conceptSearchResultDescriptor.descriptionTerm)) {
			return CheckRule.EXACT_MATCH;
		} // else
		return CheckRule.CASE_INSENSITIVE_MATCH;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getMessage(final CheckRule checkRule) {
		return checkRule.toString();
	}
}
