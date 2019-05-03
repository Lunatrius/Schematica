package aceart.update;

public class Test2 {
	public static void main(String[] args) {
		Integer a = new Integer(4);
		Integer b = a;
		
		a = new Integer(5);
		
		
		System.out.println(b);
		
		Stringer one = new Stringer();
		Stringer two = one;
		one.st = "new";
		System.out.println(two.st);
	}
}

class Stringer {
	String st = "stringer";
}
