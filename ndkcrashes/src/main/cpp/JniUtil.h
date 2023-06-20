#ifndef APPMETRICA_SDK_JNIUTIL_H
#define APPMETRICA_SDK_JNIUTIL_H

#include <jni.h>
#include <string>
#include <unordered_map>

namespace appmetrica {

    std::string readString(JNIEnv* env, jstring jstring);

    class BundleWrapper {
    public:

        BundleWrapper(JNIEnv* envArg, jobject objectArg);
        BundleWrapper(JNIEnv* envArg);

        std::string getString(const std::string& key);
        void putString(const std::string& key, const std::string& value);
        int getInt(const std::string& key);
        void putInt(const std::string& key, const int value);
        void putLong(const std::string& key, const int64_t value);
        bool getBoolean(const std::string& key, bool defaultValue);

        const jobject object;

    private:
        JNIEnv* env;

        jmethodID getStringID;
        jmethodID putStringID;
        jmethodID getIntID;
        jmethodID getBoolID;
        jmethodID putIntID;
        jmethodID putLongID;
    };

    class ArrayListWrapper {
    public:
        ArrayListWrapper(JNIEnv* envArg, int size);

        void add(jobject value);

        const jobject object;
    private:
        JNIEnv* env;

        jmethodID addId;
    };
}


#endif //APPMETRICA_SDK_JNIUTIL_H
