package cn.liuboyi.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: 实体属性操作工具类
 *
 * @author: boyi.liu
 * Date: 2019/9/20 09:54
 */
public class ObjectFieldUtil {

    private static Logger log = LoggerFactory.getLogger(ObjectFieldUtil.class);

    /**
     * 根据属性名获取属性值
     *
     * @param fieldName  字段名
     * @param o 实体
     * @return
     */
    public static Object getFieldValueByName(String fieldName, Object o) {
        if (log.isDebugEnabled()) {
            log.debug("ObjectFieldUtil.getFieldValueByName(" + fieldName + ", " + o + ")");
        }
        try {
            Field field = o.getClass().getDeclaredField(fieldName);
            //设置对象的访问权限，保证对private的属性的访问
            field.setAccessible(true);
            return field.get(o);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取属性名数组
     *
     * @param o 实体
     * @return
     */
    public static String[] getFiledName(Object o) {
        if (log.isDebugEnabled()) {
            log.debug("ObjectFieldUtil.getFieldValueByName(" + o + ")");
        }
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            System.out.println(fields[i].getType());
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    /**
     * 获取属性类型(type)，属性名(name)，属性值(value)的map组成的list
     *
     * @param o 实体
     * @return
     */
    public static List<Map<String, Object>> getFiledsInfo(Object o) {
        String str = o.getClass().getSimpleName();
        str = str.substring(0,1).toLowerCase() + str.substring(1);
        System.out.println(">>>>>>>>" + str);
        if (log.isDebugEnabled()) {
            log.debug("ObjectFieldUtil.getFieldValueByName(" + o + ")");
        }
        Field[] fields = o.getClass().getDeclaredFields();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> infoMap = null;
        for (int i = 0; i < fields.length; i++) {
            infoMap = new HashMap<String, Object>();
            infoMap.put("type", fields[i].getType().toString());
            infoMap.put("name", fields[i].getName());
            infoMap.put("value", getFieldValueByName(fields[i].getName(), o));
            list.add(infoMap);
        }
        return list;
    }

    /**
     * 获取对象的所有属性值，返回一个对象数组
     *
     * @param o  实体
     * @return
     */
    public static Object[] getFiledValues(Object o) {
        if (log.isDebugEnabled()) {
            log.debug("ObjectFieldUtil.getFieldValueByName(" + o + ")");
        }
        String[] fieldNames = getFiledName(o);
        Object[] value = new Object[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            value[i] = getFieldValueByName(fieldNames[i], o);
        }
        return value;
    }

    /**
     * 根据对象属性名设置属性值
     *
     * @param fieldName 字段名
     * @param value 字段值
     * @param o 实体
     * @return
     */
    public static void setFieldValueByName(String fieldName, Object o, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("ObjectFieldUtil.getFieldValueByName(" + fieldName + ", " + o +  ", " + value + ")");
        }
        try {
            BeanInfo obj =Introspector.getBeanInfo(o.getClass(), Object.class);
            PropertyDescriptor[] pds = obj.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if(pd.getName().equals(fieldName)){
                    pd.getWriteMethod().invoke(o, value);
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
