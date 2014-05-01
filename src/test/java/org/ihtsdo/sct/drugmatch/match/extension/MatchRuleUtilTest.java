package org.ihtsdo.sct.drugmatch.match.extension;

import org.ihtsdo.sct.drugmatch.match.MatchRuleHelper;
import org.ihtsdo.sct.drugmatch.match.MatchRuleHelperImpl;
import org.ihtsdo.sct.drugmatch.match.extension.danish.MatchRuleHelperDanishImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class MatchRuleUtilTest {

	@Test
	public final void getMatchRuleHelper() {
		// null
		MatchRuleHelper matchRuleHelper = MatchRuleUtil.getMatchRuleHelper(null);
		Assert.assertNotNull(matchRuleHelper);
		Assert.assertTrue(matchRuleHelper instanceof MatchRuleHelperImpl);
		// empty
		matchRuleHelper = MatchRuleUtil.getMatchRuleHelper("");
		Assert.assertNotNull(matchRuleHelper);
		Assert.assertTrue(matchRuleHelper instanceof MatchRuleHelperImpl);
		// Danish extension
		matchRuleHelper = MatchRuleUtil.getMatchRuleHelper("1000005");
		Assert.assertNotNull(matchRuleHelper);
		Assert.assertTrue(matchRuleHelper instanceof MatchRuleHelperDanishImpl);
	}
}
