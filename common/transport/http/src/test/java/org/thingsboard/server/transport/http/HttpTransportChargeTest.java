package org.thingsboard.server.transport.http;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class HttpTransportChargeTest {
    @Test
    void testDifferentHttpStatusCodes() {
        HttpStatus[] statusCodes = {
                HttpStatus.OK,
                HttpStatus.BAD_REQUEST,
                HttpStatus.UNAUTHORIZED,
                HttpStatus.NOT_FOUND,
                HttpStatus.INTERNAL_SERVER_ERROR
        };
        for (HttpStatus status : statusCodes) {
            DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
            responseWriter.setResult(new ResponseEntity<>(status));
            assertTrue(responseWriter.hasResult());
            ResponseEntity result = (ResponseEntity) responseWriter.getResult();
            assertEquals(status, result.getStatusCode());
        }
    }
    @Test
    void testPerformanceBaseline() {
        long startTime = System.nanoTime();
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
            responseWriter.setResult(new ResponseEntity<>(HttpStatus.OK));
        }
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        assertTrue(durationMs < 1000,
                "Performance baseline: " + iterations + " requests should complete in < 1s, took: " + durationMs + "ms");
    }
    @Test
    void testConcurrentThreads() throws InterruptedException, ExecutionException {
        int numberOfThreads = 10;
        int requestsPerThread = 50;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            Future<Boolean> future = executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    DeferredResult<ResponseEntity> responseWriter = new DeferredResult<>();
                    responseWriter.setResult(new ResponseEntity<>(HttpStatus.OK));

                    if (!responseWriter.hasResult()) {
                        return false;
                    }
                }
                return true;
            });
            futures.add(future);
        }
        for (Future<Boolean> future : futures) {
            assertTrue(future.get(), "All concurrent requests should succeed");
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }
    @Test
    void testLargePayloadHandling() {
        StringBuilder largePayload = new StringBuilder("{\"data\":[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) largePayload.append(",");
            largePayload.append("{\"id\":").append(i).append(",\"value\":\"test\"}");
        }
        largePayload.append("]}");

        DeferredResult<ResponseEntity<String>> responseWriter = new DeferredResult<>();
        responseWriter.setResult(new ResponseEntity<>(largePayload.toString(), HttpStatus.OK));

        assertTrue(responseWriter.hasResult());
        ResponseEntity<String> result = (ResponseEntity<String>) responseWriter.getResult();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().length() > 1000);
    }
}