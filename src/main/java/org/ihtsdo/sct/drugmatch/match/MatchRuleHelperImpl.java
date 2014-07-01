package org.ihtsdo.sct.drugmatch.match;

/**
 * @author dev-team@carecom.dk
 */
public class MatchRuleHelperImpl implements MatchRuleHelper {

	/**
	 * {@inheritDoc}
	 */
	public final String getAmbiguousMessage(MatchTermRule matchTermRule) {
		return "AMBIGUOUS_" + matchTermRule.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getMessage(final MatchAttributeRule matchAttributeRule) {
		return "ATTRIBUTE_" + matchAttributeRule.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getMessage(final MatchTermRule matchTermRule) {
		return matchTermRule.toString();
	}
}
