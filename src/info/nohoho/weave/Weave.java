package info.nohoho.weave;
/**
 * @author sueno
 * 
 */

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Loader;

@SuppressWarnings("all")
public class Weave {

	public static String className = null;
	
	public static void premain(Instrumentation inst, String args[]) {
		redefineable(args[0]);
	}

	public static void redefineable(String c) {
		className = c;
		try {
			Class<?> cl = makeClass(c);

			ClassPool cp = ClassPool.getDefault();
			CtClass target = cp.get(c);
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
	
	public static void defineTarget(String methodName, String methodValue) {
		try {
			Object o = define(methodName, methodValue).newInstance();
			Class c = Class.forName(className);
			Method[] mm = c.getDeclaredMethods();
			Method m = c.getDeclaredMethod("set_Stub", Object.class);
			m.invoke(null, o);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Class define(String methodName, String methodValue) {
		ClassPool cp = ClassPool.getDefault();

		// create $Clone_"+className+"
		try {
			CtClass targetC = cp.get("$Clone_"+className+"");
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
