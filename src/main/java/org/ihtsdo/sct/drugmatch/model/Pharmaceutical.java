package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.sct.drugmatch.constant.Constant;

/**
 * @author dev-team@carecom.dk
 */
public class Pharmaceutical implements Serializable {

	/**
	 * Active components.
	 */
	public final List<Component> components;

	/**
	 * Dose form.
	 */
	public final DoseForm doseForm;

	/**
	 * National drug ID.
	 */
	public final String drugId;

	/**
	 * National trade name.
	 */
	public final String tradeName;

	/**
	 * @param components
	 * @param doseFormEnglish
	 * @param doseFormNational
	 * @param drugId
	 * @param tradeName
	 */
	public Pharmaceutical(final List<Component> components,
			final String doseFormEnglish,
			final String doseFormNational,
			final String drugId,
			final String tradeName) {
		this.components = components;
		this.doseForm = new DoseForm(doseFormEnglish,
				doseFormNational);
		this.drugId = drugId;
		this.tradeName = tradeName;
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
		Pharmaceutical other = (Pharmaceutical) obj;
		if (this.components == null) {
			if (other.components != null) {
				return false;
			}
		} else if (!this.components.equals(other.components)) {
			return false;
		}
		if (this.doseForm == null) {
			if (other.doseForm != null) {
				return false;
			}
		} else if (!this.doseForm.equals(other.doseForm)) {
			return false;
		}
		if (this.drugId == null) {
			if (other.drugId != null) {
				return false;
			}
		} else if (!this.drugId.equals(other.drugId)) {
			return false;
		}
		if (this.tradeName == null) {
			if (other.tradeName != null) {
				return false;
			}
		} else if (!this.tradeName.equals(other.tradeName)) {
			return false;
		}
		return true;
	}

	/**
	 * Ex. "National TradeName azathioprine 120 mg + codeine phosphate 12 mg oral dosage form"
	 * @return English pharmaceutical preferred term
	 * @see {@link #getEnglishTerm()}
	 */
	public final String getEnglishPharmaceuticalTerm() {
		return new StringBuilder(getNormalizedTradeName())
			.append(" ")
			.append(StringUtils.uncapitalize(getEnglishTerm()))
			.toString();
	}

	/**
	 * Ex. "Azathioprine 120 mg + codeine phosphate 12 mg oral dosage form"
	 * @return English generic preferred term
	 */
	public final String getEnglishTerm() {
		StringBuilder term = new StringBuilder();
		int i = 0,
			l = this.components.size() - 1;
		for (Component component : this.components) {
			term.append((i > 0) ? StringUtils.uncapitalize(component.getEnglish()) : StringUtils.capitalize(component.getEnglish()));
			if (l > 0) {
				term.append(" + ");
			}
			i++;
			l--;
		}
		term.append(" ");
		term.append(StringUtils.uncapitalize(this.doseForm.getNormalizedNameEnglish()));
		return term.toString();
	}

	/**
	 * Generate repeatable {@link UUID} based on English {@link Pharmaceutical#components} & English {@link Pharmaceutical#doseForm}.
	 * @return {@link UUID} v3
	 * @throws UnsupportedEncodingException
	 * @see {@link #nameUUIDFromList(List)}
	 */
	public final UUID getGenericUUID() throws UnsupportedEncodingException {
		// ensure whitespace doesn't affect resulting hash
		return nameUUIDFromList(Arrays.asList(getEnglishTerm().split(Constant.REGEX_WHITESPACE_GREEDY)));
	}

	/**
	 * Ex. "Solpadol paracetamol 120 mg + codeinphosphat 12 mg oral doseringsform"
	 * @return national pharmaceutical preferred term
	 * @see {@link #getNationalTerm()}
	 */
	public final String getNationalPharmaceuticalTerm() {
		return new StringBuilder(getNormalizedTradeName())
			.append(" ")
			.append(StringUtils.uncapitalize(getNationalTerm()))
			.toString();
	}

	/**
	 * Ex. "Paracetamol 120 mg + codeinphosphat 12 mg oral doseringsform"
	 * @return national preferred term
	 */
	public final String getNationalTerm() {
		StringBuilder term = new StringBuilder();
		int i = 0,
			l = this.components.size() - 1;
		for (Component component : this.components) {
			term.append((i > 0) ? StringUtils.uncapitalize(component.getNational()) : StringUtils.capitalize(component.getNational()));
			if (l > 0) {
				term.append(" + ");
			}
			i++;
			l--;
		}
		term.append(" ");
		term.append(StringUtils.uncapitalize(this.doseForm.getNormalizedNameNational()));
		return term.toString();
	}

	/**
	 * @return whitespace normalized {@link Pharmaceutical#tradeName}
	 */
	public final String getNormalizedTradeName() {
		return StringUtils.normalizeSpace(this.tradeName);
	}

	/**
	 * Generate repeatable {@link UUID} based on {@link Pharmaceutical#drugId}, {@link Pharmaceutical#tradeName}, national {@link Pharmaceutical#components} & national {@link Pharmaceutical#doseForm}.
	 * @return {@link UUID} v3
	 * @throws UnsupportedEncodingException
	 * @see {@link #nameUUIDFromList(List)}
	 */
	public final UUID getPharmaceuticalUUID() throws UnsupportedEncodingException {
		List<String> tokens = new ArrayList<>();
		// ensure whitespace doesn't affect resulting hash
		tokens.addAll(Arrays.asList(this.drugId.split(Constant.REGEX_WHITESPACE_GREEDY)));
		tokens.addAll(Arrays.asList(getEnglishPharmaceuticalTerm().split(Constant.REGEX_WHITESPACE_GREEDY)));
		return nameUUIDFromList(tokens);
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = prime + ((this.components == null) ? 0 : this.components.hashCode());
		result = prime * result + ((this.doseForm == null) ? 0 : this.doseForm.hashCode());
		result = prime * result + ((this.drugId == null) ? 0 : this.drugId.hashCode());
		return prime * result + ((this.tradeName == null) ? 0 : this.tradeName.hashCode());
	}

	/**
	 * Attempt to generate repeatable {@link UUID} for repeated calls with identical tokens, regardless of character case and order.
	 * @param tokens
	 * @return {@link UUID} v3
	 * @throws UnsupportedEncodingException
	 */
	private static UUID nameUUIDFromList(final List<String> tokens) throws UnsupportedEncodingException {
		// support random token order, intention is repeatable UUID
		Collections.sort(tokens);
		// generate bytes
		StringBuilder data = new StringBuilder();
		for (String token : tokens) {
			data.append(token);
		}
		return UUID.nameUUIDFromBytes(data.toString().toLowerCase(Locale.ENGLISH).getBytes(CharEncoding.UTF_8));
	}

	@Override
	public final String toString() {
		return new StringBuilder(Pharmaceutical.class.getSimpleName())
			.append(" [drugId=").append(this.drugId)
			.append(", tradeName=").append(this.tradeName)
			.append(", doseForm=").append(this.doseForm)
			.append(", components=").append(this.components)
			.append(']')
			.toString();
	}
}
