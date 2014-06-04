package org.ihtsdo.sct.drugmatch.constant.rf1;

/**
 * SNOMED CT Release Format 1 Description types.
 * @author dev-team@carecom.dk
 */
public enum DescriptionType {

	UNSPECIFIED(0),

	PREFERRED_TERM(1),

	SYNONYM(2),

	FULLY_SPECIFIED_NAME(3);

	private final int id;

	/**
	 * Default constructor.
	 * @param id ID
	 */
	private DescriptionType(final int id) {
		this.id = id;
	}

	/**
	 * @return numeric value
	 */
	public int getId() {
		return this.id;
	}
}
