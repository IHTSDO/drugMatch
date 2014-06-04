package org.ihtsdo.sct.drugmatch.check;


/**
 * Generic "Check" validation.
 * @author dev-team@carecom.dk
 */
public class CheckValidationImpl implements CheckValidation {

	/**
	 * {@inheritDoc}
	 */
	public final String getMessage(final CheckRule checkRule) {
		return checkRule.toString();
	}
}
