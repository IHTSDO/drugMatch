package org.ihtsdo.sct.drugmatch.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class ComponentTest {

	private static final Component COMPONENT = new Component("substanceNameEnglish",
			"substanceNameNational",
			"strength",
			"unit");

	@Test
	public final void getEnglish() {
		Assert.assertEquals("substanceNameEnglish strength unit",
				COMPONENT.getEnglish());
	}

	@Test
	public final void getNational() {
		Assert.assertEquals("substanceNameNational strength unit",
				COMPONENT.getNational());
	}
}
