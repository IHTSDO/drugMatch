package org.ihtsdo.sct.drugmatch.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class DoseFormTest {

	private static final DoseForm DOSE_FORM = new DoseForm("several English dose \t\n form  tokens",
			"several national dose \t\n form  tokens");

	@Test
	public final void getNormalizedNameEnglish() {
		Assert.assertEquals("several English dose form tokens",
				DOSE_FORM.getNormalizedNameEnglish());
	}

	@Test
	public final void getNormalizedNameNational() {
		Assert.assertEquals("several national dose form tokens",
				DOSE_FORM.getNormalizedNameNational());
	}
}
