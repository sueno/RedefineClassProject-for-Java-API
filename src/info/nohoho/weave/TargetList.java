package info.nohoho.weave;

import info.nohoho.weave.annotation.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;


import javassist.ClassPool;
import javassist.CtClass;


public class TargetList {

	private static final List<CtClass> weaveList;
	private static final List<CtClass> targetList;
	private static CtClass target;

	/**
	 * initialize
	 */
	static {

		weaveList = new ArrayList<CtClass>();
		targetList = new ArrayList<CtClass>();

		try {

			String str = System.getProperty("java.class.path");
			String[] path = str.split(File.pathSeparator);
			
			searchPath(path);

		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	public static boolean searchPath(String... classPath) {
		try {
			URI[] path = new URI[classPath.length];
			for (int i = 0; i < classPath.length; ++i) {
				path[i] = new URI(classPath[i]);
			}
			searchPath(path);
		} catch (Exception ex) {
		}
		return false;
	}

	public static boolean searchPath(URI... classPath) {
		try {
			for (URI path : classPath) {
				File file = new File(path.toString());
				Pattern ptt = Pattern.compile(".*\\.jar$");
				Matcher mcc = ptt.matcher(file.getName());
				if (mcc.find()) {
					JarFile jar = new JarFile(file);
					for (Enumeration e = jar.entries(); e.hasMoreElements();) {
						ZipEntry ze = (ZipEntry) e.nextElement();
						execute(new File(ze.getName().replaceAll("/", ".")), "",path.toString());
					}
					int a = 0;
				} else {
					readFolder(file, "", path.toString());
				}
			}
		} catch (Throwable th) {
		}
		return false;
	}

	/**
	 * 
	 * @param folderPath
	 */
	private static void readFolder(File dir, String pkg, String path) {

		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (!file.exists()) {
				continue;
			} else if (file.isDirectory()) {
				readFolder(file, pkg + file.getName() + ".", path);
			} else if (file.isFile()) {
				execute(file, pkg, path);
			}
		}
	}

	/**
	 * 
	 * @param filePath
	 */
	private static void execute(File file, String pkg, String path) {
		String fl = file.getName();
		Pattern pt = Pattern.compile(".*\\.class$");
		Matcher mc = pt.matcher(fl);
		if (mc.matches()) {
			fl = fl.replaceAll("\\.class", "");
			try {
				ClassPool cp = ClassPool.getDefault();
				cp.appendClassPath(path);
				
				CtClass targetClass = cp.get(pkg + fl);

//				System.out.println(targetClass.getName());
				Object[] annList = targetClass.getAnnotations();
				for (Object ann : annList) {
					if (ann instanceof WeaveTarget) {
						weaveList.add(targetClass);
						target = targetClass;
					} else if (ann instanceof TargetClass) {
						targetList.add(targetClass);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public static List<CtClass> getWeaveList() {
		return weaveList;
	}

	public static List<CtClass> getTargets() {
		return targetList;
	}

	public static CtClass getCtClass() {
		return target;
	}

}
