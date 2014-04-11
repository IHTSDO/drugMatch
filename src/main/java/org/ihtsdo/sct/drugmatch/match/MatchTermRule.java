package org.ihtsdo.sct.drugmatch.match;

/**
 * @author dev-team@carecom.dk
 * 
 */
public enum MatchTermRule {

	EXACT_NATIONAL_MATCH(4),

	EXACT_ENGLISH_MATCH(3),


	CASE_INSENSITIVE_NATIONAL_MATCH(2),

	CASE_INSENSITIVE_ENGLISH_MATCH(1),


	INCORRECT_COMPONENT_ORDER_NATIONAL(0),

	MISSING_NATIONAL_SUBSTANCE(-1),

	MISSING_NATIONAL_DOSE_FORM(-2),

	MISSING_NATIONAL_UNIT(-3),

	MISSING_NATIONAL_STRENGTH(-4),


	INCORRECT_COMPONENT_ORDER_ENGLISH(-9),

	MISSING_ENGLISH_SUBSTANCE(-10),

	MISSING_ENGLISH_DOSE_FORM(-20),

	MISSING_ENGLISH_UNIT(-30),

	MISSING_ENGLISH_STRENGTH(-40),


	AMBIGUOUS_MATCH(-50),

	ZERO_TERM_MATCH(-100),

	ZERO_ATTRIBUTE_MATCH(-101);

	/**
	 * Rule weight, the higher the weight the more precise term match
	 */
	private final int weight;

	/**
	 * 
	 */
	private MatchTermRule(int weight) {
		this.weight = weight;
	}

	/**
	 * @return the weight
	 */
	public int getWeight() {
		return this.weight;
	}
}
