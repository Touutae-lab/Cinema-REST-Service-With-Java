package cinema.services;

import cinema.error.AlreadyBookException;
import cinema.model.BookingResult;
import cinema.model.IncomeReport;
import cinema.model.SeatRows;
import cinema.model.Seats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.UUID.randomUUID;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SeatManagementService {
    private final Map<Seats, Boolean> seatsAvailability = new ConcurrentHashMap<>();

    private final TransactionTokenService transactionTokenService;
    public SeatManagementService(@Autowired TransactionTokenService transactionTokenService) {
        this.transactionTokenService = transactionTokenService;

        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                int value = i  <= 4 ? 10 : 8;
                this.seatsAvailability.put(new Seats(i, j, Optional.of(value)), true);
            }
        }
    }
    public SeatRows getSeats() {
        ArrayList<Seats> seatsList = new ArrayList<>(this.seatsAvailability.keySet());
        seatsList.sort(Comparator.comparingInt(Seats::row).thenComparingInt(Seats::column));
        return new SeatRows(9, 9, seatsList);
    }
  public synchronized BookingResult bookSeat(Seats seats) throws IndexOutOfBoundsException, AlreadyBookException {
        Boolean available = this.seatsAvailability.get(seats);
        if (available == null) {
            throw new IndexOutOfBoundsException("The number of a row or a column is out of bounds!");
        } else if (!available) {
            // we can use this pattern instead
            // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The ticket has been already purchased!");
            throw new AlreadyBookException("The ticket has been already purchased!");
        } else {
            for (Seats original : this.seatsAvailability.keySet()) {
                if (original.equals(seats)) {
                    this.seatsAvailability.put(original, false);
                    UUID geneatedUUID = randomUUID();
                    this.transactionTokenService.createSession(geneatedUUID, original);
                    return new BookingResult(Optional.of(geneatedUUID), original);
                }
            }
        }
        throw new IllegalStateException("The booking system is in an inconsistent state.");
    }

    public synchronized IncomeReport incomeReportCalulate() {
        int income = 0, purchased = 0, available = 0;
        for (Seats original : this.seatsAvailability.keySet()) {
            if (this.seatsAvailability.get(original)) {
                available  += 1;
            } else {
                income += original.price().orElse(0);
                purchased += 1;
            }
        }
        return new IncomeReport(income, available, purchased);
    }

    public synchronized void returnSeat(Seats seats) {
        Boolean available = this.seatsAvailability.get(seats);
        if (!available) {
            for (Seats original : this.seatsAvailability.keySet()) {
                if (original.equals(seats)) {
                    this.seatsAvailability.put(original, true);
                }
            }
        }
    }
}