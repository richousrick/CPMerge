
/**
 * TODO Annotate class
 * @author 146813
 */
public class SimpleClass {

	String str;
	
	public SimpleClass(String str){
		this.str = str;
	}
	
	public void addSlash(){
		str+="/";
	}
	
	public void addPlus(){
		str+="+";
	}
	
	public String getString(){
		return str;
	}
	
	public void setString(String str){
		this.str = str;
	}
	
	
	public static void main(String[] args) {
		System.out.println("hello");
	}
	
	class InnerClass{
		public void f1(){
			int i = 1+1;
		}
		
		public void f2(){
			int j = 1+1;
		}
		
		public void f3(){
			System.out.println("hi");
		}
	}
}

class otherClass{
	
	public void f1(){
		System.out.println("hi");
	}
	
	public void f2(){
		System.out.println("yo");
	}
	
	public void f3(){
		int k = 12+4;
	}
	
}
