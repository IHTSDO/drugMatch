package org.ihtsdo.sct.drugmatch.match;

import java.io.Serializable;
import java.util.Comparator;

import org.ihtsdo.sct.drugmatch.enumeration.DescriptionType;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.DescriptionDescriptor;

/**
 * @author dev-team@carecom.dk
 */
public class DescriptionDescriptorTypeComparator implements Comparator<DescriptionDescriptor>, Serializable {

	/**
	 * If the {@link DescriptionDescriptor#descriptionId}s differ, the following {@link DescriptionType} order is enforced:
	 * <ul>
	 * <li>{@link DescriptionType#PREFERRED_TERM}</li>
	 * <li>{@link DescriptionType#FULLY_SPECIFIED_NAME}</li>
	 * <li>{@link DescriptionType#SYNONYM}</li>
	 * <li>{@link DescriptionType#UNSPECIFIED}</li>
	 * </ul>
	 * <br>
	 * If the {@link DescriptionType}s are equal, the ID is used as fall back.
	 * @param d1
	 * @param d2
	 * @return see list above
	 */
	public final int compare(final DescriptionDescriptor d1,
			final DescriptionDescriptor d2) {
		int result = d1.descriptionId.compareTo(d2.descriptionId);
		if (result != 0) {
			if (d1.descriptionType.intValue() == DescriptionType.PREFERRED_TERM.getId()
					&& d1.descriptionType.intValue() != d2.descriptionType.intValue()) {
				result = -1;
			} else if (d2.descriptionType.intValue() == DescriptionType.PREFERRED_TERM.getId()
					&& d1.descriptionType.intValue() > DescriptionType.PREFERRED_TERM.getId()) {
				result = d1.descriptionType.compareTo(d2.descriptionType);
			} else {
				result = d2.descriptionType.compareTo(d1.descriptionType);
			}
			if (result == 0) {
				result = d1.descriptionId.compareTo(d2.descriptionId);
			}
		}
		return result;
	}
}
