package test;

import static org.junit.Assert.*;
import junit.framework.TestCase;
import info.nohoho.weave.Inst;

import org.junit.Test;

public class Driver_MainTest extends TestCase{
	
	static {
		Inst.redefineable("test.TestStub");
	}
	
	private TestStub ts;
	
	@Override
	protected void setUp() throws Exception {
		Inst.defineTarget("test.TestStub","getNum", "return num;");
		Inst.defineTarget("test.TestStub","getObj", "return obj;");
		Inst.defineTarget("test.TestStub","getStr", "return str;");
		super.setUp();
	}
	
	@Test
	public void test_nonParam1() {
		ts = new TestStub();
		assertEquals(0, ts.test_Num());
	}
	
	@Test
	public void test_preParam() {
		ts = new TestStub(2);
		assertEquals(2, ts.test_Num());
	}
	
	@Test
	public void test_objParam() {
		ts = new TestStub("s");
		assertEquals("s", (String)ts.test_Obj());
	}
	
	@Test
	public void test_redef_nonP() {
		ts = new TestStub();
		Inst.defineTarget("test.TestStub","test_Num", "return -1;");
		assertEquals(-1, ts.test_Num());
	}
	
	@Test
	public void test_redef_preP() {
		ts = new TestStub(2);
		Inst.defineTarget("test.TestStub","test_Obj", "return new String(\"hoge-\");");
		assertEquals(2, ts.test_Num());
	}
	
	@Test
	public void test_redef_objP() {
		ts = new TestStub(2);
		Inst.defineTarget("test.TestStub","test_Num", "return -1;");
		assertEquals(null, ts.test_Obj());
	}
	
	@Test
	public void test_2obj() {
		ts = new TestStub(3);
		TestStub tss = new TestStub(5);
		Inst.defineTarget("TestStub", "getNum", "return num*3;");
		assertEquals(9, ts.getNum());
		assertEquals(15, tss.getNum());
	}

}
