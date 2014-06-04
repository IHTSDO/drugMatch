package org.ihtsdo.sct.drugmatch.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ihtsdo.sct.drugmatch.SystemEnvironmentTestSetup;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class CheckTest extends SystemEnvironmentTestSetup {

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

	@Test
	public final void getOutputFileContentSeparator() throws DrugMatchConfigurationException {
		Assert.assertEquals(';',
				Check.getOutputFileContentSeparator());
	}

	@Test
	public final void getRule() {
		// UNCHECKED
		List<ConceptSearchResultDescriptor> matchDescriptors = null,
			nationalDescriptors = null;
		Assert.assertEquals(CheckRule.UNCHECKED,
				Check.getRule(null, // componentName
				nationalDescriptors,
				matchDescriptors));
		// TRANSLATION_MISSING
		matchDescriptors = new ArrayList<>();
		matchDescriptors.add(new ConceptSearchResultDescriptor());
		nationalDescriptors = Collections.emptyList();
		Assert.assertEquals(CheckRule.TRANSLATION_MISSING,
				Check.getRule(null, // componentName
				nationalDescriptors,
				matchDescriptors));
		// ZERO_MATCH
		matchDescriptors = Collections.emptyList();
		Assert.assertEquals(CheckRule.ZERO_MATCH,
				Check.getRule(null, // componentName
				nationalDescriptors,
				matchDescriptors));
		// EXACT_MATCH
		matchDescriptors = new ArrayList<>();
		ConceptSearchResultDescriptor descriptor = new ConceptSearchResultDescriptor();
		descriptor.descriptionTerm = "descriptionTerm";
		matchDescriptors.add(descriptor);
		nationalDescriptors = null;
		Assert.assertEquals(CheckRule.EXACT_MATCH,
				Check.getRule("descriptionTerm",
				nationalDescriptors,
				matchDescriptors));
		// CASE_INSENSITIVE_MATCH
		Assert.assertEquals(CheckRule.CASE_INSENSITIVE_MATCH,
				Check.getRule("DescriptionTerm",
				nationalDescriptors,
				matchDescriptors));
		// COMPONENT_AND_TERM_MISMATCH
		Assert.assertEquals(CheckRule.COMPONENT_AND_TERM_MISMATCH,
				Check.getRule("componentName",
				nationalDescriptors,
				matchDescriptors));
		// AMBIGUOUS_MATCH
		matchDescriptors.add(new ConceptSearchResultDescriptor());
		Assert.assertEquals(CheckRule.AMBIGUOUS_MATCH,
				Check.getRule(null, // componentName
				nationalDescriptors,
				matchDescriptors));
	}
}
