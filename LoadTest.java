import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {

    private static final String URL = "http://localhost:8090/api/rate-limit/token-bucket/check?clientId=abhi";
    private static final int NUM_REQUESTS = 20;

    public static void main(String[] args) throws InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        CountDownLatch latch = new CountDownLatch(NUM_REQUESTS);
        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);

        for (int i = 0; i < NUM_REQUESTS; i++) {
            final int requestNumber = i;
            new Thread(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(URL))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    boolean allowed = response.body().contains("\"allowed\":true");
                    if (allowed) {
                        allowedCount.incrementAndGet();
                    } else {
                        blockedCount.incrementAndGet();
                    }

                    System.out.printf("Request %2d -> HTTP %d -> %s -> %s%n",
                            requestNumber, response.statusCode(),
                            allowed ? "ALLOWED" : "BLOCKED", response.body());

                } catch (Exception e) {
                    System.out.println("Request " + requestNumber + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        System.out.printf("%nTotal: %d allowed, %d blocked out of %d requests%n",
                allowedCount.get(), blockedCount.get(), NUM_REQUESTS);
    }
}