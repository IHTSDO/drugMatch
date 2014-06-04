package org.ihtsdo.sct.drugmatch.parser;

import java.io.IOException;
import java.util.List;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchStrictModeViolationException;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;

/**
 * @author dev-team@carecom.dk
 */
public interface Parser {

	/**
	 * @return {@link ParsingResult}
	 * @throws DrugMatchConfigurationException
	 * @throws DrugMatchStrictModeViolationException
	 * @throws IOException
	 */
	List<Pharmaceutical> parse() throws DrugMatchConfigurationException, DrugMatchStrictModeViolationException, IOException;
}
