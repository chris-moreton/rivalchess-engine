class RivalLibrary {
    static inline int linearScale(int x, int min, int max, int a, int b) {
        if (x < min) return a;
        if (x > max) return b;
        return a + (x - min) * (b - a) / (max - min);
    }
}
