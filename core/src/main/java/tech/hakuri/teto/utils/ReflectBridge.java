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

    /*
     * 下面三个 *Any 变体解决一个跨版本的坑：
     * 1.20.1 Forge 运行时用 srg 名（字段叫 f_91011_），1.21.8 NeoForge 运行时用 official 名
     * （同一个字段叫 rightClickDelay）。反射用的名字是字符串，编译期不会报错，
     * 但拿错名字会在运行时抛 NoSuchFieldException。
     * 传入两个候选名（srg + official），谁在就用谁，同一份源码就能在两个版本上跑。
     */

    public static <ReturnType> ReturnType getFieldAny(Class<ReturnType> returnType, Class<?> targetClass, Object instance, String... names) throws Exception {
        return returnType.cast(resolveField(targetClass, names).get(instance));
    }

    public static void setFieldAny(Class<?> targetClass, Object instance, Object value, String... names) throws Exception {
        resolveField(targetClass, names).set(instance, value);
    }

    public static <ReturnType> ReturnType invokeAny(Class<ReturnType> returnType, Class<?> targetClass, Object instance, String... names) throws Exception {
        for (Method method : targetClass.getDeclaredMethods()) {
            for (String name : names) {
                if (method.getName().equals(name)) {
                    method.setAccessible(true);
                    Object result = method.invoke(instance);
                    return returnType == null ? null : returnType.cast(result);
                }
            }
        }
        throw new NoSuchMethodException(targetClass.getName() + " 上找不到任何候选方法：" + String.join(" / ", names));
    }

    private static Field resolveField(Class<?> targetClass, String... names) throws NoSuchFieldException {
        for (String name : names) {
            try {
                Field field = targetClass.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // 试下一个候选名
            }
        }
        throw new NoSuchFieldException(targetClass.getName() + " 上找不到任何候选字段：" + String.join(" / ", names));
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