package info.nohoho.weave;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Loader;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

@SuppressWarnings("all")
public class Weave {

	/**
	 * 再定義可能なクラスの一覧
	 */
	private static final Map<String,Class> original_Map = new HashMap<String, Class>();

	/**
	 * 実行前処理
	 * 
	 * @param inst
	 * @param args
	 */
	public static void premain(Instrumentation inst, String args[]) {
//		TargetList.getTargets();
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
			original_Map.put(className, cl);

			/**
			 * 対象クラスをProxy化
			 */
			ClassPool cp = ClassPool.getDefault();
//			cp.importPackage("info.nohoho.weave");
			CtClass target = cp.get(className);
			
			// 実体のインスタンスを保持するフィールドを定義
			target.addField(CtField.make("private static java.lang.Class _cloneClass = "+cl.getName()+".class;", target));
			target.addField(CtField.make("private java.lang.Object _stub_clone;", target));
			
			// コンストラクタの引数を保持するフィールドを定義
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
							+"Object oldObject = _stub_clone;"
							+"if (_const_sig!=null&&_const_sig.length!=0) {"
								+"_stub_clone = stubClass.getConstructor(_const_sig).newInstance(_const_param);"
							+"} else {"
								+"_stub_clone = stubClass.newInstance();"
							+"}"
							+"info.nohoho.weave.Weave.extendsFields(oldObject,_stub_clone);"
						+"}",
						target));
			
			// 対象クラスのメソッドの処理を置き換え
			// 実体インスタンスへのメソッド呼び出しを行う処理に置き換え(リフレクションで呼び出す)
			CtMethod[] methods = target.getDeclaredMethods();
			for (CtMethod m : methods) {
				String fieldcheck = "";
				String targetObj = "null";
				if (!Modifier.isStatic(m.getModifiers())) {
					fieldcheck = ""
							+"if (!_stub_clone.getClass().equals(_cloneClass)) {"
								+"_set_Stub(_cloneClass);"
							+"}"
							;
					targetObj = "_stub_clone";
				}
				if (m.getName().equals("_set_Stub")) {
				}else{
					m.setBody(
								"try{"
									+ fieldcheck
									+"return ($r)_cloneClass.getDeclaredMethod(\"" + m.getName() + "\",$sig).invoke("+targetObj+", $args);"
								+"}catch(java.lang.reflect.InvocationTargetException iex) {"
									+"throw iex.getCause();"
								+"}");
				}
			}
			
			// インスタンスを生成するためのクラスを設定するメソッドを追加
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
	
	public static boolean redefineable(String className, String targetClassName) {
		String cloneName = getCloneName(className+"__"+targetClassName);
		ClassPool cp = ClassPool.getDefault();
		try {
			//change targetClass Method invocation
			CtClass target = cp.get(targetClassName);
			CtMethod[] methods = target.getDeclaredMethods();
			for (CtMethod m : methods) {
				m.instrument(new ProxyExprEditor(className,cloneName));
			}
			target.toClass(Thread.currentThread().getContextClassLoader());
			
			// Make Dummy Class
			CtClass apiClass = cp.get(className);
			CtClass dummy = cp.makeClass(cloneName);
			for (CtMethod m : apiClass.getDeclaredMethods()) {
				StringBuilder param = new StringBuilder();
				CtClass[] paramType = m.getParameterTypes();
				for (int i=0;i<paramType.length;++i) {
					param.append(paramType[i].getName()+" $"+(int)(i+1)+" ");
				}
				System.out.println(m.getReturnType().getName() +" "+m.getName()+" ("+param+")");
				try {
					dummy.addMethod(CtMethod.make(""
							+"public static "+m.getReturnType().getName()+" "+m.getName()+"("+param+") {"
								+"throw new AbstractMethodError();"
							+"}"
							, dummy));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			Class cd = dummy.toClass(Thread.currentThread().getContextClassLoader());
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
			return makeClass(targetC);
		} catch (Exception ex) {
			return null;
		}
	}
	protected static Class makeClass(CtClass targetC) {
		try{
			targetC.setName(getCloneName(targetC.getName()));
			return targetC.toClass(Thread.currentThread().getContextClassLoader());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static boolean resetTarget(String className) {
		try {
			Class.forName(className).getDeclaredMethod("_set_cloneClass", new Class[]{Class.class}).invoke(null, new Object[]{original_Map.get(className)});
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}
	
	/**
	 * 引数に渡せれたメソッドの処理を，第2引数のソースに変更
	 * 
	 * @param methodName
	 * @param methodValue
	 */
	public static boolean defineTarget(String className, String methodName, Class[] param, String src) {
		try {
			Method m = Class.forName(className).getDeclaredMethod(methodName,param);
			return defineTarget(m, src);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	public static boolean defineTarget(Method m, String src) {
		try {
			Class<?> newClass = define(m, src);
			m.getDeclaringClass().getDeclaredMethod("_set_cloneClass", new Class[]{Class.class}).invoke(null, new Object[]{newClass});
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
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
	protected static Class define(Method m, String src) throws NotFoundException, CannotCompileException {
		ClassPool cp = ClassPool.getDefault();
		
		// create $Clone_"+className+"
		CtClass targetC = cp.get(getCloneName(m.getDeclaringClass().getName()));
		targetC.defrost();
		CtMethod targetM = targetC.getDeclaredMethod(m.getName());
		targetM.insertBefore(src);
		return targetC.toClass(new Loader());
	}

	protected static String getCloneName(String className) {
		String str = className.replaceAll("\\.", "_");
		return "$Clone_"+str;
	}
	protected static String getFieldName(String methodName, CtClass... paramSig) {
		StringBuilder sb = new StringBuilder();
		sb.append(methodName);
		for (CtClass pSig : paramSig) {
			sb.append("$" + pSig.getName().replaceAll("\\.|\\[|\\]", "_"));
		}
		return sb.toString();
	}
	
	public static boolean extendsFields (Object oldObject, Object newObject) {
		try {
			Field[] fields = oldObject.getClass().getDeclaredFields();
			for (Field f : fields) {
				f.setAccessible(true);
				Field newfield = newObject.getClass().getDeclaredField(f.getName());
				newfield.setAccessible(true);
				newfield.set(newObject, f.get(oldObject));
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
}
