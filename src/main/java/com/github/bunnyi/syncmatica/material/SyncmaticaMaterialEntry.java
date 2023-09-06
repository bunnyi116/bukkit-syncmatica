package com.github.bunnyi.syncmatica.material;

import java.util.function.Predicate;

public class SyncmaticaMaterialEntry {
    public static final Unclaimed UNCLAIMED = new Unclaimed();
    public static final Unfinished UNFINISHED = new Unfinished();

    private int amountRequired;
    private int amountPresent;
    private String claimedBy;

    public int getAmountRequired() {
        return amountRequired;
    }

    public int getAmountPresent() {
        return amountPresent;
    }

    public int getAmountMissing() {
        return amountRequired - amountPresent;
    }

    public boolean isClaimed() {
        return claimedBy != null;
    }

    public boolean isFinished() {
        return amountPresent >= amountRequired;
    }

    public static class Unclaimed implements Predicate<SyncmaticaMaterialEntry> {
        @Override
        public boolean test(SyncmaticaMaterialEntry arg0) {
            return !arg0.isClaimed();
        }
    }

    public static class Unfinished implements Predicate<SyncmaticaMaterialEntry> {
        @Override
        public boolean test(SyncmaticaMaterialEntry arg0) {
            return !arg0.isFinished();
        }
    }
}
