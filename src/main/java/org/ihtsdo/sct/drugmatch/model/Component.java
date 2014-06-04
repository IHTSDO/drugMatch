package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * @author dev-team@carecom.dk
 */
public class Component implements Serializable {

	/**
	 * @see {@link Substance}
	 */
	public final Substance substance;

	/**
	 * National notation.
	 */
	public final String strength;

	/**
	 * SI.
	 */
	public final String unit;

	/**
	 * @param substanceNameEnglish
	 * @param substanceNameNational
	 * @param strength national notation
	 * @param unit SI
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

	/**
	 * Note, strength is converted to English.
	 * @return {@link Component#substance#nameEnglish} {@link Component#strength} {@link Component#unit}
	 * @see {@link Component#getStrengthEnglish(String)}
	 */
	public final String getEnglish() {
		return new StringBuilder(this.substance.getNormalizedNameEnglish())
			.append(" ")
			.append(getStrengthEnglish(this.strength))
			.append(" ")
			.append(this.unit)
			.toString();
	}

	/**
	 * @return {@link Component#substance#nameNational} {@link Component#strength} {@link Component#unit}
	 */
	public final String getNational() {
		return new StringBuilder(this.substance.getNormalizedNameNational())
			.append(" ")
			.append(this.strength)
			.append(" ")
			.append(this.unit)
			.toString();
	}

	/**
	 * Attempt to convert ISO decimal and thousands separator to English notation.<br>
	 * Skips ambiguous cases, ie. is limited to fraction with leading digits and thousands with decimal.
	 * @param nationalStrength
	 * @return national converted to English, if determined plausible, otherwise input is returned.
	 */
	public static String getStrengthEnglish(final String nationalStrength) {
		int indexOfComma = nationalStrength.indexOf(','),
			indexOfPeriod = nationalStrength.indexOf('.');
		if (indexOfComma > 0
				&& indexOfPeriod == -1
				&& StringUtils.countMatches(nationalStrength, ",") == 1) {
			// single ',' present without '.'
			String prefix = nationalStrength.substring(0, indexOfComma);
			if (prefix.matches("^\\d+$")) {
				// only leading zeroes
				return nationalStrength.replace(',', '.');
			}
		} else if (indexOfComma > 0
				&& indexOfPeriod > 0
				&& indexOfComma > indexOfPeriod
				&& indexOfComma < (nationalStrength.length() - 1)
				&& StringUtils.countMatches(nationalStrength, ",") == 1) {
			return nationalStrength.substring(0, indexOfComma).replace('.', ',') + "." + nationalStrength.substring(indexOfComma + 1);
		} // else
		return nationalStrength;
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
