package se233.kellion.model;

public final class MinionConfig {
    public final int hp;
    public final double speed;
    public final double detectRange;
    public final long fireIntervalNs;

    public MinionConfig(int hp, double speed, double detectRange, long fireIntervalNs) {
        this.hp = hp;
        this.speed = speed;
        this.detectRange = detectRange;
        this.fireIntervalNs = fireIntervalNs;
    }

    public static MinionConfig defaultM1() {
        return new MinionConfig(
                3,            // hp
                1.5,          // speed
                260,          // detect range
                1500_000_000L  // 1.5s
        );
    }

    public static MinionConfig defaultM2() {
        return new MinionConfig(
                5,            // hp
                1.5,          // speed
                260,          // detect range
                1500_000_000L  // 1.5s
        );
    }

    public static MinionConfig defaultM3() {
        return new MinionConfig(
                5,            // hp
                1.5,          // speed
                260,          // detect range
                1500_000_000L  // 1.5s
        );
    }
}
