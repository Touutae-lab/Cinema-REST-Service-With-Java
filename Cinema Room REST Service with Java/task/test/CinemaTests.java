import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.PresentationError;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.expect.json.builder.JsonArrayBuilder;
import org.hyperskill.hstest.testing.expect.json.builder.JsonObjectBuilder;

import java.util.Map;
import java.util.UUID;

import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.*;

public class CinemaTests extends SpringTest {

    private static final String ALREADY_PURCHASED_ERROR_MESSAGE = "The ticket has been already purchased!";
    private static final String OUT_OF_BOUNDS_ERROR_MESSAGE = "The number of a row or a column is out of bounds!";
    private static final String WRONG_TOKEN_ERROR_MESSAGE = "Wrong token!";
    private static final String WRONG_PASSWORD_MESSAGE = "The password is wrong!";

    private static final int totalRows = 9;
    private static final int totalCols = 9;
    private static final Gson gson = new Gson();

    private static String token = "";

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
                                .value("token", isString())
                                .value("ticket",
                                        isObject()
                                                .value("row", 1)
                                                .value("column", 1)
                                                .value("price", 10)
                                )
                );

        JsonObject object = gson.fromJson(response.getContent(), JsonObject.class);
        token = object.get("token").getAsString();

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

    CheckResult testReturnTicket() {

        HttpResponse response = post(
                "/purchase",
                gson.toJson(Map.of(
                        "row", 2,
                        "column", 5
                ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson()
                .check(
                        isObject()
                                .value("token", isString())
                                .value("ticket",
                                        isObject()
                                                .value("row", 2)
                                                .value("column", 5)
                                                .value("price", 10)
                                )
                );

        JsonObject jsonResponse = gson.fromJson(response.getContent(), JsonObject.class);

        String tokenFromResponse = jsonResponse.get("token").getAsString();
        String wrongToken = UUID.randomUUID().toString();

        try {
            response = post(
                    "/return",
                    gson.toJson(Map.of(
                            "token", wrongToken
                    ))
            ).send();
        } catch (PresentationError e) {
            return CheckResult.wrong("An error occurred while trying to send POST /return with wrong token. " +
                    "In such scenario your program should respond with a 400 status code.");
        }

        checkStatusCode(response, 400);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("error", WRONG_TOKEN_ERROR_MESSAGE)
                        .anyOtherValues()
        );

        response = post(
                "/return",
                gson.toJson(Map.of(
                        "token", tokenFromResponse
                ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("ticket",
                                isObject()
                                        .value("row", 2)
                                        .value("column", 5)
                                        .value("price", 10)
                        )
        );

        return CheckResult.correct();
    }

    CheckResult testStatsEndpoint() {

        HttpResponse response = get("/stats").addParams(Map.of()).send();
        checkStatusCode(response, 401);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("error", WRONG_PASSWORD_MESSAGE)
                        .anyOtherValues()
        );


        return CheckResult.correct();
    }

    CheckResult testStats(int numberOfPurchasedTickets, int currentIncome, int availableSeats) {
        Map<String, String> requestParams = Map.of("password", "super_secret");
        HttpResponse response = get("/stats").addParams(requestParams).send();
        checkStatusCode(response, 200);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("purchased", numberOfPurchasedTickets)
                        .value("income", currentIncome)
                        .value("available", availableSeats)
        );

        return CheckResult.correct();
    }

    CheckResult returnTicket() {
        HttpResponse response = post(
                "/return",
                gson.toJson(Map.of(
                        "token", token
                ))
        ).send();

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("ticket",
                                isObject()
                                        .value("row", 7)
                                        .value("column", 4)
                                        .value("price", 8)
                        )
        );

        return CheckResult.correct();
    }

    CheckResult testPurchaseAnotherTicket() {
        HttpResponse response = post(
                "/purchase",
                gson.toJson(Map.of(
                        "row", "7",
                        "column", "4"
                ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson()
                .check(
                        isObject()
                                .value("token", isString())
                                .value("ticket",
                                        isObject()
                                                .value("row", 7)
                                                .value("column", 4)
                                                .value("price", 8)
                                )
                );

        JsonObject object = gson.fromJson(response.getContent(), JsonObject.class);
        token = object.get("token").getAsString();

        return CheckResult.correct();
    }

    CheckResult testTokenInvalidation() {

        HttpResponse response = post(
                "/purchase",
                gson.toJson(Map.of(
                        "row", 3,
                        "column", 6
                ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("token", isString())
                        .value("ticket",
                                isObject()
                                        .value("row", 3)
                                        .value("column", 6)
                                        .value("price", 10)
                        )
        );

        JsonObject jsonResponse = gson.fromJson(response.getContent(), JsonObject.class);
        String tokenFromResponse = jsonResponse.get("token").getAsString();

        response = post(
                "/return",
                gson.toJson(Map.of(
                        "token", tokenFromResponse
                ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("ticket",
                                isObject()
                                        .value("row", 3)
                                        .value("column", 6)
                                        .value("price", 10)
                        )
        );

        response = post(
                "/return",
                gson.toJson(Map.of(
                        "token", tokenFromResponse
                ))
        ).send();

        checkStatusCode(response, 400);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("error", WRONG_TOKEN_ERROR_MESSAGE)
                        .anyOtherValues()
        );

        return CheckResult.correct();
    }

    CheckResult testReturnedTicketAvailability() {

        HttpResponse response = post(
                "/purchase",
                gson.toJson(Map.of(
                        "row", 3,
                        "column", 6
                ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("token", isString())
                        .value("ticket",
                                isObject()
                                        .value("row", 3)
                                        .value("column", 6)
                                        .value("price", 10)
                        )
        );

        JsonObject jsonResponse = gson.fromJson(response.getContent(), JsonObject.class);
        String tokenFromResponse = jsonResponse.get("token").getAsString();

        response = post(
                "/return",
                gson.toJson(Map.of(
                        "token", tokenFromResponse
                ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("ticket",
                                isObject()
                                        .value("row", 3)
                                        .value("column", 6)
                                        .value("price", 10)
                        )
        );

        response = post(
                "/purchase",
                gson.toJson(Map.of(
                        "row", 3,
                        "column", 6
                ))
        ).send();

        checkStatusCode(response, 200);

        expect(response.getContent()).asJson().check(
                isObject()
                        .value("token", isString())
                        .value("ticket",
                                isObject()
                                        .value("row", 3)
                                        .value("column", 6)
                                        .value("price", 10)
                        )
        );

        return CheckResult.correct();
    }

    @DynamicTest
    DynamicTesting[] dynamicTests = new DynamicTesting[]{
            this::testEndpoint,
            this::testEndpointAvailableSeats,
            this::testPurchaseTicket,
            this::testErrorMessageThatTicketHasBeenPurchased,
            this::testErrorMessageThatNumbersOutOfBounds,
            this::testReturnTicket,
            this::testStatsEndpoint,
            () -> testStats(1, 10, 80),
            this::testPurchaseAnotherTicket,
            () -> testStats(2, 18, 79),
            this::returnTicket,
            () -> testStats(1, 10, 80),
            this::testTokenInvalidation,
            this::testReturnedTicketAvailability
    };
}
