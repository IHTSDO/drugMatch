package org.ihtsdo.sct.drugmatch.match;

/**
 * "Match" attribute relationship rules.
 * @author dev-team@carecom.dk
 */
public enum MatchAttributeRule {

	AMBIGUOUS_MATCH,

	AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,

	EXACT_MATCH,

	EXACT_MATCH_EXCLUDING_DOSE_FORM,

	DOSE_FORM_MISSING_CHECK_CONCEPT,

	SUBSTANCE_MISSING_CHECK_CONCEPT,

	ZERO_MATCH;
}
