package org.ihtsdo.sct.drugmatch.verification.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptDescriptor;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;

/**
 * @author dev-team@carecom.dk
 */
public interface VerificationService {

	List<ConceptSearchResultDescriptor> getAttributeExactMatch(Set<Long> attributeIds,
			Set<Long> valueIds) throws DrugMatchConfigurationException, IOException;

	List<ConceptDescriptor> getConceptsByIds(Set<Long> conceptIds) throws DrugMatchConfigurationException, IOException;

	List<ConceptSearchResultDescriptor> getDoseFormExactEnglishPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	List<ConceptSearchResultDescriptor> getDoseFormExactNationalPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	List<ConceptSearchResultDescriptor> getSubstanceExactEnglishPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	List<ConceptSearchResultDescriptor> getSubstanceExactNationalPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	List<ConceptSearchResultDescriptor> getUnitExactEnglishPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	List<ConceptSearchResultDescriptor> getUnitExactNationalPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;
}
