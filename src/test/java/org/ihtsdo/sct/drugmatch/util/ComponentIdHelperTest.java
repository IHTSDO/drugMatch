package org.ihtsdo.sct.drugmatch.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author dev-team@carecom.dk
 */
public class ComponentIdHelperTest {

	@Test
	public final void getNamespaceId() {
		Assert.assertNull(ComponentIdHelper.getNamespaceId(null));
		Assert.assertEquals("0",
				ComponentIdHelper.getNamespaceId("700074001"));
		Assert.assertEquals("1000005",
				ComponentIdHelper.getNamespaceId("554851000005102"));
		Assert.assertEquals("0",
				ComponentIdHelper.getNamespaceId("123456789010"));
	}
}
