package org.ihtsdo.sct.drugmatch.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class ComponentTest {

	private static final Component COMPONENT = new Component("substanceNameEnglish",
					"substanceNameNational",
					"0,1",
					"unit");

	@Test
	public final void getEnglish() {
		Assert.assertEquals("substanceNameEnglish 0.1 unit",
				COMPONENT.getEnglish());
	}

	@Test
	public final void getNational() {
		Assert.assertEquals("substanceNameNational 0,1 unit",
				COMPONENT.getNational());
	}

	@Test
	public final void getStrengthEnglish() {
		Assert.assertEquals("Strength Through Unity, Unity Through Faith",
				Component.getStrengthEnglish("Strength Through Unity, Unity Through Faith"));
		Assert.assertEquals("100",
				Component.getStrengthEnglish("100"));
		Assert.assertEquals("0,0,001",
				Component.getStrengthEnglish("0,0,001"));
		Assert.assertEquals("0.0.001",
				Component.getStrengthEnglish("0.0.001"));
		Assert.assertEquals(".,",
				Component.getStrengthEnglish(".,"));
		Assert.assertEquals("1.000,",
				Component.getStrengthEnglish("1.000,"));
		Assert.assertEquals("1.000,0,0",
				Component.getStrengthEnglish("1.000,0,0"));
		Assert.assertEquals("1,000.00",
				Component.getStrengthEnglish("1,000.00"));
		Assert.assertEquals("0.001",
				Component.getStrengthEnglish("0,001"));
		Assert.assertEquals("000.001",
				Component.getStrengthEnglish("000,001"));
		Assert.assertEquals("1.250",
				Component.getStrengthEnglish("1,250"));
		Assert.assertEquals("1.250",
				Component.getStrengthEnglish("1.250"));
		Assert.assertEquals("10,000.05",
				Component.getStrengthEnglish("10.000,05"));
		Assert.assertEquals("10,000,000.05",
				Component.getStrengthEnglish("10.000.000,05"));
	}
}
