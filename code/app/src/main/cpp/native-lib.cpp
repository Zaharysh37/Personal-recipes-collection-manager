#include <jni.h>
#include <string>

extern "C" JNIEXPORT jboolean JNICALL
Java_com_byshnev_recipebook_activities_SignInActivity_stringsAreEqual(
        JNIEnv* env,
        jobject,
        jstring str1,
        jstring str2) {
    // Конвертация jstring в сишные строки
    const char* nativeStr1 = env->GetStringUTFChars(str1, nullptr);
    const char* nativeStr2 = env->GetStringUTFChars(str2, nullptr);

    bool result = std::string(nativeStr1) == std::string(nativeStr2);

    // Освободить память
    env->ReleaseStringUTFChars(str1, nativeStr1);
    env->ReleaseStringUTFChars(str2, nativeStr2);

    return static_cast<jboolean>(result);
}
