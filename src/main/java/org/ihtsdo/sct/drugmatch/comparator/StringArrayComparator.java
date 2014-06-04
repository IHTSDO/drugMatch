package org.ihtsdo.sct.drugmatch.comparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compare {@link String} arrays, if identical size, by value.
 * @author dev-team@carecom.dk
 */
public class StringArrayComparator implements Comparator<String[]>, Serializable {

	public final int compare(final String[] o1,
			final String[] o2) {
		int result = o1.length - o2.length;
		if (result == 0) {
			String v1,
				v2;
			for (int i = 0; i < o1.length; i++) {
				v1 = o1[i];
				v2 = o2[i];
				if (v1 == null
						&& v2 == null) {
					result = 0;
				} else if (v1 == null) {
					result = 1;
				} else if (v2 == null) {
					result = -1;
				} else {
					result = v1.compareTo(v2);
				}
				if (result != 0) {
					break;
				}
			}
		}
		return result;
	}
}
