package org.ihtsdo.sct.drugmatch.match;

/**
 * "Match" term rules.
 * @author dev-team@carecom.dk
 */
public enum MatchTermRule {

	PHARMACEUTICAL_EXACT_NATIONAL_MATCH(15, false),

	PHARMACEUTICAL_CASE_INSENSITIVE_NATIONAL_MATCH(10, false),

	PHARMACEUTICAL_INCORRECT_COMPONENT_ORDER_NATIONAL(5, false),


	GENERIC_EXACT_NATIONAL_MATCH(4, true),

	GENERIC_EXACT_ENGLISH_MATCH(3, true),


	GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH(2, true),

	GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH(1, true),


	GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL(0, true),


	PHARMACEUTICAL_PARTIAL_TRADE_NAME_NATIONAL(-1, false),


	GENERIC_PARTIAL_NATIONAL_DOSE_FORM(-3, true),

	GENERIC_MISSING_NATIONAL_SUBSTANCE(-5, true),

	GENERIC_MISSING_NATIONAL_DOSE_FORM(-10, true),

	GENERIC_MISSING_NATIONAL_UNIT(-15, true),

	GENERIC_MISSING_NATIONAL_STRENGTH(-20, true),


	GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH(-25, true),

	GENERIC_PARTIAL_ENGLISH_DOSE_FORM(-30, true),

	GENERIC_MISSING_ENGLISH_SUBSTANCE(-35, true),

	GENERIC_MISSING_ENGLISH_DOSE_FORM(-40, true),

	GENERIC_MISSING_ENGLISH_UNIT(-45, true),

	GENERIC_MISSING_ENGLISH_STRENGTH(-50, true),


	ZERO_TERM_MATCH(-100, true),

	ZERO_ATTRIBUTE_MATCH(-200, true);

	/**
	 * Rule type
	 */
	private final boolean isGeneric;

	/**
	 * Rule weight, the higher the weight the more precise term match.
	 */
	private final int weight;

	/**
	 * @param weight expected to be unique! (within this context)
	 */
	private MatchTermRule(final int weight,
			final boolean isGeneric) {
		this.isGeneric = isGeneric;
		this.weight = weight;
	}

	/**
	 * @return rule type
	 */
	public boolean isGeneric() {
		return this.isGeneric;
	}

	/**
	 * @return the weight
	 */
	public int getWeight() {
		return this.weight;
	}
}
