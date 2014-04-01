/**
 * 
 */
package org.ihtsdo.sct.drugmatch.enumeration;

/**
 * @author dev-team@carecom.dk
 *
 */
public enum ReturnCode {

	SUCCESS(0),

	GENERAL_EXCEPTION(1),

	CONFIGURATION_ERROR(2),

	INPUT_PARSE_ERROR(3);

	private final int value;

	/**
	 * default constructor
	 * 
	 * @param value value
	 */
	private ReturnCode(int value) {
		this.value = value;
	}

	/**
	 * @return numeric value
	 */
	public int getValue() {
		return this.value;
	}
}
