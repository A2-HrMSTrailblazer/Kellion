package se233.kellion.util;

public final class TotalScore {
    private static int total = 0;
    private TotalScore() {}

    public static void reset() { total = 0; }
    public static void addStageScore(int s) { total += s; }
    public static int get() { return total; }
}
