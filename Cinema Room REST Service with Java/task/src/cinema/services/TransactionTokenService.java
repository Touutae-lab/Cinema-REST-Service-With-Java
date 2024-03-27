package cinema.services;

import cinema.error.InvalidTokenException;
import cinema.model.Seats;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TransactionTokenService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(TransactionTokenService.class);
    public TransactionTokenService(@Autowired StringRedisTemplate redisTemplate, @Autowired ObjectMapper objectMapper) {
        this.objectMapper  = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void createSession(UUID sessionId, Seats data) {
        try {
            ValueOperations<String, String> ops = this.redisTemplate.opsForValue();
            ops.set(sessionId.toString(), objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public Seats getSession(String sessionId) {
        try {
            ValueOperations<String, String> ops = this.redisTemplate.opsForValue();
            String dataStr = ops.getAndDelete(sessionId);
            if (dataStr != null) {
                return objectMapper.readValue(dataStr, Seats.class);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        throw new InvalidTokenException("Wrong token!");
    }
}
