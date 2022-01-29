package org.te.toll.calculator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.te.toll.config.TollConfiguration;
import org.te.toll.enums.VehicleType;
import org.te.toll.exceptions.MultipleDaysPassages;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TollCalculator {
    private final TollConfiguration config;

    public int getTollFee(VehicleType type, List<ZonedDateTime> passages) throws MultipleDaysPassages {
        if (passages.isEmpty()) {
            return 0;
        }
        ZonedDateTime passageDateTime = passages.get(0);
        DayOfWeek passagesDay = passageDateTime.getDayOfWeek();
        Month passagesMonth = passageDateTime.getMonth();
        long passagesSameDay = passages.stream()
                .filter(x -> x.toLocalDate().equals(passageDateTime.toLocalDate())).count();
        if (passagesSameDay != passages.size()) {
            throw new MultipleDaysPassages();
        }

        if (config.getExemptMonths().contains(passagesMonth)
                || config.getExemptWeekDays().contains(passagesDay)
                || config.getExemptDates().contains(passageDateTime.toLocalDate())) {
            return 0;
        }
        if (config.getExemptVehicleTypes().contains(type)) {
            return 0;
        }

        List<List<ZonedDateTime>> clusters = groupPassagesIntoClusters(passages);
        return Math.min(clusters.stream()
                .map(this::getClusterPrice)
                .reduce(0, Integer::sum), config.getMaxDailyFare());
    }

    private int getClusterPrice(List<ZonedDateTime> cluster) {
        return cluster.stream()
                .map(config::getPriceByTime).max(Comparator.naturalOrder()).orElse(0);
    }

    private List<List<ZonedDateTime>> groupPassagesIntoClusters(List<ZonedDateTime> passages) {
        List<List<ZonedDateTime>> clusters = new ArrayList<>();

        Stack<ZonedDateTime> orderedPassages = new Stack<>();
        orderedPassages.addAll(passages.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));

        while(!orderedPassages.isEmpty()) {
            ZonedDateTime firstPassage = orderedPassages.pop();
            List<ZonedDateTime> currentCluster = new ArrayList<>(List.of(firstPassage));
            while(!orderedPassages.isEmpty() && ChronoUnit.MINUTES.between(firstPassage, orderedPassages.peek()) < 60) {
                currentCluster.add(orderedPassages.pop());
            }
            clusters.add(currentCluster);
        }

        return clusters;
    }
}
