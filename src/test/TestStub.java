package test;

public class TestStub {
	private int num = 0;
	private Object obj = null;
	private String str = null;
	public TestStub () {
	}
	public TestStub (int n) {
		num = n;
	}
	public TestStub (Object o) {
		obj = o;
	}
	public int test_Num () {
		return num;
	}
	public Object test_Obj () {
		return obj;
	}
}
