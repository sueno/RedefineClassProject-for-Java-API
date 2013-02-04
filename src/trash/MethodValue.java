package trash;

import java.lang.reflect.Method;

public class MethodValue {

	
	public static void main(String[] args) throws Exception{
		Class c = MethodValue.class;
		Method m = c.getDeclaredMethod("hoge", new Class[0]);
		System.out.println("class name : "+m.getDeclaringClass().getName());
	}
	
	public void hoge() {
		
	}
}
