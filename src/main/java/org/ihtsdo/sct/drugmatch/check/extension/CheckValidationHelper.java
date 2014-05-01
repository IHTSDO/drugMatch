package org.ihtsdo.sct.drugmatch.check.extension;

import org.ihtsdo.sct.drugmatch.check.CheckValidation;
import org.ihtsdo.sct.drugmatch.check.CheckValidationImpl;
import org.ihtsdo.sct.drugmatch.check.extension.danish.CheckValidationDanishImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dev-team@carecom.dk
 */
public final class CheckValidationHelper {

	private static final Logger log = LoggerFactory.getLogger(CheckValidationHelper.class);

	private CheckValidationHelper() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param namespaceId
	 * @return {@link CheckValidation} matching the national namespace ID, otherwise the generic instance is returned.
	 */
	public static CheckValidation getCheckValidation(final String namespaceId) {
		CheckValidation checkValidation;
		if ("1000005".equals(namespaceId)) {
			// Danish
			checkValidation = new CheckValidationDanishImpl();
		} else {
			// generic
			checkValidation = new CheckValidationImpl();
		}
		log.debug("Initialized {}", checkValidation.getClass().getName());
		return checkValidation;
	}
}
