package org.ihtsdo.sct.drugmatch.match;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class MatchTermRuleWeightComparatorTest {

	@Test
	public final void compareFullySpecifiedName() {
		MatchTermRuleWeightComparator comparator = new MatchTermRuleWeightComparator();
		Assert.assertEquals(0,
				comparator.compare(MatchTermRule.EXACT_NATIONAL_MATCH,
						MatchTermRule.EXACT_NATIONAL_MATCH));
		Assert.assertEquals(-1,
				comparator.compare(MatchTermRule.EXACT_NATIONAL_MATCH,
						MatchTermRule.EXACT_ENGLISH_MATCH));
		Assert.assertEquals(1,
				comparator.compare(MatchTermRule.EXACT_ENGLISH_MATCH,
						MatchTermRule.EXACT_NATIONAL_MATCH));
	}
}
