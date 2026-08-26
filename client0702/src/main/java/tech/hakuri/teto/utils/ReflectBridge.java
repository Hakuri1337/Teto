package tech.hakuri.teto.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 手淫
 * 某个混淆器不会混任何含有java.lang.reflect和Class.*()的方法，这里简单解决一下
 * 本类可以让你的其他类不出现任何java.lang.reflect和Class.*()的引用
 * 基础类型全用大写开头的，例：Boolean.class
 * virbox我真得谢谢你了，为了适配你我写了多少东西
 */
public class ReflectBridge {

    public static Object unlock(Unsafe unsafe, Class<?> change, Class<?> want) throws Exception {
        return unsafe.getAndSetObject(change, unsafe.objectFieldOffset(Class.class.getDeclaredField("module")), want.getModule());
    }

    public static String firstMethodName(Class<?> targetClass) {
        return targetClass.getDeclaredMethods()[0].getName();
    }

    public static <ReturnType> ReturnType invoke(Class<ReturnType> returnType, Class<?> targetClass, Object instance, String methodName, Object... args) throws Exception {
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                method.setAccessible(true);
                if (returnType == null) {
                    method.invoke(instance, args);
                } else {
                    return returnType.cast(method.invoke(instance, args));
                }
            }
        }
        return null;
    }

    public static <ReturnType> ReturnType getField(Class<ReturnType> returnType, Class<?> targetClass, Object instance, String fieldName) throws Exception {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return returnType.cast(field.get(instance));
    }

    public static void setField(Class<?> targetClass, Object instance, String fieldName, Object value) throws Exception {
        Field field = targetClass.getDeclaredField(fieldName);

        //以后再研究
        //不知为何，反射在运行时明明可以直接修改final变量的值
        //Field fieldModifiersField = Field.class.getDeclaredField("modifiers");
        //fieldModifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.setAccessible(true);
        field.set(instance, value);
    }

    public static List<Object> getFields(Class<?> targetClass, Object instance) throws Exception {
        List<Object> result = new LinkedList<>();

        for (Field field : targetClass.getDeclaredFields()) {
            field.setAccessible(true);
            result.add(field.get(instance));
        }

        return result;
    }

    //谢谢你virbox
    public static Class<?> forName(String in) throws Exception {
        return Class.forName(in);
    }

    //谢谢你virbox
    public static <LeftType> LeftType cast(Class<LeftType> left, Object right) {
        return left.cast(right);
    }

    //谢谢你virbox
    public static boolean isInstance(Class<?> left, Object right) {
        return left.isInstance(right);
    }
}