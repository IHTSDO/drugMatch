package org.ihtsdo.sct.drugmatch.match;

import org.ihtsdo.sct.drugmatch.enumeration.DescriptionType;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.DescriptionDescriptor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class DescriptionDescriptorTypeComparatorTest {

	private static final DescriptionDescriptor FULLY_SPECIFIED_NAME = new DescriptionDescriptor(),
			PREFERRED_TERM = new DescriptionDescriptor(),
			SYNONYM = new DescriptionDescriptor(),
			UNSPECIFIED = new DescriptionDescriptor();

	static {
		FULLY_SPECIFIED_NAME.descriptionId = Long.valueOf(1L);
		FULLY_SPECIFIED_NAME.descriptionType = Long.valueOf(DescriptionType.FULLY_SPECIFIED_NAME.getId());

		PREFERRED_TERM.descriptionId = Long.valueOf(2L);
		PREFERRED_TERM.descriptionType = Long.valueOf(DescriptionType.PREFERRED_TERM.getId());

		SYNONYM.descriptionId = Long.valueOf(3L);
		SYNONYM.descriptionType = Long.valueOf(DescriptionType.SYNONYM.getId());

		UNSPECIFIED.descriptionId = Long.valueOf(4L);
		UNSPECIFIED.descriptionType = Long.valueOf(DescriptionType.UNSPECIFIED.getId());
	}

	@Test
	public final void compareFullySpecifiedName() {
		DescriptionDescriptorTypeComparator comparator = new DescriptionDescriptorTypeComparator();

		// type (permutations)
		Assert.assertEquals(1,
				comparator.compare(FULLY_SPECIFIED_NAME,
						PREFERRED_TERM));

		Assert.assertEquals(-1,
				comparator.compare(FULLY_SPECIFIED_NAME,
						SYNONYM));

		Assert.assertEquals(-1,
				comparator.compare(FULLY_SPECIFIED_NAME,
						UNSPECIFIED));
	}

	@Test
	public final void compareId() {
		// identical ID
		Assert.assertEquals(0,
				new DescriptionDescriptorTypeComparator().compare(FULLY_SPECIFIED_NAME,
						FULLY_SPECIFIED_NAME));

		// identical type
		DescriptionDescriptor fullySpecifiedName2 = new DescriptionDescriptor();
		fullySpecifiedName2.descriptionId = Long.valueOf(5L);
		fullySpecifiedName2.descriptionType = FULLY_SPECIFIED_NAME.descriptionType;
		Assert.assertEquals(-1,
				new DescriptionDescriptorTypeComparator().compare(FULLY_SPECIFIED_NAME,
						fullySpecifiedName2));
	}

	@Test
	public final void comparePreferredTerm() {
		DescriptionDescriptorTypeComparator comparator = new DescriptionDescriptorTypeComparator();

		// type (permutations)
		Assert.assertEquals(-1,
				comparator.compare(PREFERRED_TERM,
						FULLY_SPECIFIED_NAME));

		Assert.assertEquals(-1,
				comparator.compare(PREFERRED_TERM,
						SYNONYM));

		Assert.assertEquals(-1,
				comparator.compare(PREFERRED_TERM,
						UNSPECIFIED));
	}

	@Test
	public final void compareSynonym() {
		DescriptionDescriptorTypeComparator comparator = new DescriptionDescriptorTypeComparator();

		// type (permutations)
		Assert.assertEquals(1,
				comparator.compare(SYNONYM,
						FULLY_SPECIFIED_NAME));

		Assert.assertEquals(1,
				comparator.compare(SYNONYM,
						PREFERRED_TERM));

		Assert.assertEquals(-1,
				comparator.compare(SYNONYM,
						UNSPECIFIED));
	}

	@Test
	public final void compareUnspecified() {
		DescriptionDescriptorTypeComparator comparator = new DescriptionDescriptorTypeComparator();

		// type (permutations)
		Assert.assertEquals(1,
				comparator.compare(UNSPECIFIED,
						FULLY_SPECIFIED_NAME));

		Assert.assertEquals(1,
				comparator.compare(UNSPECIFIED,
						PREFERRED_TERM));

		Assert.assertEquals(1,
				comparator.compare(UNSPECIFIED,
						SYNONYM));
	}
}
