package org.ihtsdo.sct.drugmatch.check;

import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;

/**
 * "Check" validation.
 * @author dev-team@carecom.dk
 */
public interface CheckValidation {

	/**
	 * @param componentName
	 * @param conceptSearchResultDescriptor
	 * @return {@link CheckRule}
	 */
	CheckRule getRule(String componentName,
			ConceptSearchResultDescriptor conceptSearchResultDescriptor);

	/**
	 * @param checkRule
	 * @return custom message if available, otherwise toString from argument.
	 */
	String getMessage(CheckRule checkRule);
}
