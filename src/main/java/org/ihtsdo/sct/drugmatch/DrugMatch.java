package org.ihtsdo.sct.drugmatch;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.ihtsdo.sct.drugmatch.check.Check;
import org.ihtsdo.sct.drugmatch.constant.ReturnCode;
import org.ihtsdo.sct.drugmatch.create.Create;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchStrictModeViolationException;
import org.ihtsdo.sct.drugmatch.id.service.impl.IdServiceImpl;
import org.ihtsdo.sct.drugmatch.match.Match;
import org.ihtsdo.sct.drugmatch.model.Pharmaceutical;
import org.ihtsdo.sct.drugmatch.parser.impl.CSVParser;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.impl.VerificationServiceImpl;
import org.ihtsdo.sct.id.service.CreateConceptIdsFaultException;
import org.ihtsdo.sct.id.service.CreateSCTIDFaultException;
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

	/**
	 * YYYY-MM-DD HH.MM.SS.
	 */
	private final String isoNow;

	public DrugMatch() {
		// store start time
		this.isoNow = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());
	}

	/**
	 * Execute DrugMatch flow.
	 * @throws CreateConceptIdsFaultException
	 * @throws CreateSCTIDFaultException
	 * @throws DrugMatchConfigurationException
	 * @throws DrugMatchStrictModeViolationException
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	public final void execute() throws CreateConceptIdsFaultException, CreateSCTIDFaultException, DrugMatchConfigurationException, DrugMatchStrictModeViolationException, IOException, KeyManagementException, NoSuchAlgorithmException {
		log.info("Starting DrugMatch flow");
		List<Pharmaceutical> pharmaceuticals = new CSVParser().parse();
		if (this.check) {
			// "Check"
			Check c = new Check(pharmaceuticals,
					this.isoNow,
					new VerificationServiceImpl());
			c.execute();
		} else if (this.match) {
			// "Match"
			Match m = new Match(pharmaceuticals,
					this.isoNow,
					new VerificationServiceImpl());
			m.execute(this.matchAttributeReport);
		} else {
			// "Create"
			Create create = new Create(pharmaceuticals,
					new IdServiceImpl(),
					this.isoNow,
					new VerificationServiceImpl());
			create.execute(this.matchAttributeReport);
		}
		log.info("Completed DrugMatch flow");
	}

	/**
	 * DrugMatch main.
	 * @param args
	 */
	public static void main(final String[] args) {
		JCommander jc = new JCommander();
		try {
			DrugMatch dm = new DrugMatch();
			jc.addObject(dm);
			jc.parse(args);
			dm.execute();
		} catch (DrugMatchStrictModeViolationException e) {
			log.error(e.getMessage());
			System.exit(ReturnCode.STRICT_MODE_VIOLATION.getValue());
		} catch (DrugMatchConfigurationException e) {
			log.error(e.getMessage());
			System.exit(ReturnCode.CONFIGURATION_ERROR.getValue());
		} catch (ParameterException e) {
			log.debug("Error encountered", e);
			jc.usage();
			System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
		} catch (Exception e) {
			log.error("Error encountered", e.getMessage());
			log.debug("Error encountered", e);
			System.exit(ReturnCode.GENERAL_EXCEPTION.getValue());
		}
	}
}
