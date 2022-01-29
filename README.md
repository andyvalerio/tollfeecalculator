# tollfeecalculator

## How to build and run the project?
tollfeecalculator is a Spring Boot application written in Java. 
The project requires a Maven installation and a JDK (min version 11). It can be built running the following command
in the root folder of the project:<br/><br/> 
*mvn clean install*<br/><br/> 

The easiest way to run the application is with this command: <br/><br/> 
*mvn spring-boot:run*<br/><br/>
This will start the application on localhost:8080. <br/>
Access the application's UI from a browser at the following address:<br/>
http://localhost:8080/swagger-ui.html <br/><br/>
From there it is possible to use the REST API /toll/get-fee through a graphic user interface.

## What does /toll/get-fee return?

The REST API takes as input a passage of a vehicle through a Toll station. 
This is identified by the vehicle type, registration number, and the instant in
which the passage takes place. <br/><br/>
The call returns the **cumulative fee** the vehicle has to pay **for the day in which
the passage takes place**. This includes all previous passages during the same day, and the 
latest passage just added.

## How does the application keep track of the passage history?

The application has a very simple module "Storage" that simulates a database/datastore
backend. For the purpose of this exercise the history is stored in an in-memory HashMap.
That means history is lost/reset every time the application is terminated.

## How to extend the current functionality? 

Adding new API operations to TollFeeCalculatorController. Leveraging the logic
implemented in StorageService and TollCalculator, it is very easy to add new 
use cases such as: "calculate the fee of the latest passage", "calculate the cumulative fee
of the current month/year for a specific vehicle".

## How is the application configured?

All configuration is in the file application.yml under the resource folder. 
This YAML file contains configuration regarding: 
- Exempt Vehicle Types 
    - Military, Foreign, etc
- Exempt Months
    - Make entire months without toll fee
- Exempt Week Days
    - Don't collect fee on specific week days 
    - For example: on Saturday and Sunday passages are free of charge
- Exempt Dates
    - Make specific days of the year free of charge
    - For example: 6st of June 2022 (National Day) should be free of charge
- Max Daily Fare
    - Specify the maximum daily fee a vehicle can pay (after which all passages are free of charge)
    - Currently, this set at 60 SEK
- Fares
    - Specify the fares for specific times of the day
    - For example:
        - starting 06:00 the fare is 9 SEK
        - starting 06:30 the fare is 16 SEK
        - starting 18:30 the fare is 0 SEK etc.

## How to test?

Unit tests are in place for the main classes with good coverage. 
**System tests** that start the whole application and call the REST API
are in the class TollFeeCalculatorApplicationTests and can be easily extended to cover more scenarios.

## Extra and Design choices
### Is it necessary to input the passages in the correct chronological order? 
No. The order in which you enter passages for any day/time and vehicle does not matter.
### Can the same vehicle have different types? 
It might be possible for a vehicle to have different types: for example a personal car
might become a diplomatic vehicle; a registration number could theoretically 
change owner and perhaps vehicle type. In this implementation we allow a vehicle (identified by registration 
number) to **change vehicle type, but only in separate days**.
If the same registration number passes a station first as a car and then, *the same day*,
as a tractor, an exception is thrown. The API returns an error message in that case.
### Input format
Registration numbers are allowed to be any string between 2 and 12 characters to support 
not only Swedish standards but worldwide standards as well. 
The passage date/time must follow the pattern: *yyyy-MM-dd HH:mm:ss* <br/>
It is enough to follow the example of date/time that is given in the swagger-ui.<br/>
Accepted vehicle types are defined in the enum VehicleType.