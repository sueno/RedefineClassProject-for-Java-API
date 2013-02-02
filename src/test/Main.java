package test;

import info.nohoho.weave.Inst;

public class Main {
	public static void main(String[] args) throws Exception{


		// Stubクラスを変更可能に
		Inst.redefineable("test.TestStub");
		
		// Stubクラスを生成
		TestStub fc = new TestStub();

		// 通常の呼び出し
		System.err.println("Called Stub.hoge()");
		System.out.println("return : " + fc.getNum());

		try {
			Thread.currentThread().sleep(100);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 戻り値の値を変更
		Inst.defineTarget("test.TestStub","getNum", "return -1;");
		System.err.println("Called Stub.hoge()");
		System.out.println("return : " + fc.getNum());

	}
}
