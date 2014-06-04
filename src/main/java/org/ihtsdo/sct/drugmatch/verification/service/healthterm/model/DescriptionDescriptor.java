package org.ihtsdo.sct.drugmatch.verification.service.healthterm.model;

import java.io.Serializable;

import org.ihtsdo.sct.drugmatch.util.ComponentIdHelper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author dev-team@carecom.dk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DescriptionDescriptor implements Serializable {

	public String descriptionLocale,
			descriptionTerm;

	public Long conceptId,
			descriptionId,
			descriptionType;

	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DescriptionDescriptor other = (DescriptionDescriptor) obj;
		if (this.descriptionId == null) {
			if (other.descriptionId != null) {
				return false;
			}
		} else if (!this.descriptionId.equals(other.descriptionId)) {
			return false;
		}
		return true;
	}

	/**
	 * @return namespace ID, if present, otherwise null.
	 * @see {@link ComponentIdHelper#getNamespaceId(String)}
	 */
	public final String getNamespaceId() {
		return (this.descriptionId == null) ? null : ComponentIdHelper.getNamespaceId(this.descriptionId.toString());
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		return prime + ((this.descriptionId == null) ? 0 : this.descriptionId.hashCode());
	}

	@Override
	public final String toString() {
		return new StringBuilder(DescriptionDescriptor.class.getSimpleName())
			.append(" [descriptionLocale=").append(this.descriptionLocale)
			.append(", descriptionTerm=").append(this.descriptionTerm)
			.append(", conceptId=").append(this.conceptId)
			.append(", descriptionId=").append(this.descriptionId)
			.append(", descriptionType=").append(this.descriptionType)
			.append(']')
			.toString();
	}
}
