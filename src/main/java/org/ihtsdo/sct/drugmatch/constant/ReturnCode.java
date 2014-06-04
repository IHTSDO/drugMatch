package org.ihtsdo.sct.drugmatch.constant;

/**
 * DrugMatch return codes.
 * @author dev-team@carecom.dk
 */
public enum ReturnCode {

	SUCCESS(0),

	GENERAL_EXCEPTION(1),

	CONFIGURATION_ERROR(2),

	STRICT_MODE_VIOLATION(3);

	private final int value;

	/**
	 * Default constructor.
	 * @param value value
	 */
	private ReturnCode(final int value) {
		this.value = value;
	}

	/**
	 * @return numeric value
	 */
	public int getValue() {
		return this.value;
	}
}
