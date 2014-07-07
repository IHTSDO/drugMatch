package org.ihtsdo.sct.drugmatch.create;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.tuple.Pair;
import org.ihtsdo.sct.drugmatch.check.Check;
import org.ihtsdo.sct.drugmatch.constant.rf2.ReleaseFormat2;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchStrictModeViolationException;
import org.ihtsdo.sct.drugmatch.id.service.IdService;
import org.ihtsdo.sct.drugmatch.match.Match;
import org.ihtsdo.sct.drugmatch.match.MatchTermRule;
import org.ihtsdo.sct.drugmatch.match.model.GenericMatch;
import org.ihtsdo.sct.drugmatch.match.model.PharmaceuticalMatch;
import org.ihtsdo.sct.drugmatch.model.Component;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.util.ComponentIdHelper;
import org.ihtsdo.sct.drugmatch.verification.service.VerificationService;
import org.ihtsdo.sct.id.service.CreateConceptIdsFaultException;
import org.ihtsdo.sct.id.service.CreateSCTIDFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * DrugMatch "Create".
 * @author dev-team@carecom.dk
 */
public class Create {

	private static final Logger log = LoggerFactory.getLogger(Create.class);

	/**
	 * {@link Map}(Component name, SNOMED CT Concept ID)
	 */
	private Map<String, Long> doseForm2Id,
		substance2Id,
		unit2Id;

	private final IdService idService;

	/**
	 * YYYYMMDD.
	 */
	private final String effectiveTime;

	private String fileNameConcept,
		fileNameDescription,
		fileNameQuantityReferenceSet,
		fileNameReferenceSetLanguage,
		fileNameRelationship,
		fileNameReportCoreConcept,
		fileNameReportExtensionConcept,
		fileNameStatedRelationship,
		placeHolderConceptId;

	/**
	 * YYYY-MM-DD HH.MM.SS.
	 */
	private final String isoNow;

	/**
	 * Implicit "Match" dependency.
	 */
	private final Match match;

	private static final String[] REPORT_HEADER = new String[] {
				"SCT Concept ID",
				"SCT Fully Specified Name"
		};

	/**
	 * @param pharmaceuticals
	 * @param idService
	 * @param isoNow YYYY-MM-DD HH.MM.SS.
	 * @param verificationService
	 * @throws DrugMatchConfigurationException
	 */
	public Create(final List<Pharmaceutical> pharmaceuticals,
			final IdService idService,
			final String isoNow,
			final VerificationService verificationService) throws DrugMatchConfigurationException {
		if (DrugMatchProperties.getModuleId() == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.MODULE_ID + "' isn't set!");
		} // else
		if (DrugMatchProperties.getNationalLanguageCode() == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.EXTENSION_LANGUAGE_CODE + "' isn't set!");
		} // else
		// extract date from isoNow and convert to SCT compatible effectiveTime
		this.effectiveTime = isoNow.substring(0, isoNow.indexOf(' ')).replace("-", "");
		this.idService = idService;
		this.isoNow = isoNow;
		this.match = new Match(pharmaceuticals, isoNow, verificationService);
	}

	/**
	 * @param token
	 * @return {@link UUID} v3 string, based on token
	 * @throws UnsupportedEncodingException
	 */
	private static String getUUID(final String token) throws UnsupportedEncodingException {
		return UUID.nameUUIDFromBytes(token.getBytes(CharEncoding.UTF_8)).toString();
	}

	/**
	 * Create attribute relationships.
	 * @param sourceId
	 * @param pharmaceutical
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void createAttributeRelationships(final String sourceId,
			final Pharmaceutical pharmaceutical) throws CreateSCTIDFaultException, DrugMatchConfigurationException, IOException {
		// dose form
		String typeId = String.valueOf(DrugMatchProperties.getAttributeIdHasDoseForm());
		Long destinationId = (pharmaceutical.doseForm.nameNational == null) ? null : this.doseForm2Id.get(pharmaceutical.doseForm.nameNational);
		if (destinationId == null) {
			destinationId = (pharmaceutical.doseForm.nameEnglish == null) ? null : this.doseForm2Id.get(pharmaceutical.doseForm.nameEnglish);
		}
		String relationshipId = this.idService.getExtensionRelationshipId(getUUID(sourceId + typeId + destinationId));
		exportRelationship(relationshipId,
				sourceId,
				String.valueOf(destinationId),
				typeId);
		// active ingredient
		typeId = String.valueOf(DrugMatchProperties.getAttributeIdHasActiveIngredient());
		for (Component component : pharmaceutical.components) {
			destinationId = (component.substance.nameNational == null) ? null : this.substance2Id.get(component.substance.nameNational);
			if (destinationId == null) {
				destinationId = (component.substance.nameEnglish == null) ? null : this.substance2Id.get(component.substance.nameEnglish);
			}
			relationshipId = this.idService.getExtensionRelationshipId(getUUID(sourceId + typeId + destinationId));
			exportRelationship(relationshipId,
					sourceId,
					String.valueOf(destinationId),
					typeId);
			exportRelationshipToQuantityReferenceSet(relationshipId,
					this.unit2Id.get(component.unit), // Concept ID
					component.strength);
		}
	}

	/**
	 * Create and export Concept.
	 * @param uuid
	 * @param parentId
	 * @return SCT Concept ID
	 * @throws CreateConceptIdsFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private String createConcept(final String uuid,
			final String parentId) throws CreateConceptIdsFaultException, DrugMatchConfigurationException, IOException {
		String conceptId = this.idService.getExtensionConceptId(uuid,
				parentId);
		exportConcept(conceptId);
		return conceptId;
	}

	/**
	 * Create and export; English fully specified name, preferred term and national preferred term.
	 * @param conceptId
	 * @param englishPreferredTerm
	 * @param nationalPreferredTerm
	 * @return English Fully Specified Name
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private String createEnglishAndNationalDescriptions(final String conceptId,
			final String englishPreferredTerm,
			final String nationalPreferredTerm) throws CreateSCTIDFaultException, DrugMatchConfigurationException, IOException {
		// national preferred term
		String descriptionId = getExtensionDescriptionId(ComponentIdHelper.getNamespaceId(conceptId),
				nationalPreferredTerm,
				DrugMatchProperties.getNationalLanguageCode());
		exportDescription(conceptId,
				descriptionId,
				nationalPreferredTerm,
				DrugMatchProperties.getNationalLanguageCode(),
				ReleaseFormat2.DESCRIPTION_TYPE_PREFERRED_TERM_ID);
		exportPreferredToLanguageReferenceSet(descriptionId);
		// English terms
		return 	createEnglishDescriptions(conceptId,
						englishPreferredTerm);
	}

	/**
	 * Create and export; English fully specified name and preferred term.
	 * @param conceptId
	 * @param englishPreferredTerm
	 * @return Fully Specified Name
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private String createEnglishDescriptions(final String conceptId,
			final String englishPreferredTerm) throws CreateSCTIDFaultException, DrugMatchConfigurationException, IOException {
		// English preferred term
		// including namespace ID and language code to avoid undesired collision
		String namespaceId = ComponentIdHelper.getNamespaceId(conceptId);
		String descriptionId = getExtensionDescriptionId(namespaceId,
				englishPreferredTerm,
				ReleaseFormat2.LANGUAGE_EN_CODE);
		exportDescription(conceptId,
				descriptionId,
				englishPreferredTerm,
				ReleaseFormat2.LANGUAGE_EN_CODE,
				ReleaseFormat2.DESCRIPTION_TYPE_PREFERRED_TERM_ID);
		exportPreferredToLanguageReferenceSet(descriptionId);
		// English fully specified name
		String englishFullySpecifiedName = englishPreferredTerm + " (product)";
		// A FSN is unambiguous and unique, source: Robert Turnbull (20140603, rtu@ihtsdo.org)
		// For a given namespace ID & language code combination (dleh, 20140604)
		descriptionId = getExtensionDescriptionId(namespaceId,
				englishFullySpecifiedName,
				ReleaseFormat2.LANGUAGE_EN_CODE);
		exportDescription(conceptId,
				descriptionId,
				englishFullySpecifiedName,
				ReleaseFormat2.LANGUAGE_EN_CODE,
				ReleaseFormat2.DESCRIPTION_TYPE_FULLY_SPECIFIED_NAME_ID);
		exportPreferredToLanguageReferenceSet(descriptionId);
		return englishFullySpecifiedName;
	}

	/**
	 * Create and export; generic pharmaceutical concept, descriptions and relationships.
	 * @param pharmaceutical
	 * @return SCT Concept ID
	 * @throws CreateConceptIdsFaultException
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private String createGenericConcept(final Pharmaceutical pharmaceutical) throws CreateConceptIdsFaultException, CreateSCTIDFaultException, DrugMatchConfigurationException, IOException {
		if (this.placeHolderConceptId == null) {
			createPlaceHolderConcept();
		}
		String conceptId = createConcept(pharmaceutical.getGenericUUID().toString(),
				this.placeHolderConceptId);
		String fullySpecifiedName = createEnglishAndNationalDescriptions(conceptId,
				pharmaceutical.getEnglishTerm(),
				pharmaceutical.getNationalTerm());
		createParentRelationship(conceptId,
				this.placeHolderConceptId);
		createAttributeRelationships(conceptId,
				pharmaceutical);
		reportCoreConcept(conceptId,
				fullySpecifiedName);
		return conceptId;
	}

	/**
	 * Create and export; national pharmaceutical concept, descriptions and relationships.
	 * @param pharmaceutical
	 * @param parentId
	 * @return SCT Concept ID
	 * @throws CreateConceptIdsFaultException
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private String createNationalConcept(final Pharmaceutical pharmaceutical,
			final String parentId) throws CreateConceptIdsFaultException, CreateSCTIDFaultException, DrugMatchConfigurationException, IOException {
		String conceptId = createConcept(pharmaceutical.getPharmaceuticalUUID().toString(),
				parentId);
		String fullySpecifiedName = createEnglishAndNationalDescriptions(conceptId,
				pharmaceutical.getEnglishPharmaceuticalTerm(),
				pharmaceutical.getNationalPharmaceuticalTerm());
		createParentRelationship(conceptId,
				parentId);
		createAttributeRelationships(conceptId,
				pharmaceutical);
		reportExtensionConcept(conceptId,
				fullySpecifiedName);
		return conceptId;
	}

	/**
	 * Create parent (main hierarchy) relationship.
	 * @param sourceId
	 * @param destinationId
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void createParentRelationship(final String sourceId,
			final String destinationId) throws CreateSCTIDFaultException, DrugMatchConfigurationException, IOException {
		String relationshipId = this.idService.getExtensionRelationshipId(getUUID(sourceId + ReleaseFormat2.RELATIONSHIP_TYPE_IS_A_ID + destinationId));
		exportRelationship(relationshipId,
				sourceId,
				destinationId,
				ReleaseFormat2.RELATIONSHIP_TYPE_IS_A_ID);
	}

	/**
	 * Create and export; DrugMatch placeholder concept, descriptions and relationships.
	 * @throws CreateConceptIdsFaultException
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void createPlaceHolderConcept() throws CreateConceptIdsFaultException, CreateSCTIDFaultException, DrugMatchConfigurationException, IOException {
		this.placeHolderConceptId = createConcept("9c261e4c-d25d-4dc2-84c2-a3227d5ffb1e", // UUID v4 (random, pulled from http://www.famkruithof.net/uuid/uuidgen )
				ReleaseFormat2.CONCEPT_PHARMACEUTICAL_OR_BIOLOGIC_PRODUCT_ID);
		String fullySpecifiedName = createEnglishDescriptions(this.placeHolderConceptId,
				"DrugMatch placeholder");
		// create parent relation
		createParentRelationship(this.placeHolderConceptId,
				ReleaseFormat2.CONCEPT_PHARMACEUTICAL_OR_BIOLOGIC_PRODUCT_ID);
		reportCoreConcept(this.placeHolderConceptId,
				fullySpecifiedName);
	}

	/**
	 * Validate {@link Pharmaceutical} attributes to determine if it's feasible to "Create" a SCT representation!
	 * @param pharmaceutical
	 * @return <code>true</code> if argument is eglible for "Create", otherwise <code>false</code>
	 */
	private boolean eglible(final Pharmaceutical pharmaceutical) {
		StringBuilder errors = new StringBuilder();
		// dose form
		Long doseFormId = (pharmaceutical.doseForm.nameNational == null) ? null : this.doseForm2Id.get(pharmaceutical.doseForm.nameNational);
		if (doseFormId == null) {
			doseFormId = (pharmaceutical.doseForm.nameEnglish == null) ? null : this.doseForm2Id.get(pharmaceutical.doseForm.nameEnglish);
		}
		if (doseFormId == null) {
			errors.append(" dose form [doseForm=");
			errors.append(pharmaceutical.doseForm);
			errors.append("],");
		}
		// components
		Long substanceId, unitId;
		for (Component component : pharmaceutical.components) {
			substanceId = (component.substance.nameNational == null) ? null : this.substance2Id.get(component.substance.nameNational);
			if (substanceId == null) {
				substanceId = (component.substance.nameEnglish == null) ? null : this.substance2Id.get(component.substance.nameEnglish);
				if (substanceId == null) {
					errors.append(" substance [substance=");
					errors.append(component.substance);
					errors.append("],");
				}
			}
			unitId = (component.unit == null) ? null : this.unit2Id.get(component.unit);
			if (unitId == null) {
				errors.append(" unit [unit=");
				errors.append(component.unit);
				errors.append("],");
			}
		}
		if (errors.length() > 0) {
			if (errors.charAt(errors.length() - 1) == ',') {
				errors.setCharAt(errors.length() - 1, '!');
			}
			log.debug("Skipping pharmaceutical [drugId={}], cause: missing{}",
					pharmaceutical.drugId,
					errors.toString());
			return false;
		} // else
		return true;
	}

	/**
	 * Execute DrugMatch "Create" and implicit dependency "Check" & "Match".
	 * @param matchAttributeReport
	 * @throws CreateConceptIdsFaultException
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws DrugMatchStrictModeViolationException
	 * @throws IOException
	 */
	public final void execute(final boolean matchAttributeReport) throws CreateConceptIdsFaultException, CreateSCTIDFaultException, DrugMatchConfigurationException, DrugMatchStrictModeViolationException, IOException {
		log.info("Starting \"Create\"");
		// implicit "Match" dependency
		this.match.execute(matchAttributeReport);
		// retrieve "Check" result
		this.doseForm2Id = this.match.getDoseForm2Id();
		this.substance2Id = this.match.getSubstance2Id();
		this.unit2Id = this.match.getUnit2Id();
		// SCT RF2
		Map<Pharmaceutical, String> pharmaceutical2ConceptId = generateReleaseFormat2();
		// Drug ID <-> SCT ID mapping
		exportMapping(pharmaceutical2ConceptId);
		log.info("Completed \"Create\"");
	}

	/**
	 * Export concept to SNOMED CT Release Format 2.
	 * @param conceptId
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void exportConcept(final String conceptId) throws DrugMatchConfigurationException, IOException {
		boolean addHeader = false;
		if (this.fileNameConcept == null) {
			this.fileNameConcept = DrugMatchProperties.getTerminologyDirectory().getPath() + File.separator + "sct2_Concept_DrugMatch_" + this.isoNow + ".txt";
			addHeader = true;
		}
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.fileNameConcept,
								!addHeader), // append
						CharEncoding.UTF_8),
				ReleaseFormat2.FILE_CONTENT_SEPARATOR_CHARACTER,
				CSVWriter.NO_QUOTE_CHARACTER,
				ReleaseFormat2.NEW_LINE)) {
			// header
			if (addHeader) {
				writer.writeNext(new String[] {
						"id",
						"effectiveTime",
						"active",
						"moduleId",
						"definitionStatusId"
				});
				log.info("Created {}", this.fileNameConcept);
			}
			// content
			writer.writeNext(new String[] {
					conceptId,
					this.effectiveTime,
					ReleaseFormat2.STATUS_ACTIVE_ID,
					DrugMatchProperties.getModuleId(),
					ReleaseFormat2.CONCEPT_DEFINITION_STATUS_PRIMITIVE_ID
			});
			writer.flush();
		}
	}

	/**
	 * Export description to SNOMED CT Release Format 2.
	 * @param conceptId
	 * @param descriptionId
	 * @param term
	 * @param languageCode
	 * @param typeId
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void exportDescription(final String conceptId,
			final String descriptionId,
			final String term,
			final String languageCode,
			final String typeId) throws DrugMatchConfigurationException, IOException {
		boolean addHeader = false;
		if (this.fileNameDescription == null) {
			this.fileNameDescription = DrugMatchProperties.getTerminologyDirectory().getPath() + File.separator + "sct2_Description_DrugMatch_" + this.isoNow + ".txt";
			addHeader = true;
		}
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.fileNameDescription,
								!addHeader), // append
						CharEncoding.UTF_8),
				ReleaseFormat2.FILE_CONTENT_SEPARATOR_CHARACTER,
				CSVWriter.NO_QUOTE_CHARACTER,
				ReleaseFormat2.NEW_LINE)) {
			// header
			if (addHeader) {
				writer.writeNext(new String[] {
						"id",
						"effectiveTime",
						"active",
						"moduleId",
						"conceptId",
						"languageCode",
						"typeId",
						"term",
						"caseSignificanceId"
				});
				log.info("Created {}", this.fileNameDescription);
			}
			// content
			writer.writeNext(new String[] {
					descriptionId,
					this.effectiveTime,
					ReleaseFormat2.STATUS_ACTIVE_ID,
					DrugMatchProperties.getModuleId(),
					conceptId,
					languageCode, // languageCode
					typeId,
					term,
					ReleaseFormat2.DESCRIPTION_CASE_SIGNIFICANCE_FALSE_ID
			});
			writer.flush();
		}
	}

	/**
	 * Export 1-1 mapping: Drug ID to SCT Concept ID.
	 * @param pharmaceutical2ConceptId
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void exportMapping(final Map<Pharmaceutical, String> pharmaceutical2ConceptId) throws DrugMatchConfigurationException, IOException {
		if (pharmaceutical2ConceptId.size() > 0) {
			log.info("Starting \"Create\" mapping export");
			String fullFileName = DrugMatchProperties.getMappingDirectory().getPath() + File.separator + "mapping_" + this.isoNow + ".csv";
			String quoteCharacter = DrugMatchProperties.getFileContentQuoteCharacter();
			char quoteChar = (quoteCharacter == null) ? CSVWriter.NO_QUOTE_CHARACTER : quoteCharacter.charAt(0);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fullFileName),
					CharEncoding.UTF_8),
					Check.getOutputFileContentSeparator(),
					quoteChar,
					System.lineSeparator())) {
				// header
				writer.writeNext(new String[] {
						"Drug ID",
						"SCT Concept ID"
				});
				// content
				for (Map.Entry<Pharmaceutical, String> entry : pharmaceutical2ConceptId.entrySet()) {
					writer.writeNext(new String[] {
							entry.getKey().drugId,
							entry.getValue()
					});
				}
				writer.flush();
				log.info("Created {}", fullFileName);
			}
			log.info("Completed \"Create\" mapping export");
		} else {
			log.debug("Skipping mapping export, cause: no data available");
		}
	}

	/**
	 * Export description to SNOMED CT Release Format 2 Language Reference Set.
	 * @param descriptionId
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void exportPreferredToLanguageReferenceSet(final String descriptionId) throws DrugMatchConfigurationException, IOException {
		boolean addHeader = false;
		if (this.fileNameReferenceSetLanguage == null) {
			this.fileNameReferenceSetLanguage = DrugMatchProperties.getReferenceSetLanguageDirectory().getPath() + File.separator + "der2_cRefset_Language_DrugMatch_" + this.isoNow + ".txt";
			addHeader = true;
		}
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.fileNameReferenceSetLanguage,
								!addHeader), // append
						CharEncoding.UTF_8),
				ReleaseFormat2.FILE_CONTENT_SEPARATOR_CHARACTER,
				CSVWriter.NO_QUOTE_CHARACTER,
				ReleaseFormat2.NEW_LINE)) {
			// header
			if (addHeader) {
				writer.writeNext(new String[] {
						"id",
						"effectiveTime",
						"active",
						"moduleId",
						"refsetId",
						"referencedComponentId",
						"acceptabilityId"
				});
				log.info("Created {}", this.fileNameReferenceSetLanguage);
			}
			// content
			writer.writeNext(new String[] {
					UUID.randomUUID().toString(), // UUID v4 as defined by Robert Turnbull (20140603, rtu@ihtsdo.org)
					this.effectiveTime,
					ReleaseFormat2.STATUS_ACTIVE_ID,
					DrugMatchProperties.getModuleId(),
					ReleaseFormat2.REFERENCE_SET_LANGUAGE_ID,
					descriptionId,
					ReleaseFormat2.META_DATA_ACCEPTABILITY_PREFERRED_ID
			});
			writer.flush();
		}
	}

	/**
	 * Export relationship to SNOMED CT Release Format 2.
	 * @param relationshipId
	 * @param sourceId
	 * @param destinationId
	 * @param typeId
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void exportRelationship(final String relationshipId,
			final String sourceId,
			final String destinationId,
			final String typeId) throws DrugMatchConfigurationException, IOException {
		boolean addHeader = false;
		if (this.fileNameRelationship == null) {
			this.fileNameRelationship = DrugMatchProperties.getTerminologyDirectory().getPath() + File.separator + "sct2_Relationship_DrugMatch_" + this.isoNow + ".txt";
			addHeader = true;
		}
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.fileNameRelationship,
								!addHeader), // append
						CharEncoding.UTF_8),
				ReleaseFormat2.FILE_CONTENT_SEPARATOR_CHARACTER,
				CSVWriter.NO_QUOTE_CHARACTER,
				ReleaseFormat2.NEW_LINE)) {
			// header
			if (addHeader) {
				writer.writeNext(new String[] {
						"id",
						"effectiveTime",
						"active",
						"moduleId",
						"sourceId",
						"destinationId",
						"relationshipGroup",
						"typeId",
						"characteristicTypeId",
						"modifierId"
				});
				log.info("Created {}", this.fileNameRelationship);
			}
			// content
			writer.writeNext(new String[] {
					relationshipId,
					this.effectiveTime,
					ReleaseFormat2.STATUS_ACTIVE_ID,
					DrugMatchProperties.getModuleId(),
					sourceId,
					destinationId,
					ReleaseFormat2.RELATIONSHIP_GROUP_NONE,
					typeId,
					ReleaseFormat2.RELATIONSHIP_CHARACTERISTIC_TYPE_DEFINING_ID,
					ReleaseFormat2.RELATIONSHIP_MODIFIER_ID
			});
			writer.flush();
		}
		exportStatedRelationship(relationshipId,
				sourceId,
				destinationId,
				typeId);
	}

	/**
	 * Export description to SNOMED CT Release Format 2 Quantity Reference Set.
	 * @param referencedComponentId
	 * @param conceptId
	 * @param number
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void exportRelationshipToQuantityReferenceSet(String referencedComponentId,
			Long conceptId,
			String number) throws DrugMatchConfigurationException, IOException {
		boolean addHeader = false;
		if (this.fileNameQuantityReferenceSet == null) {
			this.fileNameQuantityReferenceSet = DrugMatchProperties.getReferenceSetContentDirectory().getPath() + File.separator + "der2_ciRefset_QuantityReferenceSetConceptNumber_DrugMatch_" + this.isoNow + ".txt";
			addHeader = true;
		}
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.fileNameQuantityReferenceSet,
								!addHeader), // append
						CharEncoding.UTF_8),
				ReleaseFormat2.FILE_CONTENT_SEPARATOR_CHARACTER,
				CSVWriter.NO_QUOTE_CHARACTER,
				ReleaseFormat2.NEW_LINE)) {
			// header
			if (addHeader) {
				writer.writeNext(new String[] {
						"id",
						"effectiveTime",
						"active",
						"moduleId",
						"refSetId",
						"referencedComponentId",
						"conceptId",
						"number"
				});
				log.info("Created {}", this.fileNameQuantityReferenceSet);
			}
			// content
			writer.writeNext(new String[] {
					UUID.randomUUID().toString(), // UUID v4 as defined by Robert Turnbull (20140603, rtu@ihtsdo.org)
					this.effectiveTime,
					ReleaseFormat2.STATUS_ACTIVE_ID,
					DrugMatchProperties.getModuleId(),
					DrugMatchProperties.getQuantityReferenceSetId(),
					referencedComponentId,
					String.valueOf(conceptId),
					number
			});
			writer.flush();
		}
	}

	/**
	 * Export relationship to SNOMED CT Release Format 2.
	 * @param relationshipId
	 * @param sourceId
	 * @param destinationId
	 * @param typeId
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void exportStatedRelationship(final String relationshipId,
			final String sourceId,
			final String destinationId,
			final String typeId) throws DrugMatchConfigurationException, IOException {
		boolean addHeader = false;
		if (this.fileNameStatedRelationship == null) {
			this.fileNameStatedRelationship = DrugMatchProperties.getTerminologyDirectory().getPath() + File.separator + "sct2_StatedRelationship_DrugMatch_" + this.isoNow + ".txt";
			addHeader = true;
		}
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.fileNameStatedRelationship,
								!addHeader), // append
						CharEncoding.UTF_8),
				ReleaseFormat2.FILE_CONTENT_SEPARATOR_CHARACTER,
				CSVWriter.NO_QUOTE_CHARACTER,
				ReleaseFormat2.NEW_LINE)) {
			// header
			if (addHeader) {
				writer.writeNext(new String[] {
						"id",
						"effectiveTime",
						"active",
						"moduleId",
						"sourceId",
						"destinationId",
						"relationshipGroup",
						"typeId",
						"characteristicTypeId",
						"modifierId"
				});
				log.info("Created {}", this.fileNameStatedRelationship);
			}
			// content
			writer.writeNext(new String[] {
					relationshipId,
					this.effectiveTime,
					ReleaseFormat2.STATUS_ACTIVE_ID,
					DrugMatchProperties.getModuleId(),
					sourceId,
					destinationId,
					ReleaseFormat2.RELATIONSHIP_GROUP_NONE,
					typeId,
					ReleaseFormat2.RELATIONSHIP_CHARACTERISTIC_TYPE_STATED_ID,
					ReleaseFormat2.RELATIONSHIP_MODIFIER_ID
			});
			writer.flush();
		}
	}

	/**
	 * Generate SNOMED CT Release Format 2, for un-"Match"-ed {@link Pharmaceutical}s that "Check"s out.<br>
	 * Generates mapping as output, as the info is already available.
	 * @return {@link Map}({@link Pharmaceutical}, SCT Concept ID)
	 * @throws CreateConceptIdsFaultException
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private Map<Pharmaceutical, String> generateReleaseFormat2() throws CreateConceptIdsFaultException, CreateSCTIDFaultException, DrugMatchConfigurationException, IOException {
		log.info("Starting \"Create\" SNOMED CT Release Format 2 export & report");
		Map<Pharmaceutical, Pair<PharmaceuticalMatch, GenericMatch>> pharmaceutical2TermMatches = this.match.getPharmaceutical2TermMatches();
		Map<Pharmaceutical, String> result = new LinkedHashMap<>(pharmaceutical2TermMatches.size());
		Pair<PharmaceuticalMatch, GenericMatch> termMatch;
		String conceptId, parentId;
		for (Map.Entry<Pharmaceutical, Pair<PharmaceuticalMatch, GenericMatch>> entry : pharmaceutical2TermMatches
				.entrySet()) {
			termMatch = entry.getValue();
			if (termMatch != null) {
				// determine pharmaceutical match
				if (termMatch.getKey() == null) {
					conceptId = null;
				} else {
					if (MatchTermRule.PHARMACEUTICAL_EXACT_NATIONAL_MATCH.equals(termMatch.getKey().rule)
							|| MatchTermRule.PHARMACEUTICAL_CASE_INSENSITIVE_NATIONAL_MATCH.equals(termMatch.getKey().rule)
							|| MatchTermRule.PHARMACEUTICAL_INCORRECT_COMPONENT_ORDER_NATIONAL.equals(termMatch.getKey().rule)
							|| MatchTermRule.PHARMACEUTICAL_PARTIAL_TRADE_NAME_NATIONAL.equals(termMatch.getKey().rule)) {
						conceptId = (termMatch.getKey().descriptor == null) ? null : String.valueOf(termMatch.getKey().descriptor.conceptId);
					} else {
						conceptId = null;
						if (termMatch.getKey().descriptor != null) {
							log.debug("Unable to use \"Match\" for national pharmaceutical [drugId={}], cause: uneglible rule [rule={}]",
									entry.getKey().drugId,
									termMatch.getKey().rule);
						}
					}
				}
				if (conceptId == null
						&& eglible(entry.getKey())) {
					// determine if generic parent is matched
					if (MatchTermRule.GENERIC_EXACT_ENGLISH_MATCH.equals(termMatch.getValue().rule)
							|| MatchTermRule.GENERIC_EXACT_NATIONAL_MATCH.equals(termMatch.getValue().rule)
							|| MatchTermRule.GENERIC_CASE_INSENSITIVE_ENGLISH_MATCH.equals(termMatch.getValue().rule)
							|| MatchTermRule.GENERIC_CASE_INSENSITIVE_NATIONAL_MATCH.equals(termMatch.getValue().rule)
							|| MatchTermRule.GENERIC_PARTIAL_NATIONAL_DOSE_FORM.equals(termMatch.getValue().rule)
							|| MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_NATIONAL.equals(termMatch.getValue().rule)
							|| MatchTermRule.GENERIC_INCORRECT_COMPONENT_ORDER_ENGLISH.equals(termMatch.getValue().rule)
							|| MatchTermRule.GENERIC_PARTIAL_ENGLISH_DOSE_FORM.equals(termMatch.getValue().rule)) {
						parentId = (termMatch.getValue().descriptor == null) ? null : String.valueOf(termMatch.getValue().descriptor.conceptId);
					} else {
						parentId = null;
						if (termMatch.getValue().descriptor != null) {
							log.debug("Unable to use \"Match\" for generic pharmaceutical [drugId={}], cause: uneglible rule [rule={}]",
									entry.getKey().drugId,
									termMatch.getValue().rule);
						}
					}
					if (parentId == null) {
						parentId = createGenericConcept(entry.getKey());
					}
					conceptId = createNationalConcept(entry.getKey(), parentId);
				}
				if (conceptId != null) {
					result.put(entry.getKey(), conceptId);
				}
			}
		}
		log.info("Completed \"Create\" SNOMED CT Release Format 2 export & report");
		return result;
	}

	/**
	 * @param namespaceId
	 * @param term
	 * @param languageCode
	 * @return Extension Description ID
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws RemoteException
	 * @throws UnsupportedEncodingException
	 */
	private String getExtensionDescriptionId(String namespaceId,
			String term,
			String languageCode) throws CreateSCTIDFaultException, DrugMatchConfigurationException, RemoteException, UnsupportedEncodingException {
		return this.idService.getExtensionDescriptionId(
				getUUID(new StringBuilder(namespaceId)
						.append(term)
						.append(languageCode)
						.toString()));
	}

	/**
	 * Export concept to "Create" report.
	 * @param conceptId
	 * @param fullySpecifiedName
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void reportCoreConcept(final String conceptId,
			String fullySpecifiedName) throws DrugMatchConfigurationException, IOException {
		boolean addHeader = false;
		if (this.fileNameReportCoreConcept == null) {
			this.fileNameReportCoreConcept = DrugMatchProperties.getReportDirectory().getPath() + File.separator + "create_generic_pharmaceutical_" + this.isoNow + ".txt";
			addHeader = true;
		}
		String quoteCharacter = DrugMatchProperties.getFileContentQuoteCharacter();
		char quoteChar = (quoteCharacter == null) ? CSVWriter.NO_QUOTE_CHARACTER : quoteCharacter.charAt(0);
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.fileNameReportCoreConcept,
								!addHeader), // append
						CharEncoding.UTF_8),
				Check.getOutputFileContentSeparator(),
				quoteChar,
				System.lineSeparator())) {
			// header
			if (addHeader) {
				writer.writeNext(REPORT_HEADER);
				log.info("Created {}", this.fileNameReportCoreConcept);
			}
			// content
			writer.writeNext(new String[] {
					conceptId,
					fullySpecifiedName
			});
			writer.flush();
		}
	}

	/**
	 * Export concept to "Create" report.
	 * @param conceptId
	 * @param fullySpecifiedName
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private void reportExtensionConcept(final String conceptId,
			String fullySpecifiedName) throws DrugMatchConfigurationException, IOException {
		boolean addHeader = false;
		if (this.fileNameReportExtensionConcept == null) {
			this.fileNameReportExtensionConcept = DrugMatchProperties.getReportDirectory().getPath() + File.separator + "create_national_pharmaceutical_" + this.isoNow + ".txt";
			addHeader = true;
		}
		String quoteCharacter = DrugMatchProperties.getFileContentQuoteCharacter();
		char quoteChar = (quoteCharacter == null) ? CSVWriter.NO_QUOTE_CHARACTER : quoteCharacter.charAt(0);
		try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.fileNameReportExtensionConcept,
								!addHeader), // append
						CharEncoding.UTF_8),
				Check.getOutputFileContentSeparator(),
				quoteChar,
				System.lineSeparator())) {
			// header
			if (addHeader) {
				writer.writeNext(REPORT_HEADER);
				log.info("Created {}", this.fileNameReportExtensionConcept);
			}
			// content
			writer.writeNext(new String[] {
					conceptId,
					fullySpecifiedName
			});
			writer.flush();
		}
	}
}
