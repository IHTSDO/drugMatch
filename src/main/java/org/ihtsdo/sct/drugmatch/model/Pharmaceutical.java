package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author dev-team@carecom.dk
 *
 */
public class Pharmaceutical implements Serializable {

	public final List<Component> components;

	/**
	 * English dose form
	 */
	public final String doseForm;

	/**
	 * National drug ID
	 */
	public final String drugId;

	/**
	 * National trade name
	 */
	public final String tradeName;

	/**
	 * @param components
	 * @param doseForm
	 * @param drugId
	 * @param tradeName
	 */
	public Pharmaceutical(List<Component> components,
			String doseForm,
			String drugId,
			String tradeName) {
		this.components = components;
		this.doseForm = doseForm;
		this.drugId = drugId;
		this.tradeName = tradeName;
	}

	public String getEnglishTerm() {
		StringBuilder term = new StringBuilder();
		int i = this.components.size() - 1;
		for (Component component : this.components) {
			term.append(component.getEnglish());
			if (i > 0) {
				term.append(" + ");
			}
			i--;
		}
		term.append(" ");
		term.append(this.doseForm);
		return term.toString();
	}

	public String getNationalTerm() {
		StringBuilder term = new StringBuilder();
		int i = this.components.size() - 1;
		for (Component component : this.components) {
			term.append(component.getNational());
			if (i > 0) {
				term.append(" + ");
			}
			i--;
		}
		term.append(" ");
		term.append(this.doseForm);
		return term.toString();
	}

	@Override
	public String toString() {
		return new StringBuilder(Pharmaceutical.class.getSimpleName())
			.append(" [drugId=").append(this.drugId)
			.append(", tradeName=").append(this.tradeName)
			.append(", doseForm=").append(this.doseForm)
			.append(", components=").append(this.components)
			.append(']')
			.toString();
	}
}
