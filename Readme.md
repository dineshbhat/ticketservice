# Ticket Service
This application provides a ticketing service for Rent-A-Stage company.

## Features
* A Command Line Shell to use the service
	![alt text](https://github.com/dineshbhat/ticketservice/blob/master/images/Shell.png "Command Line Shell")
* Visual representation of seats status
	![alt text](https://github.com/dineshbhat/ticketservice/blob/master/images/show-seats.png "Command Line Shell")
* Find and Hold Seats
	![alt text](https://github.com/dineshbhat/ticketservice/blob/master/images/find-and-hold-seats.png "Command Line Shell")
* Find and Hold Multiple seats
	![alt text](https://github.com/dineshbhat/ticketservice/blob/master/images/multiple-holds.png "Command Line Shell")
* Reserve Seats
	![alt text](https://github.com/dineshbhat/ticketservice/blob/master/images/reserve-seats.png "Command Line Shell")
* Detect Holding more than available seats
	![alt text](https://github.com/dineshbhat/ticketservice/blob/master/images/hold-more-than-available.png "Command Line Shell")
* Fail when reserving with incorrect parameters
	![alt text](https://github.com/dineshbhat/ticketservice/blob/master/images/reserve-with-incorrect-email.png "Command Line Shell")

## Build and execute the unit tests
* JDK 1.8 required.
* run `./mvnw clean package`

## Usage
* run `java -jar target/ticket-service-0.5-SNAPSHOT.jar`
* This opens up a shell to execute the commands as described above

## Assumptions
* Uses a simple "best seat available" logic, allocating seats left to right, starting from the row closest to the stage
* When a hold expires, the seat allocations are compacted and all reservations are updated to remove fragmenation

## Design Decisions
* Chose to implement a shell to make it easier for the reviewer to view the implemented features
* Chose to not have too many packages due to a limited number of files. Can refactor as the complexity increases
* Started with a prototype to prove out the solution and then improved and hardenned it with refactoring, redesign and unit tests

## Limitations
* The unit test coverage is about 75%. I under-estimated the time required
* Could not get the setup expireHolds as a scheduled job
* expireHolds and compact Seat Layout logic is not unit tested
* Could not get to externalize configuration for a few key parameters



