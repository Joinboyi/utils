package com.emcoo.rest.modules.basemodules.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: 获取包下的所有类
 *
 * @author: boyi.liu
 * Date: 2019/9/21 14:18
 */
public class PackageUtil {

	private static List<String> list = new ArrayList<>();

	public static void main(String[] args) {
		String packageName = "com.emcoo.rest.modules.basemodules.dto.base.BaseModel.systemBase";

		List<String> classNames = getClassName(packageName);
		HashSet h = new HashSet(classNames);
		classNames.clear();
		classNames.addAll(h);
		classNames = classNames.stream().sorted(Comparator.comparing(str -> str)).collect(Collectors.toList());
		for (String className : classNames) {
			soutObject(className);
		}
		for (String str : list) {
			System.out.println(str);
		}
	}

	public static List<String> getClassName(String packageName) {
		String filePath = ClassLoader.getSystemResource("").getPath() + packageName.replace(".", "/");
		List<String> fileNames = getClassName(filePath, null);
		return fileNames;
	}

	private static List<String> getClassName(String filePath, List<String> className) {
		List<String> myClassName = new ArrayList<String>();
		File file = new File(filePath);
		File[] childFiles = file.listFiles();
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				myClassName.addAll(getClassName(childFile.getPath(), myClassName));
			} else {
				String childFilePath = childFile.getPath();
				childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."));
				childFilePath = childFilePath.replace("\\", ".");
				childFilePath = childFilePath.replace("mcoo-14/www/eat/emcoo-rest/emcoo-rest-model/target/classes/com/emcoo/rest/model/dto/BaseModel/systemBase/", "");
				myClassName.add(childFilePath);
			}
		}

		return myClassName;
	}

	private static void soutObject(String className) {
		Class clazz = null;
		try {
			clazz = Class.forName("com.emcoo.rest.modules.basemodules.dto.base.BaseModel.systemBase." + className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String[] zd = new String[0];
		try {
			zd = ObjectFieldUtil.getFiledName(clazz.newInstance());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		list.add("########################################");
		list.add("# " + className);
		list.add("########################################");
//		System.out.println("########################################");
//		System.out.println("# " + className);
//		System.out.println("########################################");
		String name = clazz.getSimpleName();
		name = name.substring(0,1).toLowerCase() + name.substring(1);
		for (String str : zd) {
			list.add(name + "." + str + "=");
//			System.out.println(name + "." + str + "=");
		}
		list.add("");
//		System.out.println();
	}

}
