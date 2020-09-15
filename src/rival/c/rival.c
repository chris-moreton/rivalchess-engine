#include <jni.h>
#include <stdio.h>

JNIEXPORT jint JNICALL
Java_RivalCLibrary_linearScaleC(JNIEnv *env, jobject obj, jint x, jint min, jint max, jint a, jint b)
{
   if (x < min) return a;
   if (x > max) return b;
   return a + (x - min) * (b - a) / (max - min);
}

JNIEXPORT jint JNICALL
Java_RivalCLibrary_getHighestScoringMoveFromArrayC(JNIEnv *env, jobject obj, jint *theseMoves)
{
    int bestIndex = -1;
    int best = 1000000;
    int c = -1;
    while (theseMoves[++c] != 0) {
        if (theseMoves[c] != -1 && theseMoves[c] < best && (theseMoves[c] >> 24 != 127)) {
            // update best move found so far, but don't consider moves with no score
            best = theseMoves[c];
            bestIndex = c;
        }
    }
    if (best == 1000000) {
       return 0;
    } else {
        theseMoves[bestIndex] = -1;
        return best & 0x00FFFFFF;
    }
}

unsigned long southFill(unsigned long bitboard) {
    unsigned long a = bitboard | (bitboard >> 8);
    unsigned long b = a | (a >> 16);
    return b | (b >> 32);
}

unsigned long openFiles(unsigned long kingShield, unsigned long pawnBitboard) {
   return southFill(kingShield) & ~southFill(pawnBitboard) & 0x00000000000000FF;
}

JNIEXPORT jint JNICALL
Java_RivalCLibrary_openFilesKingShieldEvalC(JNIEnv *env, jobject obj, jlong kingShield, jlong pawnBitboard)
{
    unsigned long openFilesBitboard = openFiles(kingShield, pawnBitboard);
    if (openFilesBitboard != 0) {
        return 25 * __builtin_popcount(openFilesBitboard & 0xE7) +
                10 * __builtin_popcount(openFilesBitboard & 0x18);
    }
    return 0;
}

