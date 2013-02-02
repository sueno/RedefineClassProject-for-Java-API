package info.nohoho.weave;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Loader;

//@SuppressWarnings("all")
public class Inst {

	public static String targetClassName = null;
	public static CtClass original_Target = null;

	/**
	 * 実行前処理
	 * 
	 * @param inst
	 * @param args
	 */
	public static void premain(Instrumentation inst, String args[]) {
		redefineable(args[0]);
	}

	/**
	 * 引数に渡されたクラスを，変更可能にする
	 * 
	 * @param className 変更可能とするクラス名
	 * @return 成功，失敗
	 */
	public static boolean redefineable(String className) {
		targetClassName = className;
		try {
			// 対象クラスのClone(実体)を生成
			Class<?> cl = makeClass(className);

			/**
			 * 対象クラスをProxy化
			 */
			ClassPool cp = ClassPool.getDefault();
			CtClass target = cp.get(className);
			// 実体のインスタンスを保持するフィールドを定義
			target.addField(CtField.make("private static java.lang.Object stub_clone;", target));// = new " + cl.getName() + "();", target));
			//コンストラクタの引数を保持するフィールドを定義
			target.addField(CtField.make("private static java.lang.Object[] _const_param;", target));
			// コンストラクタの引数をフィールドに格納，実体生成時の引数として扱う
			CtConstructor[] consts = target.getDeclaredConstructors();
			for (CtConstructor co : consts) {
				co.insertBefore("_const_param = $args; stub_clone = new " + cl.getName() + "($$);");
			}
			// 対象クラスのメソッドの処理を置き換え
			// 実体インスタンスへのメソッド呼び出しを行う処理に置き換え(リフレクションで呼び出す)
			CtMethod[] methods = target.getDeclaredMethods();
			for (CtMethod m : methods) {
				StringBuilder sb = new StringBuilder();
				sb.append("try{");
				sb.append("return ($r)stub_clone.getClass().getDeclaredMethod(\"" + m.getName() + "\",$sig).invoke(stub_clone, $args);");
				sb.append("}catch(java.lang.reflect.InvocationTargetException iex) {");
				sb.append("throw iex.getCause(); }");
				m.setBody("" + sb);
			}
			target.addMethod(CtMethod.make("public static void set_Stub(Object stub) {stub_clone = stub;}", target));
			// 対象クラスのロード
			target.toClass(Thread.currentThread().getContextClassLoader());
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * 引数に渡されたクラスのクローンを生成する
	 * 
	 * @param className
	 * @return
	 */
	public static Class makeClass(String className) {
		ClassPool cp = ClassPool.getDefault();
		// create $Clone_"+className+"
		try {
			CtClass targetC = cp.get(className);
			targetC.setName("$Clone_" + className + "");
			return targetC.toClass(Thread.currentThread().getContextClassLoader());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 引数に渡せれたメソッド名と同名のメソッド内処理を，引数のメソッドバリューに変更
	 * 
	 * @param methodName
	 * @param methodValue
	 */
	public static void defineTarget(String methodName, String methodValue) {
		try {
			Object o = define(methodName, methodValue).newInstance();
			Class c = Class.forName(targetClassName);
			Method[] mm = c.getDeclaredMethods();
			Method m = c.getDeclaredMethod("set_Stub", Object.class);
			m.invoke(null, o);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * クラスの動的生成
	 * 
	 * @param methodName
	 * @param methodValue
	 * @return
	 */
	public static Class define(String methodName, String methodValue) {
		ClassPool cp = ClassPool.getDefault();

		// create $Clone_"+className+"
		try {
			CtClass targetC = cp.get("$Clone_" + targetClassName + "");
			targetC.defrost();
			CtMethod targetM = targetC.getDeclaredMethod(methodName);
			targetM.insertBefore(methodValue);

			return targetC.toClass(new Loader());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
