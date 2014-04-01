package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;

/**
 * @author dev-team@carecom.dk
 *
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
	public Component(String substanceNameEnglish,
			String substanceNameNational,
			String strength,
			String unit) {
		this.substance = new Substance(substanceNameEnglish,
				substanceNameNational);
		this.strength = strength;
		this.unit = unit;
	}

	@Override
	public String toString() {
		return new StringBuilder(Component.class.getSimpleName())
			.append(" [substance=").append(this.substance)
			.append(", strength=").append(this.strength)
			.append(", unit=").append(this.unit)
			.append(']')
			.toString();
	}
}
