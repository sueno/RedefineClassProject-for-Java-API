package info.nohoho.weave;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

/**
 * 変更対象クラスのリスト，またそのオブジェクトへの参照をもつ
 * 
 * @author ueno-ntnu
 * 
 */
public class WeaveClassList {

	private static final Map<String, Map<Object, Object>> objMap = Collections.synchronizedMap(new HashMap<String, Map<Object, Object>>());

	/**
	 * 変更対象となるオブジェクトの弱参照・コンストラクタの引数を登録
	 * 
	 * @param ref
	 *            登録オブジェクトの参照
	 * @param sig
	 *            コンストラクタのシグニチャー
	 * @param arg
	 *            コンストラクタの引数
	 * @return
	 */
	public static final boolean registObject(Object ref) {
		if (!objMap.containsKey(ref.getClass().getName())) {
			objMap.put(ref.getClass().getName(), Collections.synchronizedMap(new WeakHashMap<Object, Object>()));
		}
		objMap.get(ref.getClass().getName()).put(ref, ref.getClass());
		return false;
	}

	/**
	 * 
	 * @param targetClass
	 * @return
	 */
	public static final boolean reloadObject(Class<?> proxyClass, Class<?> targetClass) {
		try {
			Method m = proxyClass.getDeclaredMethod("set_Stub", Class.class);
			Map<Object, Object> map = objMap.get(targetClass.getName());
			synchronized (map) {
				for (Entry<Object,Object> e : map.entrySet()) {
					m.invoke(e.getKey(), targetClass);
				}
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static final Map<Object, Object> getMap (String key) {
		return objMap.get(key);
	}
}
