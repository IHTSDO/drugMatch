package org.ihtsdo.sct.drugmatch.check.extension.danish;

import org.ihtsdo.sct.drugmatch.check.CheckRule;
import org.ihtsdo.sct.drugmatch.check.CheckValidation;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class CheckValidationDanishImplTest {

	@Test
	public final void getMessage() {
		CheckValidation checkValidation = new CheckValidationDanishImpl();
		Assert.assertEquals(CheckRule.AMBIGUOUS_MATCH.toString(),
				checkValidation.getMessage(CheckRule.AMBIGUOUS_MATCH));
		Assert.assertEquals("Case error",
				checkValidation.getMessage(CheckRule.CASE_INSENSITIVE_MATCH));
		Assert.assertEquals("Exact",
				checkValidation.getMessage(CheckRule.EXACT_MATCH));
		Assert.assertEquals("Translation missing",
				checkValidation.getMessage(CheckRule.TRANSLATION_MISSING));
		Assert.assertEquals("Missing",
				checkValidation.getMessage(CheckRule.ZERO_MATCH));
		Assert.assertEquals(CheckRule.UNCHECKED.toString(),
				checkValidation.getMessage(CheckRule.UNCHECKED));
	}
}
