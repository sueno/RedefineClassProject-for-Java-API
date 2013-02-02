package test;

import static org.junit.Assert.*;
import junit.framework.TestCase;
import info.nohoho.weave.Inst;
import info.nohoho.weave.Weave;

import org.junit.Test;

public class Driver_MainTest extends TestCase{
	
	static {
		Inst.redefineable("test.TestStub");
	}
	
	private TestStub tss;
	
	@Override
	protected void setUp() throws Exception {
		Inst.defineTarget("test_Num", "return num;");
		Inst.defineTarget("test_Obj", "return obj;");
		super.setUp();
	}
	
	@Test
	public void test_nonParam1() {
		tss = new TestStub();
		assertEquals(0, tss.test_Num());
	}
	
	@Test
	public void test_preParam() {
		tss = new TestStub(2);
		assertEquals(2, tss.test_Num());
	}
	
	@Test
	public void test_objParam() {
		tss = new TestStub("s");
		assertEquals("s", (String)tss.test_Obj());
	}
	
	@Test
	public void test_redef_nonP() {
		tss = new TestStub();
		Inst.defineTarget("test_Num", "return -1;");
		assertEquals(-1, tss.test_Num());
	}
	
	@Test
	public void test_redef_preP() {
		tss = new TestStub(2);
		Inst.defineTarget("test_Obj", "return new String(\"hoge-\");");
		assertEquals(2, tss.test_Num());
	}
	
	@Test
	public void test_redef_objP() {
		tss = new TestStub(2);
		Inst.defineTarget("test_Num", "return -1;");
		assertEquals(2, tss.test_Obj());
	}

}
