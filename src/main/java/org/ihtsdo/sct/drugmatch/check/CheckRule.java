package org.ihtsdo.sct.drugmatch.check;

/**
 * @author dev-team@carecom.dk
 */
public enum CheckRule {

	AMBIGUOUS_MATCH,

	CASE_INSENSITIVE_MATCH,

	CONCATENATION_MATCH,

	EXACT_MATCH,

	INFLECTION_MATCH,

	TRANSLATION_MISSING,

	UNCHECKED,

	ZERO_MATCH;
}
