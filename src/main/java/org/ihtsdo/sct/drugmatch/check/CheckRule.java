package org.ihtsdo.sct.drugmatch.check;

/**
 * "Check" rules.
 * @author dev-team@carecom.dk
 */
public enum CheckRule {

	AMBIGUOUS_MATCH,

	CASE_INSENSITIVE_MATCH,

	COMPONENT_AND_TERM_MISMATCH,

	EXACT_MATCH,

	TRANSLATION_MISSING,

	UNCHECKED,

	ZERO_MATCH;
}
