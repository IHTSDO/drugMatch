package org.ihtsdo.sct.drugmatch.constant.rf2;

/**
 * Constants for DrugMatch SNOMED CT Release Format 2.
 * @author dev-team@carecom.dk
 */
public final class ReleaseFormat2 {

	/**
	 * DON'T INSTANTIATE A STATIC HELPER!
	 */
	private ReleaseFormat2() {
		throw new UnsupportedOperationException();
	}

	public static final String CONCEPT_DEFINITION_STATUS_PRIMITIVE_ID = "900000000000074008";

	/**
	 * "Pharmaceutical / biologic product (product)".
	 */
	public static final String CONCEPT_PHARMACEUTICAL_OR_BIOLOGIC_PRODUCT_ID = "373873005";

	public static final String DESCRIPTION_CASE_SIGNIFICANCE_FALSE_ID = "900000000000020002";

	public static final String DESCRIPTION_TYPE_FULLY_SPECIFIED_NAME_ID = "900000000000003001",
			DESCRIPTION_TYPE_PREFERRED_TERM_ID = "900000000000013009";

	/**
	 * Tab.
	 */
	public static final char FILE_CONTENT_SEPARATOR_CHARACTER = '\t';

	public static final String LANGUAGE_EN_CODE = "en";

	public static final String META_DATA_ACCEPTABILITY_PREFERRED_ID = "900000000000548007",

			META_DATA_ATTRIBUTE_CONCEPT_ID = "900000000000461009",
			META_DATA_ATTRIBUTE_DESCRIPTION_IN_DIALECT_ID = "900000000000510002",

			META_DATA_COMPONENT_TYPE_CONCEPT_ID = "900000000000461009",
			META_DATA_COMPONENT_TYPE_DESCRIPTION_ID = "900000000000462002";

	public static final String NAMESPACE_CORE_ID = "0";

	public static final String PARTITION_EXTENSION_CONCEPT_ID = "10",
			PARTITION_EXTENSION_DESCRIPTION_ID = "11",
			PARTITION_EXTENSION_RELATIONSHIP_ID = "12";

	public static final String REFERENCE_SET_LANGUAGE_ID = "900000000000509000";

	public static final String RELATIONSHIP_CHARACTERISTIC_TYPE_DEFINING_ID = "900000000000011006",
			RELATIONSHIP_CHARACTERISTIC_TYPE_STATED_ID = "900000000000010007";

	/**
	 * Ungrouped.
	 */
	public static final String RELATIONSHIP_GROUP_NONE = "0";

	public static final String RELATIONSHIP_MODIFIER_ID = "900000000000451002";

	/**
	 * "Is a (attribute)".
	 */
	public static final String RELATIONSHIP_TYPE_IS_A_ID = "116680003";

	public static final String STATUS_ACTIVE_ID = "1";
}
