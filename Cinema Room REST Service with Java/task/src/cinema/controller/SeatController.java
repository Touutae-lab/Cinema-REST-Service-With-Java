package cinema.controller;

import cinema.model.BookingResult;
import cinema.model.SeatRows;
import cinema.model.Seats;
import cinema.services.SeatManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SeatController {
    private final SeatManagementService seatManagementService;

    public SeatController(@Autowired SeatManagementService seatManagementService) {
        this.seatManagementService = seatManagementService;
    }

    @GetMapping("/seats")
    public SeatRows getSeatServicesDetails() {
        return this.seatManagementService.getSeats();
    }

    @PostMapping("/purchase")
    public BookingResult purchaseSeat(@RequestBody Seats seats) {
        return seatManagementService.bookSeat(seats);
    }
}
