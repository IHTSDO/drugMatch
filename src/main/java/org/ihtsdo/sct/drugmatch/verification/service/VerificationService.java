package org.ihtsdo.sct.drugmatch.verification.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptDescriptor;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;

/**
 * Support "Check" & "Match" logic.
 * @author dev-team@carecom.dk
 */
public interface VerificationService {

	/**
	 * @param attributeIds
	 * @param valueIds
	 * @return {@link List}({@link ConceptSearchResultDescriptor}), exact active published attribute relationship matches.
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	List<ConceptSearchResultDescriptor> getAttributeExactMatch(Set<Long> attributeIds,
			Set<Long> valueIds) throws DrugMatchConfigurationException, IOException;

	/**
	 * @param conceptIds
	 * @return {@link List}({@link ConceptSearchResultDescriptor}) matching the given SCT IDs.
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	List<ConceptDescriptor> getConceptsByIds(Set<Long> conceptIds) throws DrugMatchConfigurationException, IOException;

	/**
	 * Generate link to Concept presentation.
	 * @param conceptIds
	 * @return URL as String
	 * @throws DrugMatchConfigurationException
	 */
	String getConceptsByIdsUrl(Set<Long> conceptIds) throws DrugMatchConfigurationException;

	/**
	 * Retrieve exact preferred term (active & published) match(es) with English language code, under the {@link DrugMatchProperties#getConstraintIdDoseForm()} top point.
	 * @param query
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	List<ConceptSearchResultDescriptor> getDoseFormExactEnglishPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	/**
	 * Retrieve exact preferred term (active & published) match(es) with national namespace ID, under the {@link DrugMatchProperties#getConstraintIdDoseForm()} top point.
	 * @param query
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	List<ConceptSearchResultDescriptor> getDoseFormExactNationalPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	/**
	 * Retrieve exact preferred term (active & published) match(es) with English language code, under the {@link DrugMatchProperties#getConstraintIdSubstance()} top point.
	 * @param query
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	List<ConceptSearchResultDescriptor> getSubstanceExactEnglishPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	/**
	 * Retrieve exact preferred term (active & published) match(es) with national namespace ID, under the {@link DrugMatchProperties#getConstraintIdSubstance()} top point.
	 * @param query
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	List<ConceptSearchResultDescriptor> getSubstanceExactNationalPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	/**
	 * Retrieve exact preferred term (active & published) match(es) with English language code, under the {@link DrugMatchProperties#getConstraintIdUnit()} top point.
	 * @param query
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	List<ConceptSearchResultDescriptor> getUnitExactEnglishPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;

	/**
	 * Retrieve exact preferred term (active & published) match(es) with national namespace ID, under the {@link DrugMatchProperties#getConstraintIdUnit()} top point.
	 * @param query
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	List<ConceptSearchResultDescriptor> getUnitExactNationalPreferredTermMatch(String query) throws DrugMatchConfigurationException, IOException;
}
