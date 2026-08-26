//请把下面这行#define _CRT_SECURE_NO_WARNINGS也加到pch.h里（加在第一行就行）（必须）
#define _CRT_SECURE_NO_WARNINGS
#include "pch.h"

#include <iostream>


//都在你的JDK安装路径下直接搜索
#include "jni.h"
#include "jvmti.h"
#include "jni_md.h"



jvmtiEnv* jvmti_env_global;//env只能取一次，不要反复取新的，重要



jclass ModifierClass;
jmethodID ModifierMethod;





//这个方法的定义是jdk内定的，详情：https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html#ClassFileLoadHook
void ClassFileLoadEventProcessor(jvmtiEnv* jvmti_env, JNIEnv* jni_env,
	jclass captured_class, jobject its_own_classloader, const char* its_name, jobject its_protection_domain, jint its_data_len, const unsigned char* its_data, jint* replace_with_len, unsigned char** replace_with) {

	if (captured_class == NULL)return;//此行及其神秘，勿删
	//当捕获到Java内部自带的类时，its_own_classloader固定为NULL（代表BootstrapClassLoader）
	//你就是想重定义String.class都行

	std::cout << "[ClassFileLoadHook]" << its_name << std::endl;

	//下面都抄的法国开挂哥Lefraudeur还有他的Mujina的
	jbyteArray old = jni_env->NewByteArray(its_data_len);
	jni_env->SetByteArrayRegion(old, 0, its_data_len, (jbyte*)its_data);


	//在JNI，同一行Java代码变成C代码就是要变大好几倍，没办法，注：在JNI层，Java层的三大访问控制修饰符（Accessor）即public protected privite概念完全不存在（似乎在JVM层运行时就不存在了，反射也是多余的，手动写一个class就可以不用反射），即JNI层无须也不存在反射（反射的两大功能之一一个就是绕private，另一个是查找类以及类的成员（方法加字段）），说明Java的访问控制修饰符在JVM底层完全没有必要存在，泛型同理，JNI不存在泛型
	jbyteArray result = (jbyteArray)jni_env->CallStaticObjectMethod(ModifierClass, ModifierMethod, captured_class, its_own_classloader, jni_env->NewStringUTF(its_name), its_protection_domain, old);


	//法国开挂哥的惊世骇俗之笔
	unsigned char* new_;
	jsize new_size = jni_env->GetArrayLength(result);

	jvmti_env->Allocate(new_size, &new_);
	jni_env->GetByteArrayRegion(result, 0, new_size, (jbyte*)new_);

	*replace_with = new_;
	*replace_with_len = new_size;
}



//能用（打印JVM暴毙前的堆栈（无效请排查Windows层））
void VMDeathEventProcessor(jvmtiEnv* jvmti_env, JNIEnv* jni_env) {//全抄的 https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html#GetAllStackTraces
	jvmtiStackInfo* stack_info;
	jint thread_count;

	jvmtiError err = jvmti_env->GetAllStackTraces(1024, &stack_info, &thread_count);
	char* err_str;
	jvmti_env->GetErrorName(err, &err_str);
	std::cout << "[VMDeath]" << err_str << std::endl;


	for (int i = 0; i < thread_count; ++i) {
		jvmtiStackInfo* curStack = &stack_info[i];
		jthread curThread = curStack->thread;
		jint curState = curStack->state;
		jvmtiFrameInfo* curFrames = curStack->frame_buffer;

		jvmtiThreadInfo curThreadInfo;
		jvmti_env->GetThreadInfo(curThread, &curThreadInfo);

		std::cout << "[Thread]" << curThreadInfo.name << std::endl;

		for (int j = 0; j < curStack->frame_count; j++) {
			char* methodName;
			char* methodSign;
			char* what;
			jvmti_env->GetMethodName(curFrames[j].method, &methodName, &methodSign, &what);

			jclass methodClass;
			jvmti_env->GetMethodDeclaringClass(curFrames[j].method, &methodClass);

			char* methodClassName;
			char* what2;
			jvmti_env->GetClassSignature(methodClass, &methodClassName, &what2);

			std::cout << "[Class]" << methodClassName << "[Method]" << methodName << methodSign << std::endl;
		}
	}
	jvmti_env->Deallocate((unsigned char*)stack_info);
}

void Setup(jvmtiEnv* jvmti_env) {
	jvmtiCapabilities cap;
	jvmti_env->GetPotentialCapabilities(&cap);



	jvmtiError err = jvmti_env->AddCapabilities(&cap);
	char* err_str;
	jvmti_env_global->GetErrorName(err, &err_str);
	std::cout << "[AddCapabilities]" << err_str << std::endl;



	jvmtiEventCallbacks callbacks{};
	callbacks.VMDeath = VMDeathEventProcessor;
	callbacks.ClassFileLoadHook = ClassFileLoadEventProcessor;
	err = jvmti_env->SetEventCallbacks(&callbacks, sizeof(jvmtiEventCallbacks));
	jvmti_env_global->GetErrorName(err, &err_str);
	std::cout << "[SetEventCallbacks]" << err_str << std::endl;



	err = jvmti_env->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, 0);
	jvmti_env_global->GetErrorName(err, &err_str);
	std::cout << "[SetEventNotificationMode][JVMTI_ENABLE][JVMTI_EVENT_VM_DEATH]" << err_str << std::endl;




}


//hook取jnienv的方法不能用于jvmtienv（我不会）
JNIEXPORT jint JNI_OnLoad(JavaVM* java_vm, void* reserved) {//正宗老JNI初始化函数，全抄自JDK文档，System.load()加载DLL必先在DLL导出表（Win知识）搜索JNI_OnLoad调用，就跟Windows加载一个DLL使用的LoadLibrary函数加载完DLL必先去其导出表里搜EntryPoint执行一样的（DllMain疑似在编译时被翻译成EntryPoint），所以理论上如果本文件存在DllMain，DllMain会比此函数更先执行？
	java_vm->GetEnv((void**)&jvmti_env_global, JVMTI_VERSION);//不知道网易咋搞的现在又能直接拿Env了
	Setup(jvmti_env_global);//打开钩子开关
	return JNI_VERSION_1_8;
}
extern "C" {//这一小块是用javac -h . 你的源码.java生成的（别漏掉中间那个点）
	JNIEXPORT void JNICALL Java_a_a_a(JNIEnv* jni_env, jclass caller, jclass arg1) {//注意如果这个方法是静态的，那caller是一个jclass，如果不是，那caller就是jobject
		//这个方法的二参接收一个数组，然而我们都知道数组在C里就是一个地址加上读取长度，也就是一个指针，所以直接取它的地址，长度就是一个jclass数据结构的长度所以不用填，所以侧面看出jclass这个数据结构也是引用，并不是真正类文件的原始内容

		ModifierClass = caller;
		ModifierMethod = jni_env->GetStaticMethodID(caller, "a", "(Ljava/lang/Class;Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/security/ProtectionDomain;[B)[B");//靠后面这一大串是叫Java类型简写还是叫JVM方法签名来着的，很简单，几分钟就可以上手，记得L不要漏;分号


		jvmtiError err = jvmti_env_global->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, 0);
		char* err_str;
		jvmti_env_global->GetErrorName(err, &err_str);
		std::cout << "[SetEventNotificationMode][JVMTI_ENABLE][JVMTI_EVENT_CLASS_FILE_LOAD_HOOK]" << err_str << std::endl;


		err = jvmti_env_global->RetransformClasses(1, &arg1);
		jvmti_env_global->GetErrorName(err, &err_str);
		std::cout << "[RetransformClasses]" << err_str << std::endl;


		err = jvmti_env_global->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, 0);
		jvmti_env_global->GetErrorName(err, &err_str);
		std::cout << "[SetEventNotificationMode][JVMTI_DISABLE][JVMTI_EVENT_CLASS_FILE_LOAD_HOOK]" << err_str << std::endl;
	}
}

