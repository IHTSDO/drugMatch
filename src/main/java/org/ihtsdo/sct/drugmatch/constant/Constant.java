package org.ihtsdo.sct.drugmatch.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ihtsdo.sct.drugmatch.comparator.MatchTermRuleWeightComparator;
import org.ihtsdo.sct.drugmatch.match.MatchTermRule;

/**
 * DrugMatch constants.
 * @author dev-team@carecom.dk
 */
public final class Constant {

	/**
	 * DON'T INSTANTIATE A STATIC HELPER!
	 */
	private Constant() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Matches (repeated) whitespace.
	 */
	public static final String REGEX_WHITESPACE_GREEDY = "\\s+";

	/**
	 * All {@link MatchTermRule}s ordered by {@link MatchTermRule#getWeight()} descending.
	 */
	public static final List<MatchTermRule> WEIGHTED_RULES;

	static {
		List<MatchTermRule> rules = new ArrayList<>(Arrays.asList(MatchTermRule.values()));
		Collections.sort(rules, new MatchTermRuleWeightComparator());
		WEIGHTED_RULES = Collections.unmodifiableList(rules);
	}
}
