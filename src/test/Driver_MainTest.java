package test;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import info.nohoho.weave.Weave;

import org.junit.Test;

public class Driver_MainTest extends TestCase{
	
	
	private StubInterface ts;
	
	// 変更するメソッド
	private static Method getNum_Method;
	private static Method getObj_Method;
	private static Method getStr_Method;
	
	static {
		// test.TestStub クラスを変更可能にする
		Weave.redefineable("test.TestStub");
		try {
			// 変更するメソッドを取得
			getNum_Method = test.TestStub.class.getDeclaredMethod("getNum", new Class[]{});
			getObj_Method = test.TestStub.class.getDeclaredMethod("getObj", new Class[]{});
			getStr_Method = test.TestStub.class.getDeclaredMethod("getStr", new Class[]{});
		} catch (Exception ex) {
			
		}
	}
	
	
	@Override
	protected void setUp() throws Exception {
		// 初期化
		Weave.defineTarget("test.TestStub","getNum",new Class[]{}, "return num;");
		Weave.defineTarget("test.TestStub","getObj",new Class[]{}, "return obj;");
		Weave.defineTarget("test.TestStub","getStr",new Class[]{}, "return str;");
		super.setUp();
	}
	
	@Test
	public void test_nonParam1() {
		ts = new TestStub();
		assertEquals(0, ts.getNum());
	}
	
	@Test
	public void test_preParam() {
		ts = new TestStub(2);
		assertEquals(2, ts.getNum());
	}
	
	@Test
	public void test_objParam() {
		ts = new TestStub("s");
		assertEquals("s", (String)ts.getObj());
	}
	
	@Test
	public void test_redef_nonP() {
		ts = new TestStub();
		Weave.defineTarget(getNum_Method, "return -1;");
		assertEquals(-1, ts.getNum());
	}
	
	@Test
	public void test_redef_preP() {
		ts = new TestStub(2);
		Weave.defineTarget(getObj_Method, "return new String(\"hoge-\");");
		assertEquals(2, ts.getNum());
	}
	
	@Test
	public void test_redef_objP() {
		ts = new TestStub(2);
		Weave.defineTarget(getNum_Method, "return -1;");
		assertEquals(null, ts.getObj());
	}
	
	@Test
	public void test_2obj_1() {
		ts = new TestStub(3);
		TestStub tss = new TestStub(5);
		Weave.defineTarget(getNum_Method, "return num*3;");
		assertEquals(9, ts.getNum());
	}

	@Test
	public void test_2obj_2() {
		ts = new TestStub(3);
		TestStub tss = new TestStub(5);
		Weave.defineTarget(getNum_Method, "return num*3;");
		assertEquals(15, tss.getNum());
	}
	
	@Test
	public void test_field_1() {
		ts = new TestStub();
		ts.setNum(4);
		Weave.defineTarget(getNum_Method, "return num*2;");
		assertEquals(8, ts.getNum());
	}
	
	@Test
	public void test_field_2() {
		ts = new TestStub(3);
		ts.setObj("s");
		Weave.defineTarget(getObj_Method, "return obj+\"_hoge\";");
		assertEquals("s_hoge", ts.getObj());
	}
	
}
