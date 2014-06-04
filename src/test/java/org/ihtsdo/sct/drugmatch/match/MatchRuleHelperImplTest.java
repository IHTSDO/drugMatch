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
	public final void getMessage4MatchAttributeRule() {
		Assert.assertEquals("ATTRIBUTE_AMBIGUOUS_MATCH",
				matchRuleHelper.getMessage(MatchAttributeRule.AMBIGUOUS_MATCH));
	}

	@Test
	public final void getMessage4MatchTermRule() {
		Assert.assertEquals("AMBIGUOUS_MATCH",
				matchRuleHelper.getMessage(MatchTermRule.AMBIGUOUS_MATCH));
	}
}
