package com.test.main;
import java.awt.Point;


/**
 * TODO Annotate class
 *
 * @author 146813
 */
public class SimpleClass {

	String str;

	/*
	 * desired

		public SimpleClass(String str) {
			this.str = str;
		}

		public void addChar(boolean slash) {
			if(slash) {
				str += "/";
			}else {
				str +="+";
			}
		}

		public String getString() {
			return str;
		}

		public void setString(String str) {
			this.str = str;
		}

		public static void main(String[] args) {
			System.out.println("hello");
		}

	 */

	public SimpleClass(String str) {
		this.str = str;
	}

	public void addSlash() {
		str += "/";
	}

	public void addPlus() {
		str += "+";
	}

	public String getString() {
		return str;
	}

	public void setString(String str) {
		this.str = str;
	}

	public static void main(String[] args) {
		System.out.println("hello");
	}

	private void t() {

	}

	class InnerClass {

		/*
		 * public void f3() {
		 * System.out.println("hi");
		 * }
		 * public void f1245(int value){
		 * int i = 1+1;
		 * if(value == 4 || value == 5){
		 * int j = 1+1;
		 * }
		 * }
		 * public String f67(boolean f6){
		 * if(f6){
		 * int i = 5+3;
		 * int j = 1+1;
		 * j = 5;
		 * }
		 * String str = "what is up";
		 * return str;
		 * }
		 * private void f8() {
		 * int j = 5 + 3;
		 * int i = 1 + 1;
		 * }
		 */
		public void f1() {
			int i = 1 + 1;
		}

		public void f2() {
			int j = 1 + 1;
		}

		public void f3() {
			System.out.println("hi");
		}

		public void f4() {
			int i = 1 + 1;
			int j = 5 + 3;
		}

		public String f7() {
			String str = "what is up";
			return str;
		}

		public void f5() {
			int j = 5 + 3;
			int i = 1 + 1;
		}

		public String f6() {
			int i = 5 + 3;
			int j = 1 + 1;

			j = 5;
			String str = "what is up";
			return str;
		}

		private void f8() {
			int j = 5 + 3;
			int i = 1 + 1;
		}

	}
}

class otherClass {

	/*
	public void f12(boolean f1) {
		if(f1)
			System.out.println("hi");
		else
			System.out.println("yo");
	}

	public void f3() {
		int k = 12 + 4;
	}

	public String function1(String inputString) {
		String str = "";
		for (char c : inputString.toCharArray()) {
			str += c;
		}
		return str;
	}
	 */

	public void f1() {
		System.out.println("hi");
	}

	public void f2() {
		System.out.println("yo");
	}

	public void f3() {
		int k = 12 + 4;
	}

	public void f4() {
		int k = 13 + 5;
	}

	public String function1(String inputString) {
		String str = "";
		for (char c : inputString.toCharArray()) {
			str += c;
		}
		return str;
	}


	class thirdClass {
		public int testInt, otherInt;
		String thirdClassStr;

		/**
		 * Init the method
		 */
		public void main() {
			testInt = 5;
		}




		public void t1() {
			int i = 10;
		}

		public void t2() {
			int j = 10;
		}

		public void t3() {
			int i = 11;
		}

		public int t4(int a) {
			int x = 3 * a;
			if (a == 4) {
				x *= 2;
				return x;
			} else
				return 0;
		}

		public int t5(int a) {
			int x = 3 * a;
			if (a == 4)
				return 0;
			else
				return x;
		}

		public int t6(int a) {
			int x = 3 * a;
			if (a == 5)
				return 0;
			else
				return x;
		}

		public int t7(int a) {
			int x = 3 * a;
			if (a == 5)
				return 0;
			else
				return x;
		}


		public int t8(int a) {
			int x = 4 * a;
			if (a == 4)
				return 0;
			else
				return x;
		}

		public int t9(int a) {
			int x = 4 * a;
			if (a == 4)
				return 0;
			else
				return x;
		}


		/**
		 * Some documentation for 1
		 *
		 * @param start
		 *            the first part
		 * @param end
		 *            the end part
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

		public int functionCaller1() {
			SimpleClass s = new SimpleClass("hi");
			s.addPlus();
			t8(1);
			thirdClassStr = "";
			thirdClassStr.length();
			function1("hi").substring(0);
			Point p1 = new Point(10, 100);
			Point p2 = new Point(100, 10);
			Math.max(costFunction0(p1, p2), costFunction1(p2, p1));
			t4(t6(t7(t5(10))));
			return t9(1);
		}

	}



}
