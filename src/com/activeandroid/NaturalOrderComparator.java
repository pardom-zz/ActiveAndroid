package com.activeandroid;

import java.util.Comparator;

class NaturalOrderComparator implements Comparator<Object> {
	int compareRight(String a, String b) {
		int bias = 0;
		int ia = 0;
		int ib = 0;

		for (;; ia++, ib++) {
			char ca = charAt(a, ia);
			char cb = charAt(b, ib);

			if (!Character.isDigit(ca) && !Character.isDigit(cb)) {
				return bias;
			}
			else if (!Character.isDigit(ca)) {
				return -1;
			}
			else if (!Character.isDigit(cb)) {
				return +1;
			}
			else if (ca < cb) {
				if (bias == 0) {
					bias = -1;
				}
			}
			else if (ca > cb) {
				if (bias == 0)
					bias = +1;
			}
			else if (ca == 0 && cb == 0) {
				return bias;
			}
		}
	}

	public int compare(Object o1, Object o2) {
		String a = o1.toString();
		String b = o2.toString();

		int ia = 0, ib = 0;
		int nza = 0, nzb = 0;
		char ca, cb;
		int result;

		while (true) {
			nza = nzb = 0;

			ca = charAt(a, ia);
			cb = charAt(b, ib);

			while (Character.isSpaceChar(ca) || ca == '0') {
				if (ca == '0') {
					nza++;
				}
				else {
					nza = 0;
				}

				ca = charAt(a, ++ia);
			}

			while (Character.isSpaceChar(cb) || cb == '0') {
				if (cb == '0') {
					nzb++;
				}
				else {
					nzb = 0;
				}

				cb = charAt(b, ++ib);
			}

			if (Character.isDigit(ca) && Character.isDigit(cb)) {
				if ((result = compareRight(a.substring(ia), b.substring(ib))) != 0) {
					return result;
				}
			}

			if (ca == 0 && cb == 0) {
				return nza - nzb;
			}

			if (ca < cb) {
				return -1;
			}
			else if (ca > cb) {
				return +1;
			}

			++ia;
			++ib;
		}
	}

	static char charAt(String s, int i) {
		if (i >= s.length()) {
			return 0;
		}
		else {
			return s.charAt(i);
		}
	}
}
