package cinema.controller;

import cinema.error.WrongPasswordException;
import cinema.model.*;
import cinema.services.SeatManagementService;
import cinema.services.TransactionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/stats")
    public IncomeReport getSeatServicesDetails(@RequestParam(required = false) String password) {
        final String correctPassword = "super_secret";
        if (password == null || !password.equals(correctPassword)) {
            throw new WrongPasswordException("The password is wrong!");
        }
        return this.seatManagementService.incomeReportCalulate();
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
