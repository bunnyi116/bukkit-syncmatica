package com.github.bunnyi.syncmatica.material;

import com.github.bunnyi.syncmatica.ServerPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class SyncmaticaMaterialList {
    private ArrayList<SyncmaticaMaterialEntry> list;
    private ServerPosition deliveryPoint;

    public SyncmaticaMaterialEntry getUnclaimedEntry() {
        Optional<SyncmaticaMaterialEntry> unclaimed = list.parallelStream().filter(SyncmaticaMaterialEntry.UNFINISHED).filter(SyncmaticaMaterialEntry.UNCLAIMED).findFirst();
        return unclaimed.orElse(null);
    }

    public Collection<DeliveryPosition> getDeliveryPosition(final SyncmaticaMaterialEntry entry) {
        if (!list.contains(entry)) {
            throw new IllegalArgumentException();
        }
        DeliveryPosition delivery = new DeliveryPosition(deliveryPoint.getBlockPosition(), deliveryPoint.getDimensionId(), entry.getAmountMissing());
        ArrayList<DeliveryPosition> deliveryList = new ArrayList<>();
        deliveryList.add(delivery);
        return deliveryList;
    }
}
