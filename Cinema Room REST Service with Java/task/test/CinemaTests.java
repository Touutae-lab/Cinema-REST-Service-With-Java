import com.google.gson.Gson;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.expect.json.builder.JsonArrayBuilder;
import org.hyperskill.hstest.testing.expect.json.builder.JsonObjectBuilder;

import java.util.Map;

import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.*;

public class CinemaTests extends SpringTest {
    private static final String ALREADY_PURCHASED_ERROR_MESSAGE = "The ticket has been already purchased!";
    private static final String OUT_OF_BOUNDS_ERROR_MESSAGE = "The number of a row or a column is out of bounds!";

    private static final int totalRows = 9;
    private static final int totalCols = 9;
    private static final Gson gson = new Gson();

    private static void checkStatusCode(HttpResponse resp, int status) {
        if (resp.getStatusCode() != status) {
            throw new WrongAnswer(
                resp.getRequest().getMethod() + " " +
                    resp.getRequest().getLocalUri() +
                    " should respond with status code " + status + ", " +
                    "responded: " + resp.getStatusCode() + "\n\n" +
                    "Response body:\n\n" + resp.getContent()
            );
        }
    }

    CheckResult testEndpoint() {
        HttpResponse response = get("/seats").send();
        checkStatusCode(response, 200);
        return CheckResult.correct();
    }

    CheckResult testEndpointAvailableSeats() {
        HttpResponse response = get("/seats").send();

        JsonArrayBuilder arrayBuilder = isArray(totalRows * totalCols);
        for (int i = 1; i <= totalRows; i++) {
            for (int j = 1; j <= totalCols; j++) {
                int price = i < 5 ? 10 : 8;
                JsonObjectBuilder objectBuilder = isObject()
                        .value("row", i)
                        .value("column", j)
                        .value("price", price);
                arrayBuilder = arrayBuilder.item(objectBuilder);
            }
        }
        expect(response.getContent()).asJson().check(
                isObject()
                        .value("seats", arrayBuilder)
                        .value("columns", 9)
                        .value("rows", 9)
        );

        return CheckResult.correct();
    }

    CheckResult testPurchaseTicket() {
        HttpResponse response = post(
            "/purchase",
            gson.toJson(Map.of(
                "row", "1",
                "column", "1"
            ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson()
            .check(
                isObject()
                    .value("row", 1)
                    .value("column", 1)
                    .value("price", 10)
            );
        return CheckResult.correct();
    }

    CheckResult testErrorMessageThatTicketHasBeenPurchased() {
        HttpResponse response = post(
            "/purchase",
            gson.toJson(Map.of(
                "row", "1",
                "column", "1"
            ))
        ).send();

        checkStatusCode(response, 400);

        expect(response.getContent()).asJson()
            .check(
                isObject()
                    .value("error", ALREADY_PURCHASED_ERROR_MESSAGE)
                    .anyOtherValues()
            );
        return CheckResult.correct();
    }

    CheckResult testErrorMessageThatNumbersOutOfBounds() {
        HttpResponse response = post(
            "/purchase",
            gson.toJson(Map.of(
                "row", "10",
                "column", "1"
            ))
        ).send();

        checkStatusCode(response, 400);

        expect(response.getContent()).asJson()
            .check(
                isObject()
                    .value("error", OUT_OF_BOUNDS_ERROR_MESSAGE)
                    .anyOtherValues()
            );

        response = post(
            "/purchase",
            gson.toJson(Map.of(
                "row", "1",
                "column", "10"
            ))
        ).send();

        checkStatusCode(response, 400);

        expect(response.getContent()).asJson()
            .check(
                isObject()
                    .value("error", OUT_OF_BOUNDS_ERROR_MESSAGE)
                    .anyOtherValues()
            );

        response = post(
            "/purchase",
            gson.toJson(Map.of(
                "row", "-1",
                "column", "-1"
            ))
        ).send();

        checkStatusCode(response, 400);

        expect(response.getContent()).asJson()
            .check(
                isObject()
                    .value("error", OUT_OF_BOUNDS_ERROR_MESSAGE)
                    .anyOtherValues()
            );


        return CheckResult.correct();
    }

    @DynamicTest
    DynamicTesting[] dynamicTests = new DynamicTesting[]{
        this::testEndpoint,
        this::testEndpointAvailableSeats,
        this::testPurchaseTicket,
        this::testErrorMessageThatTicketHasBeenPurchased,
        this::testErrorMessageThatNumbersOutOfBounds
    };
}
