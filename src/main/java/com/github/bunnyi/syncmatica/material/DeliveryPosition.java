package com.github.bunnyi.syncmatica.material;

import com.github.bunnyi.syncmatica.ServerPosition;
import com.github.bunnyi.syncmatica.util.BlockPos;

public class DeliveryPosition extends ServerPosition {

    private final int amount;

    public DeliveryPosition(final BlockPos pos, final String dim, final int amount) {
        super(pos, dim);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
