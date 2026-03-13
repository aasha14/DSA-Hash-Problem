import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// TokenBucket class (non-public)
class TokenBucket {
    private final int maxTokens;
    private final double refillRatePerSecond;
    private AtomicInteger tokens;
    private Instant lastRefill;

    public TokenBucket(int maxTokens, double refillRatePerSecond) {
        this.maxTokens = maxTokens;
        this.refillRatePerSecond = refillRatePerSecond;
        this.tokens = new AtomicInteger(maxTokens);
        this.lastRefill = Instant.now();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    public synchronized int getRemaining() {
        refill();
        return tokens.get();
    }

    private void refill() {
        long millis = Duration.between(lastRefill, Instant.now()).toMillis();
        if (millis > 0) {
            int refillAmount = (int)(millis / 1000.0 * refillRatePerSecond);
            if (refillAmount > 0) {
                tokens.set(Math.min(maxTokens, tokens.get() + refillAmount));
                lastRefill = Instant.now();
            }
        }
    }

    public synchronized long getResetSeconds() {
        return (long)Math.ceil((maxTokens - tokens.get()) / refillRatePerSecond);
    }
}

// Public class matching the filename
public class ApiRateLimiter {
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int maxRequestsPerHour;
    private final double refillRatePerSecond;

    public ApiRateLimiter(int maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
        this.refillRatePerSecond = maxRequestsPerHour / 3600.0;
    }

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(
                clientId, k -> new TokenBucket(maxRequestsPerHour, refillRatePerSecond));
        if (bucket.tryConsume()) {
            return " Allowed (" + bucket.getRemaining() + " requests remaining)";
        } else {
            return " Denied (0 requests remaining, retry after " + bucket.getResetSeconds() + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = buckets.get(clientId);
        int used = bucket == null ? 0 : maxRequestsPerHour - bucket.getRemaining();
        long reset = bucket == null ? 3600 : bucket.getResetSeconds();
        return "{used: " + used + ", limit: " + maxRequestsPerHour + ", reset: " + reset + "s}";
    }

    public static void main(String[] args) throws InterruptedException {
        ApiRateLimiter limiter = new ApiRateLimiter(1000);
        String client = "abc123";

        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(client));
            Thread.sleep(200);
        }

        System.out.println(limiter.getRateLimitStatus(client));
    }
}
