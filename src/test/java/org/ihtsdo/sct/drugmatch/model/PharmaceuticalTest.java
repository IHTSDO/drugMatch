package org.ihtsdo.sct.drugmatch.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class PharmaceuticalTest {

	private static final Pharmaceutical PHARMACEUTICAL;

	static {
		List<Component> components = new ArrayList<>();
		components.add(new Component("Substance name English_1",
				"Substance name national_1",
				"strength_1",
				"unit_1"));
		components.add(new Component("Substance name English_2",
				"Substance name national_2",
				"strength_2",
				"unit_2"));
		PHARMACEUTICAL = new Pharmaceutical(components,
				"Dose form English",
				"Dose form national",
				"drugId",
				"tradeName");
	}

	@Test
	public final void getEnglishTerm() {
		Assert.assertEquals("Substance name English_1 strength_1 unit_1 + substance name English_2 strength_2 unit_2 dose form English",
				PHARMACEUTICAL.getEnglishTerm());
	}

	@Test
	public final void getNationalTerm() {
		Assert.assertEquals("Substance name national_1 strength_1 unit_1 + substance name national_2 strength_2 unit_2 dose form national",
				PHARMACEUTICAL.getNationalTerm());
	}
}
