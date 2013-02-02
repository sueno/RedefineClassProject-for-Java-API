package test;

import java.util.Map;

import info.nohoho.weave.WeaveClassList;


public class Stub_Clone {

	private static String a;
	private TestStub ts = new TestStub(3);

	public static void main (String[] args) {
		Stub_Clone s = new Stub_Clone("s");
	}
	
	public Stub_Clone(String s) {
		a = new String(s);
		sss();
		WeaveClassList aa = new WeaveClassList();
		Map g = WeaveClassList.getMap(ts.getClass().getName());
		System.out.println("");
	}
	
	public void sss () {
		info.nohoho.weave.WeaveClassList.registObject(ts);
	}
}
