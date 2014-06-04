package org.ihtsdo.sct.drugmatch.check;


/**
 * "Check" validation.
 * @author dev-team@carecom.dk
 */
public interface CheckValidation {

	/**
	 * @param checkRule
	 * @return custom message if available, otherwise toString from argument.
	 */
	String getMessage(CheckRule checkRule);
}
