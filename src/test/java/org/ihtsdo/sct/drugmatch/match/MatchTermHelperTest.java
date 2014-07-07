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
		componentTokens = MatchTermHelper.getComponentTermTokens("Polythiazide 0.5mg/2mg prazosin hydrochloride"); // excluding dose form
		Assert.assertNotNull(componentTokens);
		Assert.assertEquals("[Polythiazide 0.5mg, "
				+ "2mg prazosin hydrochloride]",
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
	public final void getMatchPharmaceuticalRuleEnglish4MultiComponent() {
		List<Component> multipleComponents = new ArrayList<>();
		multipleComponents.add(new Component("Substance A English",
				null, // substanceNameNational
				"10",
				"mg"));
		multipleComponents.add(new Component("Substance B English",
				null, // substanceNameNational
				"20",
				"cg"));
		multipleComponents.add(new Component("Substance C English",
				null, // substanceNameNational
				"30",
				"dg"));
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(multipleComponents,
				"Oral tablet",
				null, // doseFormNational
				null, // drugId
				"Trade name");
		// missing component
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.SUBSTANCE_MISSING_CHECK_CONCEPT,
						"Trade name substance A English 10 mg + substance C English 30 dg oral tablet",
						expectedPharmaceutical));
		// generic incorrect component order
		Assert.assertEquals(MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.EXACT_MATCH,
						"Substance B English 20 cg + substance C English 30 dg + substance A English 10 mg oral tablet",
						expectedPharmaceutical));
		// case insensitive pharmaceutical match
		Assert.assertEquals(MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.EXACT_MATCH,
						"TRADE NAME SUBSTANCE A ENGLISH 10 MG + SUBSTANCE B ENGLISH 20 CG + SUBSTANCE C ENGLISH 30 DG ORAL TABLET",
						expectedPharmaceutical));
	}

	@Test
	public final void getMatchPharmaceuticalRuleEnglish4SingleComponent() {
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(Collections.unmodifiableList(Collections.singletonList(new Component("Substance name English",
				null, // substanceNameNational
				"10",
				"mg"))),
				"Oral tablet",
				null, // doseFormNational
				null, // drugId
				"Trade name");
		// missing component
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.DOSE_FORM_MISSING_CHECK_CONCEPT,
						"",
						expectedPharmaceutical));
		// undesired component - substance
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name subst4nc3 name English 10 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - strength
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name substance name English 100 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - unit
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name substance name English 10 kg oral tablet",
						expectedPharmaceutical));
		// missing dose form
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_ENGLISH_DOSE_FORM,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance name English 10 mg",
						expectedPharmaceutical));
		// partial dose form
		Assert.assertEquals(MatchTermRule.GENERIC_PARTIAL_ENGLISH_DOSE_FORM,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance name English 10 mg tablet",
						expectedPharmaceutical));
		// missing trade name
		Assert.assertEquals(MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH,
				MatchTermHelper.getMatchPharmaceuticalRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"substance name English 10 mg oral tablet",
						expectedPharmaceutical));
	}

	@Test
	public final void getMatchPharmaceuticalRuleNational4MultiComponent() {
		List<Component> multipleComponents = new ArrayList<>();
		multipleComponents.add(new Component(null, // substanceNameEnglish
				"Substance A national",
				"10",
				"mg"));
		multipleComponents.add(new Component(null, // substanceNameEnglish
				"Substance B national",
				"20",
				"cg"));
		multipleComponents.add(new Component(null, // substanceNameEnglish
				"Substance C national",
				"30",
				"dg"));
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(multipleComponents,
				null, // doseFormEnglish
				"oral tablet",
				null, // drugId
				"Trade name");
		// Exact pharmaceutical match
		Assert.assertEquals(MatchTermRule.PHARMACEUTICAL_EXACT_NATIONAL_MATCH,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.EXACT_MATCH,
						"Trade name substance A national 10 mg + substance B national 20 cg + substance C national 30 dg oral tablet",
						expectedPharmaceutical));
		// missing component
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.SUBSTANCE_MISSING_CHECK_CONCEPT,
						"Trade name substance A national 10 mg + substance C national 30 dg oral tablet",
						expectedPharmaceutical));
		// generic incorrect component order
		Assert.assertEquals(MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.EXACT_MATCH,
						"Substance B national 20 cg + substance C national 30 dg + substance A national 10 mg oral tablet",
						expectedPharmaceutical));
		// incorrect component order
		Assert.assertEquals(MatchTermRule.PHARMACEUTICAL_INCORRECT_COMPONENT_ORDER_NATIONAL,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.EXACT_MATCH,
						"Trade name substance B national 20 cg + substance C national 30 dg + substance A national 10 mg oral tablet",
						expectedPharmaceutical));
		// case insensitive pharmaceutical match
		Assert.assertEquals(MatchTermRule.PHARMACEUTICAL_CASE_INSENSITIVE_NATIONAL_MATCH,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.EXACT_MATCH,
						"TRADE NAME SUBSTANCE A NATIONAL 10 MG + SUBSTANCE B NATIONAL 20 CG + SUBSTANCE C NATIONAL 30 DG ORAL TABLET",
						expectedPharmaceutical));
	}

	@Test
	public final void getMatchPharmaceuticalRuleNational4SingleComponent() {
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(Collections.unmodifiableList(Collections.singletonList(new Component(null, // substanceNameEnglish
				"Substance name national",
				"10",
				"mg"))),
				null, // doseFormEnglish
				"oral tablet",
				null, // drugId
				"Trade name");
		// Exact pharmaceutical match
		Assert.assertEquals(MatchTermRule.PHARMACEUTICAL_EXACT_NATIONAL_MATCH,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.EXACT_MATCH,
						"Trade name substance name national 10 mg oral tablet",
						expectedPharmaceutical));
		// missing component
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.SUBSTANCE_MISSING_CHECK_CONCEPT,
						"",
						expectedPharmaceutical));
		// undesired component - substance
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name subst4nc3 name national 10 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - strength
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name substance name national 100 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - unit
		Assert.assertEquals(null,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name substance name national 10 kg oral tablet",
						expectedPharmaceutical));
		// missing dose form
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_NATIONAL_DOSE_FORM,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance name national 10 mg",
						expectedPharmaceutical));
		// partial dose form
		Assert.assertEquals(MatchTermRule.GENERIC_PARTIAL_NATIONAL_DOSE_FORM,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance name national 10 mg tablet",
						expectedPharmaceutical));
		// missing trade name
		Assert.assertEquals(MatchTermRule.GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"substance name national 10 mg oral tablet",
						expectedPharmaceutical));
		// partial trade name
		Assert.assertEquals(MatchTermRule.PHARMACEUTICAL_PARTIAL_TRADE_NAME_NATIONAL,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade substance name national 10 mg oral tablet",
						expectedPharmaceutical));
		// case insensitive pharmaceutical match
		Assert.assertEquals(MatchTermRule.PHARMACEUTICAL_CASE_INSENSITIVE_NATIONAL_MATCH,
				MatchTermHelper.getMatchPharmaceuticalRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"TRADE NAME SUBSTANCE NAME NATIONAL 10 MG ORAL TABLET",
						expectedPharmaceutical));
	}

	@Test
	public final void getMatchTermRuleEnglish4MultiComponent() {
		List<Component> multipleComponents = new ArrayList<>();
		multipleComponents.add(new Component("Substance A English",
				null, // substanceNameNational
				"10",
				"mg"));
		multipleComponents.add(new Component("Substance B English",
				null, // substanceNameNational
				"20",
				"cg"));
		multipleComponents.add(new Component("Substance C English",
				null, // substanceNameNational
				"30",
				"dg"));
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(multipleComponents,
				"Oral tablet",
				null, // doseFormNational
				null, // drugId
				"Trade name");
		// missing component
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_ENGLISH_SUBSTANCE,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.DOSE_FORM_MISSING_CHECK_CONCEPT,
						"Trade name substance A English 10 mg + substance C English 30 dg oral tablet",
						expectedPharmaceutical));
		// generic incorrect component order
		Assert.assertEquals(MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM,
						"Substance B English 20 cg + substance C English 30 dg + substance A English 10 mg oral tablet",
						expectedPharmaceutical));
		// case insensitive match
		Assert.assertEquals(MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,
						"SUBSTANCE A ENGLISH 10MG + SUBSTANCE B ENGLISH 20CG + SUBSTANCE C ENGLISH 30DG ORAL TABLET",
						expectedPharmaceutical));
	}

	@Test
	public final void getMatchTermRuleEnglish4SingleComponent() {
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(Collections.unmodifiableList(Collections.singletonList(new Component("Substance name English",
				null, // substanceNameNational
				"10",
				"mg"))),
				"Oral tablet",
				null, // doseFormNational
				null, // drugId
				"Trade name");
		// generic
		Assert.assertEquals(MatchTermRule.GENERIC_EXACT_ENGLISH_MATCH,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Substance name English 10mg oral tablet",
						expectedPharmaceutical));
		// generic case-insensitive
		Assert.assertEquals(MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"substance name English 10mg oral tablet",
						expectedPharmaceutical));
		// undesired component - substance
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_ENGLISH_SUBSTANCE,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name subst4nc3 name English 10 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - strength
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_ENGLISH_STRENGTH,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name substance name English 100 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - unit
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_ENGLISH_UNIT,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name substance name English 10 kg oral tablet",
						expectedPharmaceutical));
		// missing dose form
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_ENGLISH_DOSE_FORM,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance name English 10mg",
						expectedPharmaceutical));
		// partial dose form
		Assert.assertEquals(MatchTermRule.GENERIC_PARTIAL_ENGLISH_DOSE_FORM,
				MatchTermHelper.getMatchTermRuleEnglish(MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance name English 10mg tablet",
						expectedPharmaceutical));
	}

	@Test
	public final void getMatchTermRuleNational4MultiComponent() {
		List<Component> multipleComponents = new ArrayList<>();
		multipleComponents.add(new Component(null, // substanceNameEnglish
				"Substance A national",
				"10",
				"mg"));
		multipleComponents.add(new Component(null, // substanceNameEnglish
				"Substance B national",
				"20",
				"cg"));
		multipleComponents.add(new Component(null, // substanceNameEnglish
				"Substance C national",
				"30",
				"dg"));
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(multipleComponents,
				null, // doseFormEnglish
				"oral tablet",
				null, // drugId
				"Trade name");
		// missing component
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_NATIONAL_SUBSTANCE,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.DOSE_FORM_MISSING_CHECK_CONCEPT,
						"Trade name substance A national 10 mg + substance C national 30 dg oral tablet",
						expectedPharmaceutical));
		// generic incorrect component order
		Assert.assertEquals(MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM,
						"Substance B national 20 cg + substance C national 30 dg + substance A national 10 mg oral tablet",
						expectedPharmaceutical));
		// incorrect component order
		Assert.assertEquals(MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.EXACT_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance B national 20 cg + substance C national 30 dg + substance A national 10 mg oral tablet",
						expectedPharmaceutical));
		// case insensitive pharmaceutical match
		Assert.assertEquals(MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,
						"TRADE NAME SUBSTANCE A NATIONAL 10 MG + SUBSTANCE B NATIONAL 20 CG + SUBSTANCE C NATIONAL 30 DG ORAL TABLET",
						expectedPharmaceutical));
	}

	@Test
	public final void getMatchTermRuleNational4SingleComponent() {
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(Collections.unmodifiableList(Collections.singletonList(new Component(null, // substanceNameEnglish
				"Substance name national",
				"10",
				"mg"))),
				null, // doseFormEnglish
				"oral tablet",
				null, // drugId
				"Trade name");
		// generic
		Assert.assertEquals(MatchTermRule.GENERIC_EXACT_NATIONAL_MATCH,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Substance name national 10 mg oral tablet",
						expectedPharmaceutical));
		// generic case-insensitive
		Assert.assertEquals(MatchTermRule.GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"substance name national 10 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - substance
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_NATIONAL_SUBSTANCE,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name subst4nc3 name national 10 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - strength
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_NATIONAL_STRENGTH,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name substance name national 100 mg oral tablet",
						expectedPharmaceutical));
		// undesired component - unit
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_NATIONAL_UNIT,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH,
						"Trade name substance name national 10 kg oral tablet",
						expectedPharmaceutical));
		// missing dose form
		Assert.assertEquals(MatchTermRule.GENERIC_MISSING_NATIONAL_DOSE_FORM,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance name national 10mg",
						expectedPharmaceutical));
		// partial dose form
		Assert.assertEquals(MatchTermRule.GENERIC_PARTIAL_NATIONAL_DOSE_FORM,
				MatchTermHelper.getMatchTermRuleNational(MatchAttributeRule.AMBIGUOUS_MATCH_EXCLUDING_DOSE_FORM,
						"Trade name substance name national 10mg tablet",
						expectedPharmaceutical));
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
		// ends with
		Assert.assertEquals("oral tablet",
				MatchTermHelper.getTermDoseForm("Substance 10 mg oral tablet",
						Collections.<Component>emptyList(),
						"Oral tablet"));
		// backwards partial token match
		Assert.assertEquals("tablet",
				MatchTermHelper.getTermDoseForm(AZATHIOPRINE_TERM,
						AZATHIOPRINE_COMPONENTS,
						"Oral tablet"));
	}

	@Test
	public final void getTermPharmaceutical4ExcessiveWhitespace() {
		// emulate a parsed Pharmaceutical with excessive whitespace (ie. user supplied content)
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(Collections.unmodifiableList(Collections.singletonList(new Component("Component \tname \nEnglish",
				"Component \tname \nnational",
				"10",
				"mg"))),
				"Oral   tablet",
				"oral   tablet",
				null, // drugId
				"Trade \tname \nnational");
		// English term (normalized)
		Pharmaceutical termPharmaceutical = MatchTermHelper.getTermPharmaceutical("Component name English 10mg tablet",
						true, // isEnglish
						expectedPharmaceutical);
		Assert.assertNotNull(termPharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=null, "
				+ "doseForm=DoseForm [nameEnglish=tablet, nameNational=null], "
				+ "components=["
				+ "Component [substance=Substance [nameEnglish=Component name English, nameNational=null], strength=10, unit=mg]]]",
				termPharmaceutical.toString());
		// national term (normalized)
		termPharmaceutical = MatchTermHelper.getTermPharmaceutical("Trade name national component name national 10 mg tablet",
				false, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(termPharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=Trade name national, "
				+ "doseForm=DoseForm [nameEnglish=null, nameNational=tablet], "
				+ "components=["
				+ "Component [substance=Substance [nameEnglish=null, nameNational=component name national], strength=10, unit=mg]]]",
				termPharmaceutical.toString());
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
		// verify term component order is obeyed
		pharmaceutical = MatchTermHelper.getTermPharmaceutical("Stavudine 30mg + nevirapine 200mg + lamivudine 150mg tablet",
				true, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
					+ "drugId=null, tradeName=null, "
					+ "doseForm=DoseForm [nameEnglish=tablet, nameNational=null], "
					+ "components=["
					+ "Component [substance=Substance [nameEnglish=Stavudine, nameNational=null], strength=30, unit=mg], "
					+ "Component [substance=Substance [nameEnglish=nevirapine, nameNational=null], strength=200, unit=mg], "
					+ "Component [substance=Substance [nameEnglish=lamivudine, nameNational=null], strength=150, unit=mg]]]",
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
		// verify term component order is obeyed
		pharmaceutical = MatchTermHelper.getTermPharmaceutical("Alcohol + codeine phosphate + acetaminophen 7%vv/12mg/120mg elixir",
				true, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=null, "
				+ "doseForm=DoseForm [nameEnglish=elixir, nameNational=null], "
				+ "components=["
				+ "Component [substance=Substance [nameEnglish=Alcohol, nameNational=null], strength=7, unit=%vv], "
				+ "Component [substance=Substance [nameEnglish=codeine phosphate, nameNational=null], strength=12, unit=mg], "
				+ "Component [substance=Substance [nameEnglish=acetaminophen, nameNational=null], strength=120, unit=mg]]]",
				pharmaceutical.toString());
	}

	@Test
	public final void getTermPharmaceutical4SingleComponent() {
		Pharmaceutical expectedPharmaceutical = new Pharmaceutical(AZATHIOPRINE_COMPONENTS,
				"Oral tablet",
				"oral tablet",
				null, // drugId
				"Imuran");
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

		pharmaceutical = MatchTermHelper.getTermPharmaceutical("Imuran azathioprin 10  mg tablet",
				false, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=Imuran, "
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

		// national decimal versus English decimal
		expectedPharmaceutical = new Pharmaceutical(Collections.unmodifiableList(Collections.singletonList(new Component("Azathioprine",
						"Azathioprin",
						"0,01",
						"mg"))),
				"Oral tablet",
				"oral tablet",
				null, // drugId
				"Trade name");
		pharmaceutical = MatchTermHelper.getTermPharmaceutical("Azathioprine 0.01 mg tablet",
				true, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=null, "
				+ "doseForm=DoseForm [nameEnglish=tablet, nameNational=null], "
				+ "components=[Component [substance=Substance [nameEnglish=Azathioprine, nameNational=null], strength=0.01, unit=mg]]]",
				pharmaceutical.toString());

		pharmaceutical = MatchTermHelper.getTermPharmaceutical("Trade navn azathioprin 0,01 mg tablet",
				false, // isEnglish
				expectedPharmaceutical);
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=null, tradeName=Trade, "
				+ "doseForm=DoseForm [nameEnglish=null, nameNational=tablet], "
				+ "components=[Component [substance=Substance [nameEnglish=null, nameNational=azathioprin], strength=0,01, unit=mg]]]",
				pharmaceutical.toString());
	}
}
