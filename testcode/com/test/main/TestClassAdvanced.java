package com.test.main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import com.test.main.TestClassBasic.TestIdenticalParameterReturns;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class TestClassAdvanced {

	class Group1 {

		// fG11_2_3, fNG1, fNG2, fG21_2

		public void fG11() {
			int i = 1 + 1;
		}

		public void fNG1() {
			int j = 1 + 1;
		}

		public void fNG2() {
			System.out.println("hi");
		}

		public void fG12() {
			int i = 1 + 1;
			int j = 5 + 3;
		}

		public String fG21() {
			String str = "what is up";
			return str;
		}

		public void fG13() {
			int j = 5 + 3;
			int i = 1 + 1;
		}

		public String fG22() {
			int i = 5 + 3;
			int j = 1 + 1;

			j = 5;
			String str = "what is up";
			return str;
		}
	}

	class Group2 {

		public int fG31(int a) {
			int x = 3 * a;
			if (a == 4) {
				x *= 2;
				return x;
			} else
				return 0;
		}

		public int fG32(int a) {
			int x = 3 * a;
			if (a == 4)
				return 0;
			else
				return x;
		}

		public int fG40(int a) {
			int x = 3 * a;
			if (a == 5)
				return 0;
			else
				return x;
		}

		public int fG41(int a) {
			int x = 3 * a;
			if (a == 5)
				return 0;
			else
				return x;
		}

		public int fG33(int a) {
			int x = 4 * a;
			if (a == 4)
				return 0;
			else
				return x;
		}

		public int fG34(int a) {
			int x = 4 * a;
			if (a == 4)
				return 0;
			else
				return x;
		}

		/*
		 * Some documentation for 1
		 * @param start
		 * the first part
		 * @param end
		 * the end part
		 * @return the cost
		 */
		public double costFunction0(Point start, Point end) {
			double dx = start.getX() - end.getX();
			double dy = start.getY() - end.getY();
			return Math.max(dx, dy);
		}

		/**
		 * some documentation for another
		 *
		 * @param start
		 *            the first part
		 * @param end
		 *            the end part
		 * @return the cost
		 */
		public double costFunction1(Point start, Point end) {
			double dx = start.getX() - end.getX();
			double dy = start.getY() - end.getY();
			return Math.hypot(dx, dy);
		}

		/**
		 * some documentation for another
		 *
		 * @param start
		 *            the first part
		 * @param end
		 *            the end part
		 * @param mid
		 *            the middle part
		 * @return the cost
		 */
		public double costFunction2(Point start, Point end, Point mid) {
			double dx = start.getX() - end.getX();
			double dy = start.getY() - end.getY();
			return Math.min(dx, dy);
		}

		public int listCost0(int[] array) {
			int tot = 0;
			for (int i : array) {
				tot += i;
			}
			return tot;
		}

		public int listCost1(int[] array) {
			int tot = 0;
			for (int i : array) {
				tot *= i;
			}
			return tot;
		}

		public int listCost2(int[] array) {
			int tot = 0;
			int x = 0;
			for (int i : array) {
				tot += x;
				x++;
			}
			return tot;
		}

		private String[] reverse0(String[] array) {
			String[] rev = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				rev[rev.length - (i + 1)] = array[i];
			}
			return rev;
		}

		private String[] reverse1(String[] array) {
			String[] rev = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				rev[rev.length - (i + 1)] = array[i];
			}
			return rev;
		}

		// private void testNestedLoop0_testNestedLoop1(int max, int fID) {
		// int y = 0;
		// for(int i = 0; i< max; i++) {
		// for(int x = i; x < max; x++) {
		// if(fID == 0) {
		// y += x;
		// }else {
		// y ++;
		// }
		// }
		// }
		// }

		private void testNestedLoop0(int max) {
			int y = 0;
			for (int i = 0; i < max; i++) {
				for (int x = i; x < max; x++) {
					y += x;
				}
			}
		}

		private void testNestedLoop1(int max) {
			int y = 0;
			for (int i = 0; i < max; i++) {
				for (int x = i; x < max; x++) {
					y++;
				}
			}
		}

		private String testGenericMerge0(String[] strs, String delim) {
			ArrayList<String> str = new ArrayList<>(Arrays.asList(strs));
			String concat = "";
			for (String s : str) {
				System.out.println(s);
				concat += s + delim;
			}
			if (concat.length() > 0) {
				concat = concat.substring(concat.length(), concat.length() - delim.length());
			}
			return concat;
		}

		private String testGenericMerge1(String[] strs, String delim) {
			ArrayList<String> str = new ArrayList<>(Arrays.asList(strs));
			String concat = "";
			for (String s : str) {
				System.out.println(s);
				concat += s + delim;
			}
			if (concat.length() > 0) {
				concat = concat.substring(concat.length(), concat.length() - delim.length());
			}
			return concat;
		}

		private String testGenericMerge2(String[] strs, String delim, ArrayList<String> str) {
			String concat = "";
			for (String s : str) {
				System.out.println(s);
				concat += s + delim;
			}
			if (concat.length() > 0) {
				concat = concat.substring(concat.length(), concat.length() - delim.length());
			}
			return concat;
		}

	}

	public void testFunctionCalls() {
		// new TestClassBasic().new
		// TestIdenticalParameterReturns().tIFA0_tIFA1_tIFA2(0);
		new TestClassBasic().new TestIdenticalParameterReturns().tIFA0();

		TestIdenticalParameterReturns tIPR = new TestClassBasic().new TestIdenticalParameterReturns();

		// tIPR.tIFA0_tIFA1_tIFA2(0);
		tIPR.tIFA0();

		// tIPR.tIFP0_tIFP1_tIFP2(3, 1);
		tIPR.tIFP1(3);

		// Math.max(tIPR.tIFR0_tIFR1_tIFR2(1), tIPR.tIFR0_tIFR1_tIFR2(2));
		Math.max(tIPR.tIFR1(), tIPR.tIFR2());

		Group2 g2 = new Group2();
		// g2.fG30_fG31_fG32_fG33(g2.fG40_fG41(g2.fG30_fG31_fG32_fG33(12,3),1),1);
		g2.fG31(g2.fG41(g2.fG33(12)));

		new TestClassBasic().new TestDifferentFunctionIO().new TestOverloading().tO();

		// new TestClassBasic().new TestDifferentFunctionIO().new
		// TestOverloading().tO_tO(1,0);
		new TestClassBasic().new TestDifferentFunctionIO().new TestOverloading().tO(1);

		// int x = new Group2().listCost0_listCost1_listCost2(new int[]
		// {1,2,3,4,5}, 0);
		int x = new Group2().listCost0(new int[] { 1, 2, 3, 4, 5 });

		// String[] rev = new Group2().reverse0_reverse1(new String[]
		// {"1","2","3","4","5"}, 0);
		String[] rev = new Group2().reverse0(new String[] { "1", "2", "3", "4", "5" });
	}
}
