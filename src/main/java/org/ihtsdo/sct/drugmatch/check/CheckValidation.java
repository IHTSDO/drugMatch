package org.ihtsdo.sct.drugmatch.check;

import java.util.List;

import org.ihtsdo.sct.drugmatch.check.CheckRule;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;

/**
 * @author dev-team@carecom.dk
 *
 */
public interface CheckValidation {

	/**
	 * Precondition; matchDescriptors contains 1 element, and 1 element only!
	 * 
	 * @param componentName
	 * @param matchDescriptors
	 * @return {@link CheckRule}
	 */
	CheckRule getRule(String componentName,
			List<ConceptSearchResultDescriptor> matchDescriptors);

	/**
	 * @param checkRule
	 * @return custom violation message if available, otherwise toString from argument.
	 */
	String getCheckRuleViolationMessage(CheckRule checkRule);
}
