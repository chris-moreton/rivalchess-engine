#include <jni.h>
#include <stdio.h>

JNIEXPORT void JNICALL
Java_HelloWorld_printy(JNIEnv *env, jobject obj)
{
    printf("Hello World!\n");
    return;
}