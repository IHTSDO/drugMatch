package org.ihtsdo.sct.drugmatch.match;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author dev-team@carecom.dk
 */
public class MatchTermRuleWeightComparator implements Comparator<MatchTermRule>, Serializable {

	/**
	 * Order by weight descending.
	 * @param o1
	 * @param o2
	 * @return o2.weight - o1.weight
	 */
	public final int compare(final MatchTermRule o1,
			final MatchTermRule o2) {
		return o2.getWeight() - o1.getWeight();
	}
}
