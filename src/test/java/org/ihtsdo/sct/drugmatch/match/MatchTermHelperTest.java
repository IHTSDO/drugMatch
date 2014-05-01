package org.ihtsdo.sct.drugmatch.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class MatchTermHelperTest {

	private static final List<Component> ACETAMINOPHEN_ALCOHOL_CODEINE_PHOSPHATE_COMPONENTS,
		AZATHIOPRINE_COMPONENTS,
		LAMIVUDINE_STAVUDINE_NEVIRAPINE_COMPONENTS;

	private static final String ACETAMINOPHEN_ALCOHOL_CODEINE_PHOSPHATE_TERM = "Acetaminophen + alcohol + codeine phosphate 120mg/7%vv/12mg elixir",
		AZATHIOPRINE_TERM = "Azathioprine 10mg tablet",
		LAMIVUDINE_STAVUDINE_NEVIRAPINE_TERM = "Lamivudine 150mg + stavudine 30mg + nevirapine 200mg tablet";

	static {
		List<Component> acetaminophenAlcoholCodeinePhosphateComponents = new ArrayList<>();
		acetaminophenAlcoholCodeinePhosphateComponents.add(new Component("Acetaminophen",
				"Paracetamol",
				"120",
				"mg"));
		acetaminophenAlcoholCodeinePhosphateComponents.add(new Component("Alcohol",
				"Alkohol",
				"7",
				"%vv"));
		acetaminophenAlcoholCodeinePhosphateComponents.add(new Component("Codeine phosphate",
				"codeinphosphat",
				"12",
				"mg"));
		ACETAMINOPHEN_ALCOHOL_CODEINE_PHOSPHATE_COMPONENTS = Collections.unmodifiableList(acetaminophenAlcoholCodeinePhosphateComponents);

		AZATHIOPRINE_COMPONENTS = Collections.unmodifiableList(Collections.singletonList(new Component("Azathioprine",
				"Azathioprin",
				"10",
				"mg")));

		List<Component> lamivudineStavudineNevirapineComponents = new ArrayList<>();
		lamivudineStavudineNevirapineComponents.add(new Component("Lamivudine",
				"Lamivudin",
				"150",
				"mg"));
		lamivudineStavudineNevirapineComponents.add(new Component("Stavudine",
				"Stavudin",
				"30",
				"mg"));
		lamivudineStavudineNevirapineComponents.add(new Component("Nevirapine",
				"Nevirapin",
				"200",
				"mg"));
		LAMIVUDINE_STAVUDINE_NEVIRAPINE_COMPONENTS = Collections.unmodifiableList(lamivudineStavudineNevirapineComponents);
	}


	@Test
	public final void getComponentTermTokens4MultiComponent() {
		String[] componentTokens = MatchTermHelper.getComponentTermTokens("Lamivudine 150mg + stavudine 30mg + nevirapine 200mg"); // excluding dose form
		Assert.assertNotNull(componentTokens);
		Assert.assertEquals("[Lamivudine 150mg ,  "
				+ "stavudine 30mg ,  "
				+ "nevirapine 200mg]",
				Arrays.toString(componentTokens));
	}

	@Test
	public final void getComponentTermTokens4MultiComponent_slash_notation() {
		String[] componentTokens = MatchTermHelper.getComponentTermTokens("Acetaminophen + alcohol + codeine phosphate 120mg/7%vv/12mg"); // excluding dose form
		Assert.assertNotNull(componentTokens);
		Assert.assertEquals("[Acetaminophen  120mg,  "
				+ "alcohol  7%vv,  "
				+ "codeine phosphate  12mg]",
				Arrays.toString(componentTokens));
	}

	@Test
	public final void getComponentTermTokens4SingleComponent() {
		String[] componentTokens = MatchTermHelper.getComponentTermTokens("Azathioprine 10mg"); // excluding dose form
		Assert.assertNotNull(componentTokens);
		Assert.assertEquals("[Azathioprine 10mg]",
				Arrays.toString(componentTokens));
	}


	@Test
	public final void getTermDoseForm4MultiComponent() {
		Assert.assertEquals("tablet",
				MatchTermHelper.getTermDoseForm(LAMIVUDINE_STAVUDINE_NEVIRAPINE_TERM,
						LAMIVUDINE_STAVUDINE_NEVIRAPINE_COMPONENTS,
						"Oral tablet"));
	}

	@Test
	public final void getTermDoseForm4MultiComponent_slash_notation() {
		Assert.assertEquals("elixir",
				MatchTermHelper.getTermDoseForm(ACETAMINOPHEN_ALCOHOL_CODEINE_PHOSPHATE_TERM,
						ACETAMINOPHEN_ALCOHOL_CODEINE_PHOSPHATE_COMPONENTS,
						"Oral dosage form"));
	}

	@Test
	public final void getTermDoseForm4SingleComponent() {
		Assert.assertEquals("tablet",
				MatchTermHelper.getTermDoseForm(AZATHIOPRINE_TERM,
						AZATHIOPRINE_COMPONENTS,
						"Oral tablet"));
	}


	@Test
	public final void getTermPharmaceutical4MultiComponent() {
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(LAMIVUDINE_STAVUDINE_NEVIRAPINE_COMPONENTS,
				"Oral tablet",
				null, // doseFormNational
				null, // drugId
				null); // tradeName
		Pharmaceutical pharmaceutical = MatchTermHelper.getTermPharmaceutical(LAMIVUDINE_STAVUDINE_NEVIRAPINE_TERM,
				true, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
					+ "drugId=null, tradeName=null, "
					+ "doseForm=DoseForm [nameEnglish=tablet, nameNational=null], "
					+ "components=["
					+ "Component [substance=Substance [nameEnglish=Lamivudine, nameNational=null], strength=150, unit=mg], "
					+ "Component [substance=Substance [nameEnglish=stavudine, nameNational=null], strength=30, unit=mg], "
					+ "Component [substance=Substance [nameEnglish=nevirapine, nameNational=null], strength=200, unit=mg]]]",
				pharmaceutical.toString());
	}

	@Test
	public final void getTermPharmaceutical4MultiComponent_slash_notation() {
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(ACETAMINOPHEN_ALCOHOL_CODEINE_PHOSPHATE_COMPONENTS,
				"Oral dosage form",
				null, // doseFormNational
				null, // drugId
				null); // tradeName
		Pharmaceutical pharmaceutical = MatchTermHelper.getTermPharmaceutical(ACETAMINOPHEN_ALCOHOL_CODEINE_PHOSPHATE_TERM,
				true, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=null, "
				+ "doseForm=DoseForm [nameEnglish=elixir, nameNational=null], "
				+ "components=["
				+ "Component [substance=Substance [nameEnglish=Acetaminophen, nameNational=null], strength=120, unit=mg], "
				+ "Component [substance=Substance [nameEnglish=alcohol, nameNational=null], strength=7, unit=%vv], "
				+ "Component [substance=Substance [nameEnglish=codeine phosphate, nameNational=null], strength=12, unit=mg]]]",
				pharmaceutical.toString());
	}

	@Test
	public final void getTermPharmaceutical4SingleComponent() {
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(AZATHIOPRINE_COMPONENTS,
				"Oral tablet",
				"oral tablet", // doseFormNational
				null, // drugId
				null); // tradeName
		Pharmaceutical pharmaceutical = MatchTermHelper.getTermPharmaceutical(AZATHIOPRINE_TERM,
						true, // isEnglish
						expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=null, "
				+ "doseForm=DoseForm [nameEnglish=tablet, nameNational=null], "
				+ "components=["
				+ "Component [substance=Substance [nameEnglish=Azathioprine, nameNational=null], strength=10, unit=mg]]]",
				pharmaceutical.toString());

		pharmaceutical = MatchTermHelper.getTermPharmaceutical("azathioprin 10  mg tablet",
				false, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=null, "
				+ "doseForm=DoseForm [nameEnglish=null, nameNational=tablet], "
				+ "components=[Component [substance=Substance [nameEnglish=null, nameNational=azathioprin], strength=10, unit=mg]]]",
				pharmaceutical.toString());

		pharmaceutical = MatchTermHelper.getTermPharmaceutical("Azathioprine 100 mg tablet",
				true, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=null, "
				+ "doseForm=DoseForm [nameEnglish=tablet, nameNational=null], "
				+ "components=[]]",
		pharmaceutical.toString());
	}
}
