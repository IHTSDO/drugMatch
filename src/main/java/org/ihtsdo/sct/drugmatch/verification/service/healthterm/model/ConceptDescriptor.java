package org.ihtsdo.sct.drugmatch.verification.service.healthterm.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author dev-team@carecom.dk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConceptDescriptor implements Serializable {

	public String hierarchy;

	public Long id;

	public List<DescriptionDescriptor> descriptionDescriptor;

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
		ConceptDescriptor other = (ConceptDescriptor) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		return prime + ((this.id == null) ? 0 : this.id.hashCode());
	}

	@Override
	public final String toString() {
		return new StringBuilder(ConceptDescriptor.class.getSimpleName())
			.append(" [id=").append(this.id)
			.append(", hierarchy=").append(this.hierarchy)
			.append(", descriptionDescriptor=").append(this.descriptionDescriptor)
			.append(']')
			.toString();
	}
}
