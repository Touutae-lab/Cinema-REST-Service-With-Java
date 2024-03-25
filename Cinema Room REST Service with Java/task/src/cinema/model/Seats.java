package cinema.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

public record Seats(int row, int column, @JsonProperty Optional<Integer> price) {
    // Override equals and hashCode based on row and column
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seats seats = (Seats) o;
        return row == seats.row && column == seats.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
}

