#ifndef APPMETRICA_SDK_ARRAYLIST_H
#define APPMETRICA_SDK_ARRAYLIST_H

#include <jni.h>

namespace appmetrica {

    class ArrayList {
    public:
        const jobject jObject;

        ArrayList(JNIEnv *jniEnv, int size);

        void add(jobject jValue);

    private:
        JNIEnv *jniEnv;

        jmethodID addID;

        static jobject createArrayList(JNIEnv *jniEnv, int size);
    };

} // appmetrica

#endif //APPMETRICA_SDK_ARRAYLIST_H
