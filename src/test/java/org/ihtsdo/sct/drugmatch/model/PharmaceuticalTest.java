package org.ihtsdo.sct.drugmatch.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
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
	public final void getGenericUUID() throws UnsupportedEncodingException {
		Assert.assertEquals("88978602-4566-3bc3-9724-e39e36b779ee",
				PHARMACEUTICAL.getGenericUUID().toString());
		// "random" component order
		List<Component> reversedComponents = new ArrayList<>(PHARMACEUTICAL.components);
		Collections.reverse(reversedComponents);
		Pharmaceutical pharmaceuticalWithReversedComponents = new Pharmaceutical(reversedComponents,
				PHARMACEUTICAL.doseForm.nameEnglish,
				null,
				PHARMACEUTICAL.drugId,
				PHARMACEUTICAL.tradeName);
		Assert.assertNotEquals(PHARMACEUTICAL.getEnglishTerm(),
				pharmaceuticalWithReversedComponents.getEnglishTerm());
		Assert.assertEquals(PHARMACEUTICAL.getGenericUUID().toString(),
				pharmaceuticalWithReversedComponents.getGenericUUID().toString());
	}

	@Test
	public final void getNationalTerm() {
		Assert.assertEquals("Substance name national_1 strength_1 unit_1 + substance name national_2 strength_2 unit_2 dose form national",
				PHARMACEUTICAL.getNationalTerm());
	}

	@Test
	public final void getNormalizedTradeName() {
		Pharmaceutical pharmaceutical = new Pharmaceutical(null, // components
				null, // doseFormEnglish
				null, // doseFormNational
				null, // drugId
				"several trade \t\n name  tokens");
		Assert.assertEquals("several trade name tokens",
				pharmaceutical.getNormalizedTradeName());
	}

	@Test
	public final void getPharmaceuticalTerm() {
		Assert.assertEquals("tradeName substance name national_1 strength_1 unit_1 + substance name national_2 strength_2 unit_2 dose form national",
				PHARMACEUTICAL.getPharmaceuticalTerm());
	}

	@Test
	public final void getPharmaceuticalUUID() throws UnsupportedEncodingException {
		Assert.assertEquals("5e85a530-3858-35cb-8e0e-a9b5825a0a03",
				PHARMACEUTICAL.getPharmaceuticalUUID().toString());
		// "random" component order
		List<Component> reversedComponents = new ArrayList<>(PHARMACEUTICAL.components);
		Collections.reverse(reversedComponents);
		Pharmaceutical pharmaceuticalWithReversedComponents = new Pharmaceutical(reversedComponents,
				null,
				PHARMACEUTICAL.doseForm.nameNational,
				PHARMACEUTICAL.drugId,
				PHARMACEUTICAL.tradeName);
		Assert.assertNotEquals(PHARMACEUTICAL.getEnglishTerm(),
				pharmaceuticalWithReversedComponents.getEnglishTerm());
		Assert.assertEquals(PHARMACEUTICAL.getPharmaceuticalUUID().toString(),
				pharmaceuticalWithReversedComponents.getPharmaceuticalUUID().toString());
	}
}
