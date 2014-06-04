package org.ihtsdo.sct.drugmatch.check;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class CheckValidationImplTest {

	@Test
	public final void getMessage() {
		CheckValidation checkValidation = new CheckValidationImpl();
		for (CheckRule checkRule : CheckRule.values()) {
			Assert.assertEquals(checkRule.toString(),
					checkValidation.getMessage(checkRule));
		}
	}
}
