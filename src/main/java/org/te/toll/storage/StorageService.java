package org.te.toll.storage;

import org.springframework.stereotype.Component;
import org.te.toll.enums.VehicleType;
import org.te.toll.exceptions.MultipleVehicleTypesException;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public interface StorageService {
    void recordPassage(String registration, VehicleType type, ZonedDateTime dateTime);
    List<ZonedDateTime> getPassages(String registration, ZonedDateTime day);

    /**
     * @param registration Registration number of the vehicle
     * @param dateTime
     * @return The VehicleType of the vehicle with the same registration number of it already passed a paying station
     * during the same day.
     * @throws MultipleVehicleTypesException
     */
    VehicleType vehicleTypeOf(String registration, ZonedDateTime dateTime) throws MultipleVehicleTypesException;
}
