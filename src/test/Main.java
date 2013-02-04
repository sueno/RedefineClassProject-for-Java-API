package test;

import info.nohoho.weave.Weave;

public class Main {
	
	public static void main(String[] args) throws Exception{


		// Stubクラスを変更可能に
		Weave.redefineable("test.TestStub");
		
		// Stubクラスを生成
		StubInterface fc = new TestStub();

		// 通常の呼び出し
		System.err.println("Called Stub.hoge()");
		System.out.println("return : " + fc.getNum());

		try {
			Thread.currentThread().sleep(100);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 戻り値の値を変更
		Weave.defineTarget("test.TestStub","getNum",new Class[]{}, "return -1;");
		System.err.println("Called Stub.hoge()");
		System.out.println("return : " + fc.getNum());

	}
}
