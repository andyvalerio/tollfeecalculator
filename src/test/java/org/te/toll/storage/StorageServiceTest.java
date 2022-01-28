package org.te.toll.storage;

import org.junit.jupiter.api.Test;
import org.te.toll.enums.VehicleType;
import org.te.toll.exceptions.MultipleVehicleTypesException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceTest {

    StorageService storage = new StorageServiceInMemoryImpl();

    @Test
    void whenAddingFirstPassage_ItCanBeRetrievedFromStorage() {
        ZonedDateTime timeOfPassage = ZonedDateTime.now();
        storage.recordPassage("xyz", VehicleType.Car, timeOfPassage);
        List<ZonedDateTime> passages = storage.getPassages("xyz", ZonedDateTime.now());
        assertTrue(passages.get(0).isEqual(timeOfPassage));
    }

    @Test
    void whenLookingForWrongRegistration_NoPassagesAreRetrieved() {
        ZonedDateTime timeOfPassage = ZonedDateTime.now();
        storage.recordPassage("xyz_2", VehicleType.Car, timeOfPassage);
        List<ZonedDateTime> passages = storage.getPassages("xyz_3", ZonedDateTime.now());
        assertTrue(passages.isEmpty());
    }

    @Test
    void whenPassagesInDifferentDays_OnlyCorrectDayIsRetrieved() {
        ZonedDateTime timeOfPassage_1 = ZonedDateTime.now();
        ZonedDateTime timeOfPassage_2 = ZonedDateTime.now().plus(1, ChronoUnit.DAYS);
        storage.recordPassage("xyz_4", VehicleType.Car, timeOfPassage_1);
        storage.recordPassage("xyz_4", VehicleType.Car, timeOfPassage_2);
        List<ZonedDateTime> passages = storage.getPassages("xyz_4", timeOfPassage_2);
        assertTrue(passages.get(0).isEqual(timeOfPassage_2));
        assertEquals(1, passages.size());
    }

    @Test
    void whenVehicleHasOneTypeTheSameDay_TypeIsRetrieved() throws MultipleVehicleTypesException {
        ZonedDateTime timeOfPassage_1 = ZonedDateTime.now();
        ZonedDateTime timeOfPassage_2 = ZonedDateTime.now().plus(1, ChronoUnit.MINUTES);
        storage.recordPassage("xyz_5", VehicleType.Car, timeOfPassage_1);
        storage.recordPassage("xyz_5", VehicleType.Car, timeOfPassage_2);
        VehicleType vehicleTypeOf = storage.vehicleTypeOf("xyz_5", timeOfPassage_1);
        assertEquals(vehicleTypeOf, VehicleType.Car);
    }

    @Test
    void whenVehicleHasDifferentTypesTheSameDay_MultipleVehicleTypesExceptionIsThrown() {
        ZonedDateTime timeOfPassage_1 = ZonedDateTime.now();
        ZonedDateTime timeOfPassage_2 = ZonedDateTime.now().plus(1, ChronoUnit.MINUTES);
        storage.recordPassage("xyz_6", VehicleType.Car, timeOfPassage_1);
        storage.recordPassage("xyz_6", VehicleType.Emergency, timeOfPassage_2);
        assertThrows(MultipleVehicleTypesException.class, () -> {
            storage.vehicleTypeOf("xyz_6", timeOfPassage_1);
        });
    }

}