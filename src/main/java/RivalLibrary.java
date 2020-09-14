import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Platform(include="RivalLibrary.h")
public class RivalLibrary {

    public static native int linearScale(int x, int min, int max, int a, int b);

    public static void main(String[] args) {
        // Pointer objects allocated in Java get deallocated once they become unreachable,
        // but C++ destructors can still be called in a timely fashion with Pointer.deallocate()
        System.out.println(linearScale(1,2,3,4,5));
    }
}