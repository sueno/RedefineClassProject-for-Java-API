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
import javassist.Modifier;

@SuppressWarnings("all")
public class Inst {

//	public static String targetClassName = null;
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
	 * @param className
	 *            変更可能とするクラス名
	 * @return 成功，失敗
	 */
	public static boolean redefineable(String className) {
//		targetClassName = className;
		try {
			// 対象クラスのClone(実体)を生成
			Class<?> cl = makeClass(className);

			/**
			 * 対象クラスをProxy化
			 */
			ClassPool cp = ClassPool.getDefault();
			cp.importPackage("info.nohoho.weave");
			CtClass target = cp.get(className);
			// 実体のインスタンスを保持するフィールドを定義
			target.addField(CtField.make("private static java.lang.Class _cloneClass = "+cl.getName()+".class;", target));
			target.addField(CtField.make("private boolean __flag = false;", target));
			target.addField(CtField.make("private java.lang.Object _stub_clone;", target));// = new " + cl.getName() + "();", target));
			//コンストラクタの引数を保持するフィールドを定義
			target.addField(CtField.make("private java.lang.Class[] _const_sig = null;", target));
			target.addField(CtField.make("private java.lang.Object[] _const_param = null;", target));
		
			
			// コンストラクタの引数をフィールドに格納，実体生成時の引数として扱う
			CtConstructor[] consts = target.getDeclaredConstructors();
			for (CtConstructor co : consts) {
				co.insertAfter(""
								+"_stub_clone = new "+cl.getName()+"($$);"
								+"_const_sig = $sig;"
								+"_const_param = $args;"
								);
			}

			// 実体参照の変更メソッドを追加
			target.addMethod(CtMethod.make(
						"public void _set_Stub(Class stubClass) {"
							+"if (_const_sig!=null&&_const_sig.length!=0) {"
								+"_stub_clone = stubClass.getConstructor(_const_sig).newInstance(_const_param);"
							+"} else {"
								+"_stub_clone = stubClass.newInstance();"
							+"}"
						+"}",
						target));
			// 対象クラスのメソッドの処理を置き換え
			// 実体インスタンスへのメソッド呼び出しを行う処理に置き換え(リフレクションで呼び出す)
			CtMethod[] methods = target.getDeclaredMethods();
			for (CtMethod m : methods) {
				String fieldcheck = "";
				if (!Modifier.isStatic(m.getModifiers())) {
					fieldcheck = ""
							+"if (!_stub_clone.getClass().equals(_cloneClass)) {"
//								+"this.class.getDeclaredMathod(\"addObj\",new Class[0]).invoke(this,new Object[0]);"
//								+"_stub_clone = new "+cl.getName()+"(_const_param);"
								+"_set_Stub(_cloneClass);"
//								+"info.nohoho.weave.WeaveClassList.registObject(this);"
							+"}"
							;
				}
//				System.out.println("Method : "+m.getLongName()+"\naddsrc : "+fieldcheck+"\n");
				if (m.getName().equals("_set_Stub")) {
//					m.insertBefore("if (!__flag)info.nohoho.weave.WeaveClassList.registObject(this);");
				}else{
					m.setBody(
								"try{"
									+ fieldcheck
									+"return ($r)_stub_clone.getClass().getDeclaredMethod(\"" + m.getName() + "\",$sig).invoke(_stub_clone, $args);"
								+"}catch(java.lang.reflect.InvocationTargetException iex) {"
									+"throw iex.getCause();"
								+"}");
				}
			}

			target.addMethod(CtMethod.make("public static void _set_cloneClass (Class c) {"
												+"_cloneClass = c;"
											+"}"
											, target));
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
	protected static Class makeClass(String className) {
		ClassPool cp = ClassPool.getDefault();
		// create $Clone_"+className+"
		try {
			CtClass targetC = cp.get(className);
			targetC.setName(getCloneName(className));
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
	public static boolean defineTarget(String className, String methodName, String methodValue) {
		try {
			Class<?> oldClass = Class.forName(className);
			Class<?> newClass = define(className,methodName, methodValue);
			oldClass.getDeclaredMethod("_set_cloneClass", new Class[]{Class.class}).invoke(null, new Object[]{newClass});
//			Method m = c.getDeclaredMethod("set_Stub", Class.class);
//				m.invoke(null, define(methodName, methodValue));
//			WeaveClassList.reloadObject(oldClass, newClass);
			return true;
		} catch (Exception ex) {
//			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * クラスの動的生成
	 * 
	 * @param methodName
	 * @param methodValue
	 * @return
	 */
	protected static Class define(String className, String methodName, String methodValue) {
		ClassPool cp = ClassPool.getDefault();

		// create $Clone_"+className+"
		try {
			CtClass targetC = cp.get(getCloneName(className));
			targetC.defrost();
			CtMethod targetM = targetC.getDeclaredMethod(methodName);
			targetM.insertBefore(methodValue);

			return targetC.toClass(new Loader());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private static String getCloneName(String className) {
		String[] str = className.split("\\.");
		return "$Clone_"+str[str.length-1];
	}
	
}
