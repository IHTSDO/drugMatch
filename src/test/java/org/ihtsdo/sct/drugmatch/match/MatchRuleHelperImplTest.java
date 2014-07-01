package org.ihtsdo.sct.drugmatch.match;


import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 *
 */
public class MatchRuleHelperImplTest {

	private static final MatchRuleHelper matchRuleHelper = new MatchRuleHelperImpl();

	@Test
	public final void getAmbiguousMessage() {
		Assert.assertEquals("AMBIGUOUS_GENERIC_MISSING_NATIONAL_STRENGTH",
				matchRuleHelper.getAmbiguousMessage(MatchTermRule.GENERIC_MISSING_NATIONAL_STRENGTH));
	}

	@Test
	public final void getMessage4MatchAttributeRule() {
		Assert.assertEquals("ATTRIBUTE_AMBIGUOUS_MATCH",
				matchRuleHelper.getMessage(MatchAttributeRule.AMBIGUOUS_MATCH));
	}

	@Test
	public final void getMessage4MatchTermRule() {
		Assert.assertEquals("GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH",
				matchRuleHelper.getMessage(MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH));
	}
}
