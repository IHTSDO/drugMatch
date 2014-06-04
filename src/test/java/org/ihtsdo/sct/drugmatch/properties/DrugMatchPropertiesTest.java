package org.ihtsdo.sct.drugmatch.properties;

import org.ihtsdo.sct.drugmatch.SystemEnvironmentTestSetup;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class DrugMatchPropertiesTest extends SystemEnvironmentTestSetup {

	@Test
	public final void createGenericReport() throws DrugMatchConfigurationException {
		Assert.assertTrue(DrugMatchProperties.createGenericReport());
	}

	@Test
	public final void getAttributeIdHasActiveIngredient() throws DrugMatchConfigurationException {
		Assert.assertEquals(Long.valueOf(127489000L),
				DrugMatchProperties.getAttributeIdHasActiveIngredient());
	}

	@Test
	public final void getAttributeIdHasDoseForm() throws DrugMatchConfigurationException {
		Assert.assertEquals(Long.valueOf(411116001L),
				DrugMatchProperties.getAttributeIdHasDoseForm());
	}

	@Test
	public final void getConstraintIdDoseForm() throws DrugMatchConfigurationException {
		Assert.assertEquals(Long.valueOf(105904009L),
				DrugMatchProperties.getConstraintIdDoseForm());
	}

	@Test
	public final void getConstraintIdSubstance() throws DrugMatchConfigurationException {
		Assert.assertEquals(Long.valueOf(105590001L),
				DrugMatchProperties.getConstraintIdSubstance());
	}

	@Test
	public final void getConstraintIdUnit() throws DrugMatchConfigurationException {
		Assert.assertEquals(Long.valueOf(258666001L),
				DrugMatchProperties.getConstraintIdUnit());
	}

	@Test
	public final void getFileContentQuoteCharacter() throws DrugMatchConfigurationException {
		Assert.assertEquals("\"",
				DrugMatchProperties.getFileContentQuoteCharacter());
	}

	@Test
	public final void getFileContentSeparatorCharacter() throws DrugMatchConfigurationException {
		Assert.assertNull(DrugMatchProperties.getFileContentSeparatorCharacter());
	}

	@Test
	public final void getInputFileIncludeFirstLine() throws DrugMatchConfigurationException {
		Assert.assertNull(DrugMatchProperties.getInputFileIncludeFirstLine());
	}

	@Test
	public final void getInputFilePath() throws DrugMatchConfigurationException {
		Assert.assertEquals("src/test/resource/drugmatch_input_example.csv",
				DrugMatchProperties.getInputFilePath());
	}

	@Test
	public final void getMappingDirectory() throws DrugMatchConfigurationException {
		Assert.assertEquals("./result/mapping",
				DrugMatchProperties.getMappingDirectory().toString());
	}

	@Test
	public final void getModuleId() throws DrugMatchConfigurationException {
		Assert.assertEquals("554471000005108",
				DrugMatchProperties.getModuleId());
	}

	@Test
	public final void getNationalLanguageCode() throws DrugMatchConfigurationException {
		Assert.assertEquals("da",
				DrugMatchProperties.getNationalLanguageCode());
	}

	@Test
	public final void getNationalNamespaceId() throws DrugMatchConfigurationException {
		Assert.assertEquals("1000005",
				DrugMatchProperties.getNationalNamespaceId());
	}

	@Test
	public final void getOutputDirectory() throws DrugMatchConfigurationException {
		Assert.assertEquals("./result",
				DrugMatchProperties.getOutputDirectory().toString());
	}

	@Test
	public final void getReferenceSetDirectory() throws DrugMatchConfigurationException {
		Assert.assertEquals("./result/RF2/Refset",
				DrugMatchProperties.getReferenceSetDirectory().toString());
	}

	@Test
	public final void getReleaseFormat2Directory() throws DrugMatchConfigurationException {
		Assert.assertEquals("./result/RF2",
				DrugMatchProperties.getReleaseFormat2Directory().toString());
	}

	@Test
	public final void getReportDirectory() throws DrugMatchConfigurationException {
		Assert.assertEquals("./result/report",
				DrugMatchProperties.getReportDirectory().toString());
	}

	@Test
	public final void getSctIdService() throws DrugMatchConfigurationException {
		Assert.assertEquals("http://95.85.42.232:8080/axis2/services/id_generator",
				DrugMatchProperties.getSctIdService());
	}

	@Test
	public final void getSctReleaseId() throws DrugMatchConfigurationException {
		Assert.assertEquals("20140131",
				DrugMatchProperties.getSctReleaseId());
	}

	@Test
	public final void getTerminologyDirectory() throws DrugMatchConfigurationException {
		Assert.assertEquals("./result/RF2/Terminology",
				DrugMatchProperties.getTerminologyDirectory().toString());
	}

	@Test
	public final void getVerificationLogin() throws DrugMatchConfigurationException {
		Assert.assertEquals("login",
				DrugMatchProperties.getVerificationLogin());
	}

	@Test
	public final void getVerificationPassword() throws DrugMatchConfigurationException {
		Assert.assertEquals("password",
				DrugMatchProperties.getVerificationPassword());
	}

	@Test
	public final void getVerificationService() throws DrugMatchConfigurationException {
		Assert.assertEquals("https://my.healthterm.com",
				DrugMatchProperties.getVerificationService());
	}

	@Test
	public final void isStrictMode() throws DrugMatchConfigurationException {
		Assert.assertTrue(DrugMatchProperties.isStrictMode());
	}
}
