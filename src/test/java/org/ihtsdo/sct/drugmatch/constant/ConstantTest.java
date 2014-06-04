package org.ihtsdo.sct.drugmatch.constant;

import org.ihtsdo.sct.drugmatch.match.MatchTermRule;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class ConstantTest {

	@Test
	public final void regexWhitespaceGreedy() {
		// most common whitespace in DrugMatch context
		Assert.assertEquals("token1Wtoken2Wtoken3Wtoken4",
				"token1 token2\ttoken3\ntoken4".replaceAll(Constant.REGEX_WHITESPACE_GREEDY, "W"));
		// most common (repeated) whitespace in DrugMatch context
		Assert.assertEquals("token1Wtoken2Wtoken3Wtoken4W",
				"token1  token2\t\ttoken3\n\ntoken4  ".replaceAll(Constant.REGEX_WHITESPACE_GREEDY, "W"));
	}

	@Test
	public final void weightedRules() {
		MatchTermRule previousMatchTermRule = null;
		for (MatchTermRule matchTermRule : Constant.WEIGHTED_RULES) {
			if (previousMatchTermRule == null) {
				previousMatchTermRule = matchTermRule;
			} else {
				Assert.assertTrue("previousMatchTermRule.getWeight() > matchTermRule.getWeight()",
						previousMatchTermRule.getWeight() > matchTermRule.getWeight());
				previousMatchTermRule = matchTermRule;
			}
		}
	}
}
