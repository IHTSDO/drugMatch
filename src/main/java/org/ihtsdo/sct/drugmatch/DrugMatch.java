package org.ihtsdo.sct.drugmatch;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ihtsdo.sct.drugmatch.check.Check;
import org.ihtsdo.sct.drugmatch.enumeration.ReturnCode;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.model.ParsingResult;
import org.ihtsdo.sct.drugmatch.parser.impl.CSVParser;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dev-team@carecom.dk
 * 
 */
public class DrugMatch {

	private static final Logger log = LoggerFactory.getLogger(DrugMatch.class);

	private final DrugMatchProperties drugMatchProperties = new DrugMatchProperties();

	private final String isoNow;

	public DrugMatch() {
		// store start time
		this.isoNow = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());
	}

	public void execute() throws DrugMatchConfigurationException, IOException, KeyManagementException, NoSuchAlgorithmException {
		log.info("Starting DrugMatch flow");
		ParsingResult parsingResult = new CSVParser().parse();
		// strict mode check
		if (this.drugMatchProperties.isStrictMode()
				&& parsingResult.parseCount != parsingResult.pharmaceuticals.size()) {
			log.error("FLOW ABORTED, CAUSE: STRICT MODE, WARNINGS DETECTED DURING PARSING: {} PARSED {} VALID!",
					parsingResult.parseCount,
					parsingResult.pharmaceuticals.size());
			System.exit(ReturnCode.INPUT_PARSE_ERROR.getValue());
		} // else
		// check
		new Check(this.drugMatchProperties,
				parsingResult.pharmaceuticals,
				this.isoNow).execute();
		// match
		
		// create
		
		log.info("Completed DrugMatch flow");
	}

	public static void main(String[] args) {
		try {
			new DrugMatch().execute();
		} catch (DrugMatchConfigurationException | IOException e) {
			log.error(e.getMessage());
			System.exit(ReturnCode.CONFIGURATION_ERROR.getValue());
		} catch (Exception e) {
			log.error("Error encountered", e);
			System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
		}
	}
}
