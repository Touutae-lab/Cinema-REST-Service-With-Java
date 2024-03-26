package cinema.model;

import java.util.UUID;

public record BookingResult(UUID token, Seats ticket) {

}
