package test;

public class TestStub implements StubInterface{
	private int num = 0;
	private Object obj = null;
	private String str = null;
	private static int p_num = 10;
	private static Object p_obj = "static_obj";
	public TestStub () {
	}
	public TestStub (int n) {
		num = n;
	}
	public TestStub (Object o) {
		obj = o;
	}
	public int test_Num () {
		return 1;
	}
	public Object test_Obj () {
		return "str";
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}
	public static int getP_num() {
		return p_num;
	}
	public static void setP_num(int p_num) {
		TestStub.p_num = p_num;
	}
	public static Object getP_obj() {
		return p_obj;
	}
	public static void setP_obj(Object p_obj) {
		TestStub.p_obj = p_obj;
	}
	
}
