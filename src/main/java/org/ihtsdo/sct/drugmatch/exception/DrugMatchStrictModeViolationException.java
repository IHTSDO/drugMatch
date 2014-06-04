package org.ihtsdo.sct.drugmatch.exception;

/**
 * @author dev-team@carecom.dk
 */
public class DrugMatchStrictModeViolationException extends Exception {

	/**
	 * @param message
	 */
	public DrugMatchStrictModeViolationException(final String message) {
		super(message);
	}
}
