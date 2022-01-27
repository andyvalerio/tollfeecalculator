package org.te.toll;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.te.toll.enums.VehicleType;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/toll")
public class TollFeeCalculatorController {
    @ApiOperation(value = "This method returns the current daily toll-fee for the vehicle for the input date and after" +
            "considering the input passage.")
    @RequestMapping(value = "/get-fee/{vehicleType}/{registration}/{date}", method= RequestMethod.GET)
    public ResponseEntity<?> getFee(
            @ApiParam(value = "The type of vehicle", required = true, defaultValue = "Car")
            @PathVariable VehicleType vehicleType,
            @ApiParam(value = "The registration number of the vehicle", required = true, defaultValue = "ULJ985")
            @PathVariable String registration,
            @ApiParam(value = "The station passing time", required = true, defaultValue = "2021-10-31 16:01:00")
            @PathVariable String date) {
        // TODO check if the same vehicle reg number was registered with a different type in a previous passage
        if (registration.length() < 2 || registration.length() > 12) {
            // A Japanese registration number could theoretically have 12 digits (longest in the world) 🙂
            return ResponseEntity.badRequest().body("Wrong registration number format");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
            ZonedDateTime dateTime = ZonedDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Wrong date format");
        }
        return ResponseEntity.ok(0);
    }
}
