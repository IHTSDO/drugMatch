package org.ihtsdo.sct.drugmatch.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class SubstanceTest {

	private static final Substance SUBSTANCE = new Substance("several English \t\n Substance  tokens",
			"several national \t\n Substance  tokens");

	@Test
	public final void getNormalizedNameEnglish() {
		Assert.assertEquals("several English Substance tokens",
				SUBSTANCE.getNormalizedNameEnglish());
	}

	@Test
	public final void getNormalizedNameNational() {
		Assert.assertEquals("several national Substance tokens",
				SUBSTANCE.getNormalizedNameNational());
	}
}
