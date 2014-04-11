package org.ihtsdo.sct.drugmatch.match;

import java.util.Comparator;

/**
 * @author dev-team@carecom.dk
 *
 */
public class MatchTermRuleWeightComparator implements Comparator<MatchTermRule> {

	public int compare(MatchTermRule o1,
			MatchTermRule o2) {
		int result = o2.getWeight() - o1.getWeight();
		if (result == 0) {
			result = o1.compareTo(o2);
		}
		return result;
	}
}
