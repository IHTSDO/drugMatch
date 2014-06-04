package org.ihtsdo.sct.drugmatch.parser.impl;

import java.io.IOException;
import java.util.List;

import org.ihtsdo.sct.drugmatch.SystemEnvironmentTestSetup;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchStrictModeViolationException;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class CSVParserTest extends SystemEnvironmentTestSetup {

	@Test
	public final void parse() throws DrugMatchConfigurationException, DrugMatchStrictModeViolationException, IOException {
		List<Pharmaceutical> pharmaceuticals = new CSVParser().parse();
		Assert.assertNotNull(pharmaceuticals);
		Assert.assertEquals(7,
				pharmaceuticals.size());
		Pharmaceutical pharmaceutical = null;
		for (Pharmaceutical p : pharmaceuticals) {
			if ("T004".equals(p.drugId)) {
				pharmaceutical = p;
				break;
			}
		}
		Assert.assertNotNull(pharmaceutical);
		Assert.assertEquals("Pharmaceutical ["
				+ "drugId=T004, tradeName=Triomune, "
				+ "doseForm=DoseForm [nameEnglish=Oral tablet, nameNational=oral tablet], "
				+ "components=["
				+ "Component [substance=Substance [nameEnglish=Lamivudine, nameNational=Lamivudin], strength=150, unit=mg], "
				+ "Component [substance=Substance [nameEnglish=Stavudine, nameNational=Stavudin], strength=30, unit=mg], "
				+ "Component [substance=Substance [nameEnglish=Nevirapine, nameNational=Nevirapin], strength=200, unit=mg]]]",
				pharmaceutical.toString());
	}
}
