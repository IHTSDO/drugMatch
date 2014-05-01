package org.ihtsdo.sct.drugmatch;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ihtsdo.sct.drugmatch.check.Check;
import org.ihtsdo.sct.drugmatch.enumeration.ReturnCode;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchInputParsingException;
import org.ihtsdo.sct.drugmatch.match.Match;
import org.ihtsdo.sct.drugmatch.model.ParsingResult;
import org.ihtsdo.sct.drugmatch.parser.impl.CSVParser;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.verification.service.VerificationService;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.impl.VerificationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * @author dev-team@carecom.dk
 */
public class DrugMatch {

	private static final Logger log = LoggerFactory.getLogger(DrugMatch.class);

	@Parameter(names = { "-c", "--check" }, description = "Execute \"Check\"")
	private boolean check = false;

	@Parameter(names = { "-m", "--match" }, description = "Execute \"Match\" (implies \"Check\")")
	private boolean match = false;

	@Parameter(names = { "--matchAttributeReport" }, description = "Generate separate \"Match\" attribute report", hidden = true)
	private boolean matchAttributeReport = false;

	private final String isoNow;

	public DrugMatch() {
		// store start time
		this.isoNow = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());
	}

	public final void execute() throws DrugMatchConfigurationException, DrugMatchInputParsingException, IOException, KeyManagementException, NoSuchAlgorithmException {
		log.info("Starting DrugMatch flow");
		ParsingResult parsingResult = new CSVParser().parse();
		// strict mode check
		if (DrugMatchProperties.isStrictMode()
				&& parsingResult.parseCount != parsingResult.pharmaceuticals.size()) {
			throw new DrugMatchInputParsingException("FLOW ABORTED, CAUSE: STRICT MODE, WARNINGS DETECTED DURING PARSING: " +
				parsingResult.parseCount +" PARSED " +
				parsingResult.pharmaceuticals.size() + " VALID!");
		} // else
		// determine flow scope
		this.match = (!this.check || this.match);
		// start flow
// TODO strict mode check
		VerificationService verificationService = new VerificationServiceImpl();
		if (this.match) {
			// match
			Match m = new Match(parsingResult.pharmaceuticals,
					this.isoNow,
					verificationService);
			m.execute(this.matchAttributeReport);
		} else {
			// check
			Check c = new Check(parsingResult.pharmaceuticals,
					this.isoNow,
					verificationService);
			c.execute();
		}
		// create
		log.info("Completed DrugMatch flow");
	}

	public static void main(final String[] args) {
		JCommander jc = new JCommander();
		try {
			DrugMatch dm = new DrugMatch();
			jc.addObject(dm);
			jc.parse(args);
			dm.execute();
		} catch (DrugMatchInputParsingException e) {
			log.error(e.getMessage());
			System.exit(ReturnCode.INPUT_PARSE_ERROR.getValue());
		} catch (DrugMatchConfigurationException | IOException e) {
			log.error(e.getMessage());
			System.exit(ReturnCode.CONFIGURATION_ERROR.getValue());
		} catch (ParameterException e) {
			log.debug("Error encountered", e);
			jc.usage();
			System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
		} catch (Exception e) {
			log.error("Error encountered", e);
			System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
		}
	}
}
