package io.github.himathahangama.railnet.network;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.domain.factory.RailwayFactory;
import io.github.himathahangama.railnet.domain.factory.TownFactory;
import io.github.himathahangama.railnet.domain.factory.WarehouseFactory;
import io.github.himathahangama.railnet.domain.state.RailwayStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkManagerTest {
    @Test
    void managesACompleteRailwayLifecycleAndObserverNotifications() {
        NetworkManager network = network();
        AtomicInteger notifications = new AtomicInteger();
        network.addObserver(notifications::incrementAndGet);

        network.addTown("Northport", 400);
        network.addTown("Southport", 500);
        network.addRailway("Northport", "Southport");
        Railway railway = network.getRailways().getFirst();

        for (int day = 0; day < NetworkManager.CONSTRUCTION_DAYS; day++) {
            railway.dayPassed();
        }
        assertEquals(RailwayStatus.SINGLE_TRACK, railway.getState().status());
        assertTrue(network.isRailwayEligibleForDuplication("Southport", "Northport"));

        network.duplicateRailway("Southport", "Northport");
        for (int day = 0; day < NetworkManager.UPGRADE_DAYS; day++) {
            railway.dayPassed();
        }

        assertEquals(RailwayStatus.DUAL_TRACK, railway.getState().status());
        assertTrue(notifications.get() >= 5);
    }

    @Test
    void rejectsDuplicateConnectionsRegardlessOfDirection() {
        NetworkManager network = network();
        network.addTown("Northport", 400);
        network.addTown("Southport", 500);
        network.addRailway("Northport", "Southport");

        assertThrows(
                IllegalArgumentException.class,
                () -> network.addRailway("Southport", "Northport"));
    }

    @Test
    void transportsFromTownThenWarehouseReserves() {
        NetworkManager network = network();
        network.addTown("Northport", 40);
        network.addWarehouse("Northport", "Northport_Depot", 200);
        Town town = network.getTowns().getFirst();
        town.produceGoods();
        town.getWarehouses().getFirst().storeGoods(90);

        assertEquals(100, network.transportGoodsFromTown(town, 100));
        assertEquals(0, town.getStockpile());
        assertEquals(30, town.getWarehouses().getFirst().getStockpile());
    }

    private NetworkManager network() {
        return new NetworkManager(
                new TownFactory(),
                new RailwayFactory(),
                new WarehouseFactory());
    }
}
