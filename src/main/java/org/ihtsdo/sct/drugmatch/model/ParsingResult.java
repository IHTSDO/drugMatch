package org.ihtsdo.sct.drugmatch.model;

import java.util.List;

/**
 * @author dev-team@carecom.dk
 */
public class ParsingResult {

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
	public ParsingResult(final int lineCount,
			final List<Pharmaceutical> pharmaceuticals) {
		this.parseCount = lineCount;
		this.pharmaceuticals = pharmaceuticals;
	}

	@Override
	public final String toString() {
		return new StringBuilder(ParsingResult.class.getSimpleName())
			.append(" [parseCount=").append(this.parseCount)
			.append(", pharmaceuticals=").append(this.pharmaceuticals)
			.append(']')
			.toString();
	}
}
