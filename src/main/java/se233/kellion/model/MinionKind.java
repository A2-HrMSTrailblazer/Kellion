package se233.kellion.model;

public enum MinionKind {
    M1("/se233/kellion/assets/M1_walk.gif",
            "/se233/kellion/assets/M1_walk2.gif",
            "/se233/kellion/assets/M1_attack.gif",
            28, MinionConfig.defaultM1()),
    M2("/se233/kellion/assets/M2_walk.gif",
            "/se233/kellion/assets/M2_walk2.gif",
            "/se233/kellion/assets/Bullet_M2.png",
            32,
            MinionConfig.defaultM2()),
    M3("/se233/kellion/assets/M3_walk.gif",
            "/se233/kellion/assets/M3_walk2.gif",
            "/se233/kellion/assets/Bullet_M3.png",
            32,
            MinionConfig.defaultM3()
    );

    public final String left, right, atk;
    public final double fitHeight;
    public final MinionConfig cfg;

    MinionKind(String left, String right, String atk, double fitHeight, MinionConfig cfg) {
        this.left = left; this.right = right; this.atk = atk;
        this.fitHeight = fitHeight;
        this.cfg = cfg;
    }

    public MinionSkin skin() {
        return MinionSkin.fromPaths(left, right, atk, fitHeight);
    }

}

