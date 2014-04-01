/**
 * 
 */
package org.ihtsdo.sct.drugmatch.verification.service.healthterm.model;

import java.io.Serializable;

/**
 * @author dev-team@carecom.dk
 *
 */
public class LogEntry implements Serializable {

	public String level,
		code,
		cause,
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
		LogEntry other = (LogEntry) obj;
		if (this.cause == null) {
			if (other.cause != null) {
				return false;
			}
		} else if (!this.cause.equals(other.cause)) {
			return false;
		}
		if (this.code == null) {
			if (other.code != null) {
				return false;
			}
		} else if (!this.code.equals(other.code)) {
			return false;
		}
		if (this.level == null) {
			if (other.level != null) {
				return false;
			}
		} else if (!this.level.equals(other.level)) {
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
		int result = prime + ((this.cause == null) ? 0 : this.cause.hashCode());
		result = prime * result + ((this.code == null) ? 0 : this.code.hashCode());
		result = prime * result + ((this.level == null) ? 0 : this.level.hashCode());
		return prime * result + ((this.value == null) ? 0 : this.value.hashCode());
	}

	@Override
	public String toString() {
		return new StringBuilder(LogEntry.class.getSimpleName())
			.append(" [level=").append(this.level)
			.append(", code=").append(this.code)
			.append(", cause=").append(this.cause)
			.append(", value=").append(this.value)
			.append(']')
			.toString();
	}
}
