package java.time.temporal;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$TemporalAdjusters$lxLY-2BERW0kc72bWr7fARmx5Z8 implements TemporalAdjuster {
    public static final /* synthetic */ -$$Lambda$TemporalAdjusters$lxLY-2BERW0kc72bWr7fARmx5Z8 INSTANCE = new -$$Lambda$TemporalAdjusters$lxLY-2BERW0kc72bWr7fARmx5Z8();

    private /* synthetic */ -$$Lambda$TemporalAdjusters$lxLY-2BERW0kc72bWr7fARmx5Z8() {
    }

    public final Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.DAY_OF_YEAR, 1).plus(1, ChronoUnit.YEARS);
    }
}
