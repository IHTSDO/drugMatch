package org.ihtsdo.sct.drugmatch.verification.service.healthterm.model;

import java.io.Serializable;

/**
 * @author dev-team@carecom.dk
 *
 */
public class KeyValueDescriptor implements Serializable {

	public String key,
		value;

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
		KeyValueDescriptor other = (KeyValueDescriptor) obj;
		if (this.key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!this.key.equals(other.key)) {
			return false;
		}
		if (this.value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!this.value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + ((this.key == null) ? 0 : this.key.hashCode());
		return prime * result + ((this.value == null) ? 0 : this.value.hashCode());
	}

	@Override
	public String toString() {
		return new StringBuilder(KeyValueDescriptor.class.getSimpleName())
			.append(" [key=").append(this.key)
			.append(", value=").append(this.value)
			.append(']')
			.toString();
	}
}
