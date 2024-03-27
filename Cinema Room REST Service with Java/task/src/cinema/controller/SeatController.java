package cinema.controller;

import cinema.model.BookingResult;
import cinema.model.PurchaseTokenRequest;
import cinema.model.SeatRows;
import cinema.model.Seats;
import cinema.services.SeatManagementService;
import cinema.services.TransactionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
public class SeatController {
    private final SeatManagementService seatManagementService;
    private final TransactionTokenService transactionTokenService;

    public SeatController(@Autowired SeatManagementService seatManagementService, @Autowired TransactionTokenService transactionTokenService) {
        this.seatManagementService = seatManagementService;
        this.transactionTokenService = transactionTokenService;
    }

    @GetMapping("/seats")
    public SeatRows getSeatServicesDetails() {
        return this.seatManagementService.getSeats();
    }

    @PostMapping("/purchase")
    public BookingResult purchaseSeat(@RequestBody Seats seats) {
        return seatManagementService.bookSeat(seats);
    }

    @PostMapping("/return")
    public BookingResult purchaseSeat(@RequestBody PurchaseTokenRequest request) {
        Seats result = this.transactionTokenService.getSession(request.token());
        this.seatManagementService.returnSeat(result);
        return new BookingResult(Optional.empty(), result);
    }
}
