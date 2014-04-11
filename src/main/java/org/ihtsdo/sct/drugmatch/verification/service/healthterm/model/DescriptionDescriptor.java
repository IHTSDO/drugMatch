package org.ihtsdo.sct.drugmatch.verification.service.healthterm.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author dev-team@carecom.dk
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DescriptionDescriptor implements Serializable {

	public String descriptionLocale,
			descriptionTerm;

	public Long conceptId,
			descriptionId,
			descriptionType;

	@Override
	public boolean equals(Object obj) {
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
	 * @return extract extension namespace ID, if present, otherwise null.
	 */
	public String getNamespaceId() {
		if (this.descriptionId == null) {
			return null;
		} // else
		String id = this.descriptionId.toString();
		if (id.length() > 10) { // assuming extension ID
			int start = ((id.length() - 4) - 7) + 1;
			int end = start + 7;
			return id.substring(start, end);
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + ((this.descriptionId == null) ? 0 : this.descriptionId.hashCode());
	}

	@Override
	public String toString() {
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
