#include "ArrayList.h"

namespace appmetrica {

    ArrayList::ArrayList(JNIEnv *jniEnv, int size) : jniEnv(jniEnv), jObject(createArrayList(jniEnv, size)) {
        jclass collectionClass = jniEnv->FindClass("java/util/Collection");
        addID = jniEnv->GetMethodID(collectionClass, "add", "(Ljava/lang/Object;)Z");
    }

    void ArrayList::add(jobject jValue) {
        jniEnv->CallBooleanMethod(jObject, addID, jValue);
    }

    jobject ArrayList::createArrayList(JNIEnv *jniEnv, int size) {
        jclass jClass = jniEnv->FindClass("java/util/ArrayList");
        jmethodID jConstructor = jniEnv->GetMethodID(jClass, "<init>", "(I)V");
        return jniEnv->NewObject(jClass, jConstructor, size);
    }

} // appmetrica
