package org.ihtsdo.sct.drugmatch.match.model;

import java.io.Serializable;

import org.ihtsdo.sct.drugmatch.match.MatchTermRule;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.DescriptionDescriptor;

/**
 * National "Match".
 * @author dev-team@carecom.dk
 */
public class PharmaceuticalMatch implements Serializable {

	public final DescriptionDescriptor descriptor;

	public final MatchTermRule rule;

	/**
	 * @param descriptor
	 * @param rule
	 */
	public PharmaceuticalMatch(final DescriptionDescriptor descriptor,
			final MatchTermRule rule) {
		super();
		this.descriptor = descriptor;
		this.rule = rule;
	}

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
		PharmaceuticalMatch other = (PharmaceuticalMatch) obj;
		if (this.descriptor == null) {
			if (other.descriptor != null) {
				return false;
			}
		} else if (!this.descriptor.equals(other.descriptor)) {
			return false;
		}
		if (this.rule != other.rule) {
			return false;
		}
		return true;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = prime + ((this.descriptor == null) ? 0 : this.descriptor.hashCode());
		return prime * result + ((this.rule == null) ? 0 : this.rule.hashCode());
	}

	@Override
	public final String toString() {
		return new StringBuilder(PharmaceuticalMatch.class.getSimpleName())
			.append(" [descriptor=").append(this.descriptor)
			.append(", rule=").append(this.rule)
			.append(']')
			.toString();
	}
}
