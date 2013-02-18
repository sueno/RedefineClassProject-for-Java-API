package info.nohoho.weave;

import java.lang.reflect.Field;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class ProxyExprEditor extends ExprEditor {

	private String className;
	private String cloneName;
	
	public ProxyExprEditor(String className, String cloneName) {
		super();
		this.className = className;
		this.cloneName = cloneName;
	}
	
	@Override
	public void edit(ConstructorCall c) throws CannotCompileException {
		if (c.getClassName().equals(className)) {
			c.replace("{"
					+"System.out.println(\"Call Constructor : \");"
					+"super();"
					+"}");
		}
	}
	
	@Override
	public void edit(MethodCall m) throws CannotCompileException {
		if (m.getClassName().equals(className)) {
			try {
				Field[] f = Class.forName(cloneName).getDeclaredFields();
				m.replace("{"
					+"System.err.println(\"##WEAVE##  Call Method : "+m.getClassName()+" ."+m.getMethodName()+"\");"
					+"Class[] __param = new Class[$sig.length+3];"
					+"__param[0] = Object.class;"
					+"__param[1] = Class[].class;"
					+"__param[2] = Object[].class;"
					+"System.arraycopy($sig,0,__param,1,$sig.length);"
					+"Object[] __args = new Object[$args.length+3];"
					+"__args[0] = $0;"
					+"__args[1] = $sig;"
					+"__args[2] = $args;"
					+"System.arraycopy($args,0,__args,1,$args.length);"
//					+"boolean flag = (boolean)Class.forName(\""+cloneName+"\").getDeclaredMethod(\""+m.getMethodName()+"_Exist\",$sig).invoke(null,$args);"
//					+"$_ = null;"
//					+"if ((boolean)(Class.forName(\""+cloneName+"\").getDeclaredField(\"" + Weave.getFieldName(m.getMethodName(), m.getMethod().getParameterTypes()) + "\").get(null))) {"
						+"$_ = ($r)Class.forName(\""+cloneName+"\").getDeclaredMethod(\""+m.getMethodName()+"\",__param).invoke(null,__args);"
//						+"$_ = $proceed($$);"
//					+"}"
//					+"$_ = $proceed($$);"
					+"}");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
