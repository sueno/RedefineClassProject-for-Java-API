RedefineClassProject (正式版)
=========================
実行時にクラスを何度でも再定義可能にするライブラリです．

メソッド単位での再定義が可能です．

なお，クラスを再定義すると，再定義前のクラスのインスタンスへの参照は無くなり，
再定義後新たに生成されたクラスのインスタンスが参照されます．

javassistライブラリが必要です．

標準JavaAPIにも対応予定．．．


用途
---
・ユニットテストのスタブ等

動作例
---
TestStub.java

```
public class TestStub{
	private int num = 0;
	public int getNum() {
		return num;
	}
}
```

Main.java

```
import info.nohoho.weave.Weave;

public class Main {
	
	public static void main(String[] args) throws Exception{

		// Stubクラスを変更可能に
		Weave.redefineable("test.TestStub");
		
		// Stubクラスを生成
		TestStub fc = new TestStub();

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
```

実行結果

```
return : 0
return : -1
return : 0
java.lang.NullPointerException: ほげー！
	at $Clone_test_TestStub.getNum(TestStub.java)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at java.lang.reflect.Method.invoke(Method.java:597)
	at test.TestStub.getNum(TestStub.java)
	at test.Main.main(Main.java:39)
return : 94
return : 29
return : 71
return : 83
return : 18
```