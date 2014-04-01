package org.ihtsdo.sct.drugmatch.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author dev-team@carecom.dk
 *
 */
public class ParsingResult implements Serializable {

	/**
	 * Number representing the amount of {@link Pharmaceutical}s parse attempts.
	 * Will differ from result size if malformed content was encountered during parsing.
	 */
	public final int parseCount;

	public final List<Pharmaceutical> pharmaceuticals;

	/**
	 * @param lineCount
	 * @param pharmaceuticals
	 */
	public ParsingResult(int lineCount,
			List<Pharmaceutical> pharmaceuticals) {
		this.parseCount = lineCount;
		this.pharmaceuticals = pharmaceuticals;
	}

	@Override
	public String toString() {
		return new StringBuilder(ParsingResult.class.getSimpleName())
			.append(" [parseCount=").append(this.parseCount)
			.append(", pharmaceuticals=").append(this.pharmaceuticals)
			.append(']')
			.toString();
	}
}
