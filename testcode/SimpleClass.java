
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
		 public void f3() {
			System.out.println("hi");
		}

		public void f1245(int value){
			int i = 1+1;
			if(value == 4 || value == 5){
				int j = 1+1;
			}
		} 
		
		public String f67(boolean f6){
			if(f6){
				int i = 5+3;
				int j = 1+1;
				j = 5;
			}
			String str = "what is up";
			return str;
		 }
		 
		 private void f8() {
			int j = 5 + 3;
			int i = 1 + 1;
		}
		 
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
		
		public String f6(){
			int i = 5+3;
			int j = 1+1;
			
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
		int k = 13+5;
	}
	public String function1(String inputString) {
		String str = "";
		for (char c : inputString.toCharArray()) {
			str += c;
		}
		return str;
	}
	
	
	class thirdClass{
		
		public void t1(){
			int i = 10;
		}
		
		public void t2(){
			int j = 10;
		}
		
		public void t1(){
			int i = 11;
		}
	}

	

}
