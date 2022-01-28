package org.te.toll.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.te.toll.enums.VehicleType;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Setter
public class TollConfiguration {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    @Getter
    List<VehicleType> exemptVehicleTypes;
    @Getter
    List<Month> exemptMonths;
    @Getter
    List<DayOfWeek> exemptWeekDays;
    @Getter
    int maxDailyFare;
    private List<String> exemptDatesRaw;
    private List<Fare> fares;

    public List<LocalDate> getExemptDates() {
        return exemptDatesRaw.stream()
                .map(x -> LocalDate.parse(x, formatter))
                .collect(Collectors.toList());
    }

    public int getPriceByTime(ZonedDateTime time) {
        int currentFare = 0;
        for (Fare fare: fares) {
            ZonedDateTime fareStart = ZonedDateTime.of(time.getYear(), time.getMonthValue(), time.getDayOfMonth(),
                    fare.getHour(), fare.getMinute(), 0, 0, ZoneId.systemDefault());
            if (!fareStart.isAfter(time)) {
                currentFare = fare.getPrice();
            } else {
                break;
            }
        }
        return currentFare;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Fare {
        private int hour;
        private int minute;
        private int price;
    }
}
