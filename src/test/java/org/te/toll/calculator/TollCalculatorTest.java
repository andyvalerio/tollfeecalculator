package org.te.toll.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.te.toll.config.TollConfiguration;
import org.te.toll.enums.VehicleType;
import org.te.toll.exceptions.MultipleDaysPassages;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TollCalculatorTest {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    TollConfiguration config = new TollConfiguration();

    @BeforeEach
    void setupConfiguration() {
        config.setExemptMonths(List.of(Month.JULY));
        config.setExemptWeekDays(List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        config.setExemptDatesRaw(List.of("2022-01-05", "2022-01-06", "2022-04-14", "2022-04-15", "2022-04-18",
                "2022-05-25", "2022-05-26", "2022-06-06", "2022-06-24", "2022-11-04", "2026-12-26"));
        config.setExemptVehicleTypes(List.of(VehicleType.Motorbike, VehicleType.Tractor, VehicleType.Emergency,
                VehicleType.Diplomat, VehicleType.Foreign, VehicleType.Military));
        config.setFares(List.of(new TollConfiguration.Fare(0, 0, 0),
                new TollConfiguration.Fare(6, 0, 9),
                new TollConfiguration.Fare(6, 30, 16),
                new TollConfiguration.Fare(7, 0, 22),
                new TollConfiguration.Fare(8, 0, 16),
                new TollConfiguration.Fare(8, 30, 9),
                new TollConfiguration.Fare(15, 0, 16),
                new TollConfiguration.Fare(15, 30, 22),
                new TollConfiguration.Fare(17, 0, 16),
                new TollConfiguration.Fare(18, 0, 9),
                new TollConfiguration.Fare(18, 30, 0)
                ));
        config.setMaxDailyFare(60);
    }

    @Test
    void whenPassingOnceFromStationOnWeekDay_CorrectFeeIsCalculated() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(VehicleType.Car, List.of(ZonedDateTime.parse("2022-01-11 10:00:00", formatter)));
        assertEquals(9, tollFee);
    }

    @Test
    void whenPassingFromStationOnNewYear_FeeIsZero() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(VehicleType.Car, List.of(ZonedDateTime.parse("2022-01-01 10:00:00", formatter)));
        assertEquals(0, tollFee);
    }

    @Test
    void whenPassingFromStationInJuly_FeeIsZero() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(VehicleType.Car, List.of(ZonedDateTime.parse("2022-07-01 10:00:00", formatter)));
        assertEquals(0, tollFee);
    }

    @Test
    void whenPassingFromStationOnWeekend_FeeIsZero() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(VehicleType.Car, List.of(ZonedDateTime.parse("2022-01-16 10:00:00", formatter)));
        assertEquals(0, tollFee);
    }

    @Test
    void whenPassingFromStationWithMilitaryVehicle_FeeIsZero() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(VehicleType.Military, List.of(ZonedDateTime.parse("2022-01-26 10:00:00", formatter)));
        assertEquals(0, tollFee);
    }

    @Test
    void whenNotPassingAtAll_FeeIsZero() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(VehicleType.Car, List.of());
        assertEquals(0, tollFee);
    }

    @Test
    void whenTryingToMixMultipleDays_ExceptionIsThrown() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        assertThrows(MultipleDaysPassages.class, () -> calculator.getTollFee(VehicleType.Car,
                List.of(ZonedDateTime.parse("2022-01-26 10:00:00", formatter),
                        ZonedDateTime.parse("2022-01-27 10:00:00", formatter))));

    }

    @Test
    void whenPassingMultipleTimesInTheSameHour_OnlyTheHighestFeeIsConsidered() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(
                VehicleType.Car, List.of(
                        ZonedDateTime.parse("2022-01-12 05:45:00", formatter), // 0 SEK
                        ZonedDateTime.parse("2022-01-12 06:05:00", formatter), // 9 SEK
                        ZonedDateTime.parse("2022-01-12 06:40:00", formatter)  // 16 SEK
                )
        );
        assertEquals(16, tollFee);
    }

    @Test
    void whenTotalPriceIsOverMaximum_MaximumDailyPriceIsReturned() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(
                VehicleType.Car, List.of(
                        ZonedDateTime.parse("2022-01-12 06:35:00", formatter), // 16 SEK
                        ZonedDateTime.parse("2022-01-12 07:36:00", formatter), // 22 SEK
                        ZonedDateTime.parse("2022-01-12 15:31:00", formatter), // 22 SEK
                        ZonedDateTime.parse("2022-01-12 17:10:00", formatter)  // 16 SEK
                )
        );
        assertEquals(60, tollFee);
    }

    @Test
    void whenOrderingThePassagesRandomly_TheResultIsStillCorrect() throws MultipleDaysPassages {
        TollCalculator calculator = new TollCalculator(config);
        int tollFee = calculator.getTollFee(
                VehicleType.Car, List.of(
                        ZonedDateTime.parse("2022-01-12 15:31:00", formatter), // 22 SEK
                        ZonedDateTime.parse("2022-01-12 07:36:00", formatter), // 22 SEK
                        ZonedDateTime.parse("2022-01-12 17:10:00", formatter),  // 16 SEK
                        ZonedDateTime.parse("2022-01-12 06:35:00", formatter) // 16 SEK
                )
        );
        assertEquals(60, tollFee);
    }

}