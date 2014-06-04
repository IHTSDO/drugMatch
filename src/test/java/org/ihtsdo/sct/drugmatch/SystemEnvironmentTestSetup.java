package org.ihtsdo.sct.drugmatch;

import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author dev-team@carecom.dk
 */
public class SystemEnvironmentTestSetup {

	@BeforeClass
	public static void setup() {
		System.setProperty(DrugMatchProperties.SETTING_FILE, "src/test/resource/setting.properties");
	}

	@AfterClass
	public static void teardown() {
		System.clearProperty(DrugMatchProperties.SETTING_FILE);
	}
}
