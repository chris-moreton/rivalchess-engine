#include <jni.h>
#include <stdio.h>

JNIEXPORT jint JNICALL
Java_RivalCLibrary_linearScaleC(JNIEnv *env, jobject obj, jint x, jint min, jint max, jint a, jint b)
{
   if (x < min) return a;
   if (x > max) return b;
   return a + (x - min) * (b - a) / (max - min);
}
