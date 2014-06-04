package org.ihtsdo.sct.drugmatch.match.extension.danish;

import org.ihtsdo.sct.drugmatch.match.MatchAttributeRule;
import org.ihtsdo.sct.drugmatch.match.MatchRuleHelper;
import org.ihtsdo.sct.drugmatch.match.MatchTermRule;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class MatchRuleHelperDanishImplTest {

	private static final MatchRuleHelper MATCH_RULE_HELPER = new MatchRuleHelperDanishImpl();

	@Test
	public final void getMessage4MatchAttributeRule() {
		Assert.assertEquals("Product with substances and dose form found",
				MATCH_RULE_HELPER.getMessage(MatchAttributeRule.EXACT_MATCH_EXCLUDING_UNIT));
		Assert.assertEquals("ATTRIBUTE_DOSE_FORM_MISSING_CHECK_CONCEPT",
				MATCH_RULE_HELPER.getMessage(MatchAttributeRule.DOSE_FORM_MISSING_CHECK_CONCEPT));
	}

	@Test
	public final void getMessage4MatchTermRule() {
		Assert.assertEquals("Product – all attributes found - Translation missing",
				MATCH_RULE_HELPER.getMessage(MatchTermRule.GENERIC_EXACT_ENGLISH_MATCH));
		Assert.assertEquals("AMBIGUOUS_MATCH",
				MATCH_RULE_HELPER.getMessage(MatchTermRule.AMBIGUOUS_MATCH));
	}
}
