package org.ihtsdo.sct.drugmatch.util;

import org.ihtsdo.sct.drugmatch.constant.rf2.ReleaseFormat2;

/**
 * @author dev-team@carecom.dk
 */
public final class ComponentIdHelper {

	/**
	 * DON'T INSTANTIATE A STATIC HELPER!
	 */
	private ComponentIdHelper() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Minimum length (Extension namespace ID(7) + Partition ID(2) + checksum(1)).
	 */
	private static final int COMPONENT_EXTENSION_MINIMUM_LENGTH = 10;

	private static final char PARTITION_EXTENSION_PREFIX_ID = '1';

	/**
	 * An extension namespace ID will always consist of 7 digits (range: 0000000 - 9999999)
	 * @param componentId
	 * @return namespace ID for a component by separating part of the ID
	 *         (range: 0000000 - 9999999), a String is used to support leading
	 *         zeros. See SNOMED CT Technical Reference Guide, section 'SCTID's
	 *         and Extensions', or <code>null</code>.
	 */
	public static String getNamespaceId(final String componentId) {
		if (componentId == null) {
			return null;
		} // else
		if (componentId.length() > COMPONENT_EXTENSION_MINIMUM_LENGTH
				&& PARTITION_EXTENSION_PREFIX_ID == componentId.charAt(componentId.length() - 3)) {
			int start = ((componentId.length() - 4) - 7) + 1;
			int end = start + 7;
			return componentId.substring(start, end);
		}
		return ReleaseFormat2.NAMESPACE_CORE_ID;
	}
}
