package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * @author dev-team@carecom.dk
 */
public class DoseForm implements Comparable<DoseForm>, Serializable {

	public final String nameEnglish,
		nameNational;

	/**
	 * @param nameEnglish
	 * @param nameNational
	 */
	public DoseForm(final String nameEnglish,
			final String nameNational) {
		this.nameEnglish = nameEnglish;
		this.nameNational = nameNational;
	}

	public final int compareTo(final DoseForm other) {
		int result = this.nameEnglish.compareTo(other.nameEnglish);
		if (result == 0) {
			result = this.nameNational.compareTo(other.nameNational);
		}
		return result;
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
		DoseForm other = (DoseForm) obj;
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

	/**
	 * @return whitespace normalized {@link DoseForm#nameEnglish}
	 */
	public final String getNormalizedNameEnglish() {
		return StringUtils.normalizeSpace(this.nameEnglish);
	}

	/**
	 * @return whitespace normalized {@link DoseForm#nameNational}
	 */
	public final String getNormalizedNameNational() {
		return StringUtils.normalizeSpace(this.nameNational);
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = prime + ((this.nameEnglish == null) ? 0 : this.nameEnglish.hashCode());
		return prime * result + ((this.nameNational == null) ? 0 : this.nameNational.hashCode());
	}

	@Override
	public final String toString() {
		return new StringBuilder(DoseForm.class.getSimpleName())
			.append(" [nameEnglish=").append(this.nameEnglish)
			.append(", nameNational=").append(this.nameNational)
			.append(']')
			.toString();
	}
}
