package se233.kellion.model;

public final class MinionFactory {
    private MinionFactory() {}

    public static Minion create(MinionKind kind, double x, double y, double patrolL, double patrolR) {
        return switch (kind) {
            case M1 -> new Minion(x, y, patrolL, patrolR, kind.skin(), kind.cfg,kind);
            case M2 -> new Minion2(x, y, patrolL, patrolR, kind);
            case M3 -> new Minion3(x, y, patrolL, patrolR, kind);
        };
    }
}
