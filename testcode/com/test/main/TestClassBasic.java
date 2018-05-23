package com.test.main;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class TestClassBasic {

	class TestIdenticalParameterReturns {

		public TestIdenticalParameterReturns() {
			int i = 10;
		}

		// Expected
		// public void tIFA0_tIFA1_tIFA2(int fID){
		// int i = 10;
		// }

		/**
		 * TestIdenticalFunctionAssignment
		 */
		public void tIFA0() {
			int i = 10;
		}

		/**
		 * TestIdenticalFunctionAssignment
		 */
		public void tIFA1() {
			int i = 10;
		}

		/**
		 * TestIdenticalFunctionAssignment
		 */
		public void tIFA2() {
			int i = 10;
		}

		// Expected
		// public int tIFR0_tIFR1_tIFR2(int fID) {
		// return 10;
		// }

		/**
		 * TestIdenticalFunctionReturns
		 */
		public int tIFR0() {
			return 10;
		}

		/**
		 * TestIdenticalFunctionReturns
		 */
		public int tIFR1() {
			return 10;
		}

		/**
		 * TestIdenticalFunctionReturns
		 */
		public int tIFR2() {
			return 10;
		}

		// Expected
		// public void tIFP0_tIFP1_tIFP2(int x) {
		// x = 12;
		// }

		/**
		 * TestIdenticalFunctionParimeter
		 */
		public void tIFP0(int x) {
			x = 12;
		}

		/**
		 * TestIdenticalFunctionParimeter
		 */
		public void tIFP1(int x) {
			x = 12;
		}

		/**
		 * TestIdenticalFunctionParimeter
		 */
		public void tIFP2(int x) {
			x = 12;
		}

		// Expected
		// public int tIFRP0_tIFRP1_tIFRP2(int x) {
		// return x;
		// }

		/**
		 * TestIdenticalFunctionReturnsParimeter
		 */
		public int tIFRP0(int x) {
			return x;
		}

		/**
		 * TestIdenticalFunctionReturnsParimeter
		 */
		public int tIFRP1(int x) {
			return x;
		}

		/**
		 * TestIdenticalFunctionReturnsParimeter
		 */
		public int tIFRP2(int x) {
			return x;
		}
	}

	class TestSimilarParameterReturns {
		// Expected
		// public void tIFA0_tIFA1(int fID){
		// int i = 10;
		// }
		//
		// /**
		// * TestSimilarFunctionAssignment
		// */
		// public void tSFA2() {
		// int i = 11;
		// }

		/**
		 * TestIdenticalFunctionAssignment
		 */
		public void tSFA0() {
			int i = 10;
		}

		/**
		 * TestIdenticalFunctionAssignment
		 */
		public void tSFA1() {
			int i = 10;
		}

		/**
		 * TestIdenticalFunctionAssignment
		 */
		public void tSFA2() {
			int i = 11;
		}

		// Expected
		// public int tSFR0_tSFR1() {
		// return 10;
		// }
		//
		// /**
		// * TestIdenticalFunctionReturns
		// */
		// public int tIFR2() {
		// return 11;
		// }

		/**
		 * TestIdenticalFunctionReturns
		 */
		public int tSFR0() {
			return 10;
		}

		/**
		 * TestIdenticalFunctionReturns
		 */
		public int tSFR1() {
			return 10;
		}

		/**
		 * TestIdenticalFunctionReturns
		 */
		public int tIFR2() {
			return 11;
		}

		// public void tSFP0_tSFP1(int x) {
		// x = 12;
		// }
		//
		// /**
		// * TestIdenticalFunctionParimeter
		// */
		// public void tSFP2(int y) {
		// y = 12;
		// }

		/**
		 * TestIdenticalFunctionParimeter
		 */
		public void tSFP0(int x) {
			x = 12;
		}

		/**
		 * TestIdenticalFunctionParimeter
		 */
		public void tSFP1(int x) {
			x = 12;
		}

		/**
		 * TestDifferentFunctionParimeter
		 */
		public void tSFP2(int y) {
			y = 12;
		}

		// public int tSFRP0_tSFRP1(int x) {
		// return x;
		// }
		//
		// /**
		// * TestIdenticalFunctionReturnsParimeter
		// */
		// public int tSFRP2(int y) {
		// return y;
		// }

		/**
		 * TestIdenticalFunctionReturnsParimeter
		 */
		public int tSFRP0(int x) {
			return x;
		}

		/**
		 * TestIdenticalFunctionReturnsParimeter
		 */
		public int tSFRP1(int x) {
			return x;
		}

		/**
		 * TestIdenticalFunctionReturnsParimeter
		 */
		public int tSFRP2(int y) {
			return y;
		}
	}

	class TestIdenticalStructure {

		// public int tII0_tII1_tII2(boolean x) {
		// if(x) {
		// return 5;
		// }else {
		// return 7;
		// }
		// }

		public int tII0(boolean x) {
			if (x)
				return 5;
			else
				return 7;
		}

		public int tII1(boolean x) {
			if (x)
				return 5;
			else
				return 7;
		}

		public int tII2(boolean x) {
			if (x)
				return 5;
			else
				return 7;
		}

		// Not expected to merge

		public int tIS0(int x) {
			switch (x) {
				case 0:
					return x;
				case 1:
					return 2;
				case 3:
					return 11;
				case 5:
					return 12;
				default:
					return x * 3;
			}
		}

		public int tIS1(int x) {
			switch (x) {
				case 0:
					return x;
				case 1:
					return 2;
				case 3:
					return 11;
				case 5:
					return 12;
				default:
					return x * 3;
			}
		}

		// public int tISLC0_tISLC1_tISLC2(int x) {
		// return x == 12?7:5;
		// }

		public int tISLC0(int x) {
			return x == 12 ? 7 : 5;
		}

		public int tISLC1(int x) {
			return x == 12 ? 7 : 5;
		}

		public int tISLC2(int x) {
			return x == 12 ? 7 : 5;
		}

		// public int tIF0_tIF1_tIF2(int s) {
		// int t = 0;
		// for(int i = 0; i<s; i++) {
		// t++;
		// }
		// return t;
		// }

		public int tIF0(int s) {
			int t = 0;
			for (int i = 0; i < s; i++) {
				t++;
			}
			return t;
		}

		public int tIF1(int s) {
			int t = 0;
			for (int i = 0; i < s; i++) {
				t++;
			}
			return t;
		}

		public int tIF2(int s) {
			int t = 0;
			for (int i = 0; i < s; i++) {
				t++;
			}
			return t;
		}

		// public int tIW0_tIW1_tIW2(int s) {
		// int i = 0;
		// while(i < s) {
		// i++;
		// }
		// return i;
		// }

		public int tIW0(int s) {
			int i = 0;
			while (i < s) {
				i++;
			}
			return i;
		}

		public int tIW1(int s) {
			int i = 0;
			while (i < s) {
				i++;
			}
			return i;
		}

		public int tIW2(int s) {
			int i = 0;
			while (i < s) {
				i++;
			}
			return i;
		}

		// public int tIDW0_tIDW1_tIDW2(int s) {
		// int i = 0;
		// do {
		// i++;
		// } while(i < s);
		// return i;
		// }

		public int tIDW0(int s) {
			int i = 0;
			do {
				i++;
			} while (i < s);
			return i;
		}

		public int tIDW1(int s) {
			int i = 0;
			do {
				i++;
			} while (i < s);
			return i;
		}

		public int tIDW2(int s) {
			int i = 0;
			do {
				i++;
			} while (i < s);
			return i;
		}

		// public int tIFE0_tIFE1_tIFE2(int[] s) {
		// int i = 0;
		// for(int x : s ) {
		// i+=x;
		// }
		// return i;
		// }

		public int tIFE0(int[] s) {
			int i = 0;
			for (int x : s) {
				i += x;
			}
			return i;
		}

		public int tIFE1(int[] s) {
			int i = 0;
			for (int x : s) {
				i += x;
			}
			return i;
		}

		public int tIFE2(int[] s) {
			int i = 0;
			for (int x : s) {
				i += x;
			}
			return i;
		}

	}

	class TestSimilarStructure {

		// public int tSI0_tSI1(boolean x) {
		// if(x) {
		// return 5;
		// }else {
		// return 7;
		// }
		// }

		public int tSI0(boolean x) {
			if (x)
				return 5;
			else
				return 7;
		}

		public int tSI1(boolean x) {
			if (x)
				return 5;
			else
				return 7;
		}

		public int tSI2(boolean x) {
			if (x)
				return 7;
			else
				return 5;
		}

		// public int tSS0_tSS1(int x) {
		// switch(x) {
		// case 0:
		// return x;
		// case 1:
		// return 2;
		// case 3:
		// return 11;
		// case 5:
		// return 12;
		// default:
		// return x*3;
		// }
		// }

		public int tSS0(int x) {
			switch (x) {
				case 0:
					return x;
				case 1:
					return 2;
				case 3:
					return 11;
				case 5:
					return 12;
				default:
					return x * 3;
			}
		}

		public int tSS1(int x) {
			switch (x) {
				case 0:
					return x;
				case 1:
					return 2;
				case 3:
					return 11;
				case 5:
					return 12;
				default:
					return x * 3;
			}
		}

		public int tIS2(int x) {
			switch (x) {
				case 2:
					return x;
				case 4:
					return 2;
				case 6:
					return 11;
				case 7:
					return 12;
				default:
					return x * 3;
			}
		}

		// public int tSSLC0_tSSLC1(int x) {
		// return x == 12?7:5;
		// }

		public int tSSLC0(int x) {
			return x == 12 ? 7 : 5;
		}

		public int tSSLC1(int x) {
			return x == 12 ? 7 : 5;
		}

		public int tSSLC2(int x) {
			return x == 11 ? 7 : 5;
		}

		// public int tSF0_tSF1(int s) {
		// int t = 0;
		// for(int i = 0; i<s; i++) {
		// t++;
		// }
		// return t;
		// }

		public int tSF0(int s) {
			int t = 0;
			for (int i = 0; i < s; i++) {
				t++;
			}
			return t;
		}

		public int tSF1(int s) {
			int t = 0;
			for (int i = 0; i < s; i++) {
				t++;
			}
			return t;
		}

		public int tSF2(int s) {
			int t = 0;
			for (int i = s; i >= 0; i--) {
				t++;
			}
			return t;
		}

		// public int tSW0_tSW1(int s) {
		// int i = 0;
		// while(i < s) {
		// i++;
		// }
		// return i;
		// }

		public int tSW0(int s) {
			int i = 0;
			while (i < s) {
				i++;
			}
			return i;
		}

		public int tSW1(int s) {
			int i = 0;
			while (i < s) {
				i++;
			}
			return i;
		}

		public int tSW2(int s) {
			int i = s;
			while (i >= 0) {
				i--;
			}
			return i;
		}

		// public int tSDW0_tSDW1(int s) {
		// int i = 0;
		// do {
		// i++;
		// } while(i < s);
		// return i;
		// }

		public int tSDW0(int s) {
			int i = 0;
			do {
				i++;
			} while (i < s);
			return i;
		}

		public int tSDW1(int s) {
			int i = 0;
			do {
				i++;
			} while (i < s);
			return i;
		}

		public int tSDW2(int s) {
			int i = s;
			do {
				i--;
			} while (i >= 0);
			return i;
		}

		// public int tSFE0_tSFE1_tSFE2(int[] s, int fID) {
		// int i = 0;
		// for (int x : s) {
		// if (fID == 2) {
		// i -= x;
		// } else {
		// i += x;
		// }
		// }
		// return i;
		// }

		public int tSFE0(int[] s) {
			int i = 0;
			for (int x : s) {
				i += x;
			}
			return i;
		}

		public int tSFE1(int[] s) {
			int i = 0;
			for (int x : s) {
				i += x;
			}
			return i;
		}

		public int tSFE2(int[] s) {
			int i = 0;
			for (int x : s) {
				i -= x;
			}
			return i;
		}

		public int tSFE3(String[] s) {
			String i = "";
			for (String x : s) {
				i += x;
			}
			return i.length();
		}

	}

	class TestAnnotationsMerge {

		/**
		 * This annotation should stay
		 */
		public int functionWithAnnotation() {
			return 12;
		}

		/**
		 * This annotation should be removed
		 */
		public int f1(int x) {
			return x;
		}

		// this should stay

		/**
		 * This annotation should be removed
		 */
		public int f2(int x) {
			return x;
		}

		/**
		 * This annotation should stay
		 */
		public String functionToNotBeMerged() {
			return "hi";
		}

		/**
		 * This annotation should be removed
		 */
		public int f3(int x) {
			return x;
		}

	}

	class TestDifferentFunctionIO {

		class TestDifferentReturns {
			// Should not be merged

			public String iFBDR0(int s) {
				int x = 0;
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			public int iFBDR1(int s) {
				int x = 0;
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x;
			}
		}

		class TestDifferentParameterNames {

			// public String iDPN0_iDPN1_iDPN2(int s, int x, int fID) {
			// if(fID == 0) {
			// x = 0;
			// }
			// for (int i = 0; i < s; i++) {
			// for (int y = i; y >= 0; y--) {
			// x += y;
			// }
			// }
			// return x + "";
			// }

			public String tDPN0(int s) {
				int x = 0;
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			public String tDPN1(int s, int x) {
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			public String tDPN2(int x, int s) {
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			// Not expected to be merged

			public String tDPN3(int t) {
				int x = 0;
				for (int i = 0; i < t; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			// Only 1 of (iDPN4, 5) can merge

			public String tDPN4(int s, int z) {
				int x = z;
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			public String tDPN5(int s, String z) {
				int x = 0;
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			// Cannot Merge

			public String tDPN6(int s, int i) {
				int x = 0;
				for (; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			// cannot Merge

			public String tDPN7(int s, int... is) {
				int x = 0;
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

		}

		class TestOverloading {

			// public String tO_tO(int s, int x, int fID) {
			// if(fID == 0) {
			// x = 0;
			// }
			// for (int i = 0; i < s; i++) {
			// for (int y = i; y >= 0; y--) {
			// x += y;
			// }
			// }
			// return x + "";
			// }

			public String tO(int s) {
				int x = 0;
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			public String tO(int s, int x) {
				for (int i = 0; i < s; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				return x + "";
			}

			public String tO() {
				return "";
			}
		}

		class TestInvalids {

			public void tIC0() {
				int x = 0;
				for (int i = 0; i < 10; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
					}
				});
			}

			public void tIC1() {
				int x = 0;
				for (int i = 0; i < 10; i++) {
					for (int y = i; y >= 0; y--) {
						x += y;
					}
				}
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
					}
				});
			}
		}

	}

}
