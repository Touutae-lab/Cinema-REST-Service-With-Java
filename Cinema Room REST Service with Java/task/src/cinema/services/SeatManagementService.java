package cinema.services;

import cinema.error.AlreadyBookException;
import cinema.model.BookingTransactions;
import cinema.model.SeatRows;
import cinema.model.Seats;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SeatManagementService {
    private final Map<Seats, Boolean> seatsAvailability = new ConcurrentHashMap<>();
    public SeatManagementService() {
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                this.seatsAvailability.put(new Seats(i, j, Optional.of(10)), true);
            }
        }
    }
    public SeatRows getSeats() {
        ArrayList<Seats> seatsList = new ArrayList<>(this.seatsAvailability.keySet());
        seatsList.sort(Comparator.comparingInt(Seats::row).thenComparingInt(Seats::column));
        return new SeatRows(9, 9, seatsList);
    }
    public synchronized Seats bookSeat(Seats seats) throws IndexOutOfBoundsException, AlreadyBookException {
        Boolean available = this.seatsAvailability.get(seats);
        if (available == null) {
            throw new IndexOutOfBoundsException("The number of a row or a column is out of bounds!");
        } else if (!available) {
            throw new AlreadyBookException("The ticket has been already purchased!");
        } else {
            l
            this.seatsAvailability.put(seats, false);
            return seats;
        }
    }
}