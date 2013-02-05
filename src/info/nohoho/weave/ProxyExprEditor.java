package info.nohoho.weave;

import javassist.CannotCompileException;
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
			m.replace("{"
					+"System.err.println(\"##WEAVE##  Call Method : "+m.getClassName()+" ."+m.getMethodName()+"\");"
					+"$_ = \"残念！hogeしか返さないよー！\";"
					+"/**$_ = $proceed($$);*/"
					+"}");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		}
	}
}
