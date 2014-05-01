package org.ihtsdo.sct.drugmatch.check.extension;

import org.ihtsdo.sct.drugmatch.check.CheckValidation;
import org.ihtsdo.sct.drugmatch.check.CheckValidationImpl;
import org.ihtsdo.sct.drugmatch.check.extension.danish.CheckValidationDanishImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class CheckValidationHelperTest {

	@Test
	public final void getCheckValidation() {
		// null
		CheckValidation checkValidation = CheckValidationHelper.getCheckValidation(null);
		Assert.assertNotNull(checkValidation);
		Assert.assertTrue(checkValidation instanceof CheckValidationImpl);
		// empty
		checkValidation = CheckValidationHelper.getCheckValidation("");
		Assert.assertNotNull(checkValidation);
		Assert.assertTrue(checkValidation instanceof CheckValidationImpl);
		// Danish extension
		checkValidation = CheckValidationHelper.getCheckValidation("1000005");
		Assert.assertNotNull(checkValidation);
		Assert.assertTrue(checkValidation instanceof CheckValidationDanishImpl);
	}
}
