package org.ihtsdo.sct.drugmatch.check;

import java.util.Collections;
import java.util.List;

import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class CheckTest {

	@Test
	public final void getMatchDescriptors() {
		// null
		List<ConceptSearchResultDescriptor> englishDescriptors = null,
			nationalDescriptors = null;
		List<ConceptSearchResultDescriptor> matchDescriptors = Check.getMatchDescriptors(englishDescriptors,
				nationalDescriptors);
		Assert.assertEquals(englishDescriptors,
				matchDescriptors);
		// empty
		nationalDescriptors = Collections.emptyList();
		matchDescriptors = Check.getMatchDescriptors(englishDescriptors,
				nationalDescriptors);
		Assert.assertEquals(nationalDescriptors,
				matchDescriptors);
		// national match
		nationalDescriptors = Collections.singletonList(new ConceptSearchResultDescriptor());
		matchDescriptors = Check.getMatchDescriptors(englishDescriptors,
				nationalDescriptors);
		Assert.assertEquals(nationalDescriptors,
				matchDescriptors);
	}
}
