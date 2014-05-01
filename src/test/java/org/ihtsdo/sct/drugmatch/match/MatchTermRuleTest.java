package org.ihtsdo.sct.drugmatch.match;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class MatchTermRuleTest {

	@Test
	public final void uniqueWeights() {
		SortedMap<Integer, SortedSet<MatchTermRule>> weight2Rules = new TreeMap<>();
		SortedSet<MatchTermRule> rules;
		for (MatchTermRule rule : MatchTermRule.values()) {
			rules = weight2Rules.get(rule.getWeight());
			if (rules == null) {
				rules = new TreeSet<>();
				weight2Rules.put(rule.getWeight(), rules);
			}
			rules.add(rule);
		}
		if (weight2Rules.size() != MatchTermRule.values().length) {
			StringBuilder msg = new StringBuilder("Unexpected duplicated weight(s): ");
			for (SortedSet<MatchTermRule> matchTermRules : weight2Rules.values()) {
				if (matchTermRules.size() > 1) {
					msg.append(matchTermRules.toString());
					msg.append(", ");
				}
			}
			Assert.fail(msg.substring(0, msg.lastIndexOf(",")));
		}
	}
}
