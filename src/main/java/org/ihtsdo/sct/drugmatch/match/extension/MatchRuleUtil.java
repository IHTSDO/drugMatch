package org.ihtsdo.sct.drugmatch.match.extension;

import org.ihtsdo.sct.drugmatch.match.MatchRuleHelper;
import org.ihtsdo.sct.drugmatch.match.MatchRuleHelperImpl;
import org.ihtsdo.sct.drugmatch.match.extension.danish.MatchRuleHelperDanishImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dev-team@carecom.dk
 */
public final class MatchRuleUtil {

	private static final Logger log = LoggerFactory.getLogger(MatchRuleUtil.class);

	private MatchRuleUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param namespaceId
	 * @return {@link MatchRuleHelper} matching the national namespace ID, otherwise the generic instance is returned.
	 */
	public static MatchRuleHelper getMatchRuleHelper(final String namespaceId) {
		MatchRuleHelper matchRuleHelper;
		if ("1000005".equals(namespaceId)) {
			// Danish
			matchRuleHelper = new MatchRuleHelperDanishImpl();
		} else {
			// generic
			matchRuleHelper = new MatchRuleHelperImpl();
		}
		log.debug("Initialized {}", matchRuleHelper.getClass().getName());
		return matchRuleHelper;
	}
}
