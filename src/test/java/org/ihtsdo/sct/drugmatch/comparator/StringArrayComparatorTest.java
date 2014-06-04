package org.ihtsdo.sct.drugmatch.comparator;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class StringArrayComparatorTest {

	@Test
	public final void compare() {
		StringArrayComparator comparator = new StringArrayComparator();
		// size
		Assert.assertEquals(-1,
				comparator.compare(new String[0],
							new String[] { "value" }));
		Assert.assertEquals(1,
				comparator.compare(new String[] { "value" },
							new String[0]));
		// content
		Assert.assertEquals(0,
				comparator.compare(new String[0],
							new String[0]));
		Assert.assertEquals(0,
				comparator.compare(new String[] { null },
							new String[] { null }));
		Assert.assertEquals(0,
				comparator.compare(new String[] { "value" },
							new String[] { "value" }));
		Assert.assertEquals(1,
				comparator.compare(new String[] { null },
							new String[] { "value" }));
		Assert.assertEquals(-1,
				comparator.compare(new String[] { "value" },
							new String[] { null }));
	}
}
