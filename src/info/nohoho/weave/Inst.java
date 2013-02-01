package info.nohoho.weave;


import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Loader;

//@SuppressWarnings("all")
public class Inst {

	public static String targetClassName = null;
	
	public static void premain(Instrumentation inst, String args[]) {
		redefineable(args[0]);
	}

	/**
	 * 引数に渡されたクラスを，変更可能にする
	 * @param className
	 */
	public static void redefineable(String className) {
		targetClassName = className;
		try {
			// 対象クラスのCloneを生成
			Class<?> cl = makeClass(className);
			
			// 対象クラスをProzy化
			ClassPool cp = ClassPool.getDefault();
			CtClass target = cp.get(className);
			target.addField(CtField.make(
					"private static java.lang.Object stub_clone = new "
							+ cl.getName() + "();", target));
			CtMethod[] methods = target.getDeclaredMethods();
			for (CtMethod m : methods) {
				StringBuilder sb = new StringBuilder();
				sb.append("try{");
				sb.append("return ($r)stub_clone.getClass().getDeclaredMethod(\""
						+ m.getName() + "\",$sig).invoke(stub_clone, $args);");
				sb.append("}catch(java.lang.reflect.InvocationTargetException iex) {");
				sb.append("throw iex.getCause(); }");
				m.setBody("" + sb);
			}
			target.addMethod(CtMethod
					.make("public static void set_Stub(Object stub) {stub_clone = stub;}",
							target));
			target.toClass(Thread.currentThread().getContextClassLoader());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 引数に渡されたクラスのクローンを生成する
	 * @param className
	 * @return
	 */
	public static Class makeClass(String className) {
		ClassPool cp = ClassPool.getDefault();
		// create $Clone_"+className+"
		try {
			CtClass targetC = cp.get(className);
			targetC.setName("$Clone_"+className+"");
			return targetC.toClass(Thread.currentThread()
					.getContextClassLoader());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 引数に渡せれたメソッド名と同名のメソッド内処理を，引数のメソッドバリューに変更
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
	 * @param methodName
	 * @param methodValue
	 * @return
	 */
	public static Class define(String methodName, String methodValue) {
		ClassPool cp = ClassPool.getDefault();

		// create $Clone_"+className+"
		try {
			CtClass targetC = cp.get("$Clone_"+targetClassName+"");
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
