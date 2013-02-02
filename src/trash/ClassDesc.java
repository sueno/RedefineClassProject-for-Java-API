package trash;

public class ClassDesc {

	public static void main (String[] args) throws Throwable{
		Class cl = Class.forName("test.TestStub");
		cl.getConstructor(null);
	}
}
