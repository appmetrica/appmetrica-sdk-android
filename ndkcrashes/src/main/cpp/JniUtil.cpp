#include "JniUtil.h"

using namespace std;

namespace appmetrica {

    string readString(JNIEnv* env, jstring jstring) {
        jboolean isCopy = true;
        const char* cstr = env->GetStringUTFChars(jstring, &isCopy);
        auto stdstring = string(cstr);

        env->ReleaseStringUTFChars(jstring, cstr);

        return stdstring;
    }

    BundleWrapper::BundleWrapper(JNIEnv* envArg, jobject objectArg): env(envArg), object(objectArg)  {
        auto bundleClass = env->FindClass("android/os/Bundle");
        getStringID = env->GetMethodID(bundleClass, "getString", "(Ljava/lang/String;)Ljava/lang/String;");
        putStringID = env->GetMethodID(bundleClass, "putString", "(Ljava/lang/String;Ljava/lang/String;)V");
        getIntID = env->GetMethodID(bundleClass, "getInt", "(Ljava/lang/String;)I");
        getBoolID = env->GetMethodID(bundleClass, "getBoolean", "(Ljava/lang/String;Z)Z");
        putIntID = env->GetMethodID(bundleClass, "putInt", "(Ljava/lang/String;I)V");
        putLongID = env->GetMethodID(bundleClass, "putLong", "(Ljava/lang/String;J)V");
    }

    BundleWrapper::BundleWrapper(JNIEnv* envArg): BundleWrapper(
            envArg,
            envArg->NewObject(
                    envArg->FindClass("android/os/Bundle"),
                    envArg->GetMethodID(envArg->FindClass("android/os/Bundle"), "<init>", "()V")
            )
    ) { }

    string BundleWrapper::getString(const string& key) {
        auto keyJ = env->NewStringUTF(key.c_str());
        auto result = (jstring) env->CallObjectMethod(object, getStringID, keyJ);
        env->DeleteLocalRef(keyJ);
        return readString(env, result);
    }

    void BundleWrapper::putString(const string& key, const string& value) {
        auto keyJ = env->NewStringUTF(key.c_str());
        auto valueJ = env->NewStringUTF(value.c_str());
        env->CallVoidMethod(object, putStringID, keyJ, valueJ);
        env->DeleteLocalRef(keyJ);
        env->DeleteLocalRef(valueJ);
    }

    int BundleWrapper::getInt(const string& key) {
        auto keyJ = env->NewStringUTF(key.c_str());
        auto result = (jint) env->CallIntMethod(object, getIntID, keyJ);
        env->DeleteLocalRef(keyJ);
        return result;
    }

    void BundleWrapper::putInt(const string& key, const int value) {
        auto keyJ = env->NewStringUTF(key.c_str());
        env->CallVoidMethod(object, putIntID, keyJ, value);
        env->DeleteLocalRef(keyJ);
    }

    void BundleWrapper::putLong(const string& key, const int64_t value) {
        auto keyJ = env->NewStringUTF(key.c_str());
        jlong jval = value;
        env->CallVoidMethod(object, putLongID, keyJ, jval);
        env->DeleteLocalRef(keyJ);
    }

    bool BundleWrapper::getBoolean(const std::string &key, bool defaultValue) {
        auto keyJ = env->NewStringUTF(key.c_str());
        return env->CallBooleanMethod(object, getBoolID, keyJ, defaultValue ? JNI_TRUE : JNI_FALSE) == JNI_TRUE;
    }

    ArrayListWrapper::ArrayListWrapper(JNIEnv *envArg, int size): env(envArg), object(
            envArg->NewObject(
                    envArg->FindClass("java/util/ArrayList"),
                    envArg->GetMethodID(envArg->FindClass("java/util/ArrayList"), "<init>", "(I)V"), size)
            ), addId(envArg->GetMethodID(envArg->FindClass("java/util/Collection"), "add", "(Ljava/lang/Object;)Z")) {
    }

    void ArrayListWrapper::add(jobject value) {
        env->CallBooleanMethod(object, addId, value);
    }
}
