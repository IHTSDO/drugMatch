package org.ihtsdo.sct.drugmatch.parser;

import java.io.IOException;

import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.model.ParsingResult;

/**
 * @author dev-team@carecom.dk
 *
 */
public interface Parser {

	ParsingResult parse() throws DrugMatchConfigurationException, IOException;
}