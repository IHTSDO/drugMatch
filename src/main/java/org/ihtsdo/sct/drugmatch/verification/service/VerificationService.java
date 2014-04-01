package org.ihtsdo.sct.drugmatch.verification.service;

import java.io.IOException;
import java.util.List;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;

/**
 * @author dev-team@carecom.dk
 *
 */
public interface VerificationService {

	List<ConceptSearchResultDescriptor> getDoseFormExactEnglishPreferredTermMatch(String query) throws IOException, DrugMatchConfigurationException;

	List<ConceptSearchResultDescriptor> getSubstanceExactEnglishPreferredTermMatch(String query) throws IOException, DrugMatchConfigurationException;

	List<ConceptSearchResultDescriptor> getSubstanceExactNationalPreferredTermMatch(String query) throws IOException, DrugMatchConfigurationException;

	List<ConceptSearchResultDescriptor> getUnitExactEnglishPreferredTermMatch(String query) throws IOException, DrugMatchConfigurationException;
}