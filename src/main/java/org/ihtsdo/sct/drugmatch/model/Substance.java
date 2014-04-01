package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;

/**
 * @author dev-team@carecom.dk
 *
 */
public class Substance implements Comparable<Substance>, Serializable {

	public final String nameEnglish,
		nameNational;

	/**
	 * @param nameEnglish
	 * @param nameNational
	 */
	public Substance(String nameEnglish,
			String nameNational) {
		this.nameEnglish = nameEnglish;
		this.nameNational = nameNational;
	}

	public int compareTo(Substance other) {
		int result = this.nameEnglish.compareTo(other.nameEnglish);
		if (result == 0) {
			result = this.nameNational.compareTo(other.nameNational);
		}
		return result;
	}

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
		Substance other = (Substance) obj;
		if (this.nameEnglish == null) {
			if (other.nameEnglish != null) {
				return false;
			}
		} else if (!this.nameEnglish.equals(other.nameEnglish)) {
			return false;
		}
		if (this.nameNational == null) {
			if (other.nameNational != null) {
				return false;
			}
		} else if (!this.nameNational.equals(other.nameNational)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + ((this.nameEnglish == null) ? 0 : this.nameEnglish.hashCode());
		return prime * result + ((this.nameNational == null) ? 0 : this.nameNational.hashCode());
	}

	@Override
	public String toString() {
		return new StringBuilder(Substance.class.getSimpleName())
			.append(" [nameEnglish=").append(this.nameEnglish)
			.append(", nameNational=").append(this.nameNational)
			.append(']')
			.toString();
	}
}
