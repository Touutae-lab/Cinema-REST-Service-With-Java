package cinema.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record BookingResult(Optional<UUID> token, Seats ticket) {
}
