package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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

	public final String getEnglishTerm() {
		StringBuilder term = new StringBuilder();
		int i = 0,
			l = this.components.size() - 1;
		for (Component component : this.components) {
			term.append((i > 0) ? StringUtils.uncapitalize(component.getEnglish()) : component.getEnglish());
			if (l > 0) {
				term.append(" + ");
			}
			i++;
			l--;
		}
		term.append(" ");
		term.append(StringUtils.uncapitalize(this.doseForm.nameEnglish));
		return term.toString();
	}

	public final String getNationalTerm() {
		StringBuilder term = new StringBuilder();
		int i = 0,
			l = this.components.size() - 1;
		for (Component component : this.components) {
			term.append((i > 0) ? StringUtils.uncapitalize(component.getNational()) : component.getNational());
			if (l > 0) {
				term.append(" + ");
			}
			i++;
			l--;
		}
		term.append(" ");
		term.append(StringUtils.uncapitalize(this.doseForm.nameNational));
		return term.toString();
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = prime + ((this.components == null) ? 0 : this.components.hashCode());
		result = prime * result + ((this.doseForm == null) ? 0 : this.doseForm.hashCode());
		result = prime * result + ((this.drugId == null) ? 0 : this.drugId.hashCode());
		return prime * result + ((this.tradeName == null) ? 0 : this.tradeName.hashCode());
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
