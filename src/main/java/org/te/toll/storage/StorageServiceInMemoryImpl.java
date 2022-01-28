package org.te.toll.storage;

import org.springframework.stereotype.Component;
import org.te.toll.enums.VehicleType;
import org.te.toll.exceptions.MultipleVehicleTypesException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StorageServiceInMemoryImpl implements StorageService {

    Map<String, List<ZonedDateTime>> passagesByRegistration;
    Map<String, List<TypeDay>> vehicleTypeByRegistration;

    public StorageServiceInMemoryImpl() {
        passagesByRegistration = new HashMap<>();
        vehicleTypeByRegistration = new HashMap<>();
    }

    @Override
    public void recordPassage(String registration, VehicleType type, ZonedDateTime dateTime) {
        vehicleTypeByRegistration.computeIfAbsent(registration, k -> new ArrayList<>()).add(new TypeDay(type, dateTime));
        passagesByRegistration.computeIfAbsent(registration, k -> new ArrayList<>()).add(dateTime);
    }

    @Override
    public List<ZonedDateTime> getPassages(String registration, ZonedDateTime day) {
        List<ZonedDateTime> passages = passagesByRegistration.getOrDefault(registration, List.of());
        return passages.stream()
                .filter(x -> x.truncatedTo(ChronoUnit.DAYS).isEqual(day.truncatedTo(ChronoUnit.DAYS)))
                .collect(Collectors.toList());
    }

    @Override
    public VehicleType vehicleTypeOf(String registration, ZonedDateTime dateTime) throws MultipleVehicleTypesException {
        List<VehicleType> typeDayList = vehicleTypeByRegistration.getOrDefault(registration, List.of()).stream()
                .filter(x -> x.day.isEqual(dateTime.truncatedTo(ChronoUnit.DAYS)))
                .map(x -> x.type)
                .distinct()
                .collect(Collectors.toList());
        if (typeDayList.isEmpty()) {
            return null;
        }
        if (typeDayList.size() == 1) {
            return typeDayList.get(0);
        }
        throw new MultipleVehicleTypesException();
    }

    private static class TypeDay {
        VehicleType type;
        ZonedDateTime day;
        public TypeDay (VehicleType type, ZonedDateTime dateTime) {
            this.type = type;
            day = dateTime.truncatedTo(ChronoUnit.DAYS);
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof  TypeDay)) {
                return false;
            } else {
                TypeDay typeDay = (TypeDay) o;
                return this.type.equals(typeDay.type) && this.day.isEqual(typeDay.day);
            }
        }
    }

}
