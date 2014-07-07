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
	public final void getEnglishPharmaceuticalTerm() {
		Assert.assertEquals("tradeName substance name English_1 strength_1unit_1 + substance name English_2 strength_2unit_2 dose form English",
				PHARMACEUTICAL.getEnglishPharmaceuticalTerm());
	}

	@Test
	public final void getEnglishTerm() {
		Assert.assertEquals("Substance name English_1 strength_1unit_1 + substance name English_2 strength_2unit_2 dose form English",
				PHARMACEUTICAL.getEnglishTerm());
	}

	@Test
	public final void getGenericUUID() throws UnsupportedEncodingException {
		Assert.assertEquals("a5f81fff-2b2f-30b8-8013-82f4170f7a51",
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
	public final void getNationalPharmaceuticalTerm() {
		Assert.assertEquals("tradeName substance name national_1 strength_1 unit_1 + substance name national_2 strength_2 unit_2 dose form national",
				PHARMACEUTICAL.getNationalPharmaceuticalTerm());
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
	public final void getPharmaceuticalUUID() throws UnsupportedEncodingException {
		Assert.assertEquals("f32b0752-7d55-3bc4-96b6-d2bc2fe3738b",
				PHARMACEUTICAL.getPharmaceuticalUUID().toString());
		// "random" component order
		List<Component> reversedComponents = new ArrayList<>(PHARMACEUTICAL.components);
		Collections.reverse(reversedComponents);
		Pharmaceutical pharmaceuticalWithReversedComponents = new Pharmaceutical(reversedComponents,
				PHARMACEUTICAL.doseForm.nameEnglish,
				PHARMACEUTICAL.doseForm.nameNational,
				PHARMACEUTICAL.drugId,
				PHARMACEUTICAL.tradeName);
		Assert.assertNotEquals(PHARMACEUTICAL.getEnglishTerm(),
				pharmaceuticalWithReversedComponents.getEnglishTerm());
		Assert.assertEquals(PHARMACEUTICAL.getPharmaceuticalUUID().toString(),
				pharmaceuticalWithReversedComponents.getPharmaceuticalUUID().toString());
	}
}
