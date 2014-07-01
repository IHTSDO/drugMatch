package org.ihtsdo.sct.drugmatch.match;


/**
 * @author dev-team@carecom.dk
 */
public interface MatchRuleHelper {

	/**
	 * @param matchTermRule
	 * @return (ambiguous) custom message if available, otherwise "AMBIGUOUS_" + toString() from argument.
	 */
	String getAmbiguousMessage(MatchTermRule matchTermRule);

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
