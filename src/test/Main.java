package test;

import java.lang.reflect.Method;

import info.nohoho.weave.Weave;

public class Main {
	
	public static void main(String[] args) throws Exception{


		// Stubクラスを変更可能に
		Weave.redefineable("test.TestStub");
		
		// Stubクラスを生成
		StubInterface fc = new TestStub();

		// 通常の呼び出し
		System.out.println("return : " + fc.getNum());

		// 変更するメソッドを用意
		Method m=null;
		try {
			m = test.TestStub.class.getDeclaredMethod("getNum", new Class[0]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 戻り値の値を変更
		Weave.defineTarget(m, "return -1;");
		System.out.println("return : " + fc.getNum());
		
		// 変更をリセット
		Weave.resetTarget("test.TestStub");
		System.out.println("return : " + fc.getNum());
		
		// Nullpoを吐くように変更
		Weave.defineTarget(m,"throw new NullPointerException(\"ほげー！\");");
		try {
			System.out.println("return : " + fc.getNum());
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
		
		// ランダムな値を返す
		Weave.defineTarget(m, "return (int)(Math.random()*100);");
		for (int i=0;i<5;++i) {
			System.out.println("return : " + fc.getNum());
		}
		
	}
}
