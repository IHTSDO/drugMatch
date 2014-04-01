package org.ihtsdo.sct.drugmatch.verification.service.healthterm.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author dev-team@carecom.dk
 *
 */
public class ConceptSearchResultDescriptor implements Serializable {

	public String conceptCode,
			conceptFullySpecifiedName,
			conceptHierarchy,
			conceptNamespaceId,
			conceptNamespaceName,
			
			descriptionNamespaceId,
			descriptionNamespaceName,
			descriptionTerm;

	public Long healthtermConceptId,
			healthtermDescriptionId,
			descriptionType;

	public List<KeyValueDescriptor> keyValueDescriptors;

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
	public int hashCode() {
		final int prime = 31;
		int result = prime + ((this.healthtermConceptId == null) ? 0 : this.healthtermConceptId.hashCode());
		return prime * result + ((this.healthtermDescriptionId == null) ? 0 : this.healthtermDescriptionId.hashCode());
	}

	@Override
	public String toString() {
		return new StringBuilder(ConceptSearchResultDescriptor.class.getSimpleName())
			.append(" [conceptCode=").append(this.conceptCode)
			.append(", conceptFullySpecifiedName=").append(this.conceptFullySpecifiedName)
			.append(", conceptHierarchy=").append(this.conceptHierarchy)
			.append(", conceptNamespaceId=").append(this.conceptNamespaceId)
			.append(", conceptNamespaceName=").append(this.conceptNamespaceName)
			.append(", descriptionNamespaceId=").append(this.descriptionNamespaceId)
			.append(", descriptionNamespaceName=").append(this.descriptionNamespaceName)
			.append(", descriptionTerm=").append(this.descriptionTerm)
			.append(", healthtermConceptId=").append(this.healthtermConceptId)
			.append(", healthtermDescriptionId=").append(this.healthtermDescriptionId)
			.append(", descriptionType=").append(this.descriptionType)
			.append(", keyValueDescriptors=").append(this.keyValueDescriptors)
			.append(']')
			.toString();
	}
}
