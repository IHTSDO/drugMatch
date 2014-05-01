package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;

/**
 * @author dev-team@carecom.dk
 */
public class Component implements Serializable {

	public final Substance substance;

	public final String strength,
		unit;

	/**
	 * @param substanceNameEnglish
	 * @param substanceNameNational
	 * @param strength
	 * @param unit
	 */
	public Component(final String substanceNameEnglish,
			final String substanceNameNational,
			final String strength,
			final String unit) {
		this.substance = new Substance(substanceNameEnglish,
				substanceNameNational);
		this.strength = strength;
		this.unit = unit;
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
		Component other = (Component) obj;
		if (this.strength == null) {
			if (other.strength != null) {
				return false;
			}
		} else if (!this.strength.equals(other.strength)) {
			return false;
		}
		if (this.substance == null) {
			if (other.substance != null) {
				return false;
			}
		} else if (!this.substance.equals(other.substance)) {
			return false;
		}
		if (this.unit == null) {
			if (other.unit != null) {
				return false;
			}
		} else if (!this.unit.equals(other.unit)) {
			return false;
		}
		return true;
	}

	public final String getEnglish() {
		return new StringBuilder(this.substance.nameEnglish)
			.append(" ")
			.append(this.strength)
			.append(" ")
			.append(this.unit)
			.toString();
	}

	public final String getNational() {
		return new StringBuilder(this.substance.nameNational)
			.append(" ")
			.append(this.strength)
			.append(" ")
			.append(this.unit)
			.toString();
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = prime + ((this.strength == null) ? 0 : this.strength.hashCode());
		result = prime * result + ((this.substance == null) ? 0 : this.substance.hashCode());
		return prime * result + ((this.unit == null) ? 0 : this.unit.hashCode());
	}

	@Override
	public final String toString() {
		return new StringBuilder(Component.class.getSimpleName())
			.append(" [substance=").append(this.substance)
			.append(", strength=").append(this.strength)
			.append(", unit=").append(this.unit)
			.append(']')
			.toString();
	}
}
