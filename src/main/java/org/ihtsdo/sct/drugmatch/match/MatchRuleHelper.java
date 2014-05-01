package org.ihtsdo.sct.drugmatch.match;


/**
 * @author dev-team@carecom.dk
 */
public interface MatchRuleHelper {

	/**
	 * @param matchAttributeRule
	 * @return custom message if available, otherwise toString() from argument.
	 */
	String getMessage(MatchAttributeRule matchAttributeRule);

	/**
	 * @param matchTermRule
	 * @return custom message if available, otherwise toString() from argument.
	 */
	String getMessage(MatchTermRule matchTermRule);
}
