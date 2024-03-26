package cinema.services;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionTokenService {
    private final Map<UUID, Instant> tokens = new ConcurrentHashMap<>();


}
