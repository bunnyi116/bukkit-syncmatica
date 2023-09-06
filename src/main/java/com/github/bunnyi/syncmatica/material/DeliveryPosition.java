package com.github.bunnyi.syncmatica.material;

import com.github.bunnyi.syncmatica.ServerPosition;
import com.github.bunnyi.syncmatica.util.BlockPos;

public class DeliveryPosition extends ServerPosition {

    private final int amount;

    public DeliveryPosition(BlockPos pos, String dim, int amount) {
        super(pos, dim);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
