package org.ihtsdo.sct.drugmatch.verification.service.healthterm.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author dev-team@carecom.dk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConceptSearchResultDescriptor implements Serializable {

	public String conceptCode,
			descriptionTerm;

	public Long healthtermConceptId,
			healthtermDescriptionId,
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
		ConceptSearchResultDescriptor other = (ConceptSearchResultDescriptor) obj;
		if (this.healthtermConceptId == null) {
			if (other.healthtermConceptId != null) {
				return false;
			}
		} else if (!this.healthtermConceptId.equals(other.healthtermConceptId)) {
			return false;
		}
		if (this.healthtermDescriptionId == null) {
			if (other.healthtermDescriptionId != null) {
				return false;
			}
		} else if (!this.healthtermDescriptionId
				.equals(other.healthtermDescriptionId)) {
			return false;
		}
		return true;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = prime + ((this.healthtermConceptId == null) ? 0 : this.healthtermConceptId.hashCode());
		return prime * result + ((this.healthtermDescriptionId == null) ? 0 : this.healthtermDescriptionId.hashCode());
	}

	@Override
	public final String toString() {
		return new StringBuilder(ConceptSearchResultDescriptor.class.getSimpleName())
			.append(" [conceptCode=").append(this.conceptCode)
			.append(", descriptionTerm=").append(this.descriptionTerm)
			.append(", healthtermConceptId=").append(this.healthtermConceptId)
			.append(", healthtermDescriptionId=").append(this.healthtermDescriptionId)
			.append(", descriptionType=").append(this.descriptionType)
			.append(']')
			.toString();
	}
}
