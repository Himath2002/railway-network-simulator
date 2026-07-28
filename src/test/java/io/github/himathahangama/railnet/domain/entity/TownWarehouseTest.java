package io.github.himathahangama.railnet.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TownWarehouseTest {
    @Test
    void townProducesAndTransportsFreightAgainstItsStockpile() {
        Town town = new Town("Ashford", 240);

        town.produceGoods();
        town.transportGoods(100);

        assertEquals(140, town.getStockpile());
        assertEquals(100, town.getTransportedToday());
        assertThrows(IllegalArgumentException.class, () -> town.transportGoods(141));
    }

    @Test
    void warehouseHonoursCapacityAndReturnsActualMovement() {
        Warehouse warehouse = new Warehouse("Ashford_Depot", 300);

        assertEquals(300, warehouse.storeGoods(450));
        assertEquals(300, warehouse.getStockpile());
        assertEquals(120, warehouse.takeGoods(120));
        assertEquals(180, warehouse.getStockpile());
    }
}
