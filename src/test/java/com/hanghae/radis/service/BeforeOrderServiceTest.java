package com.hanghae.radis.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeforeOrderServiceTest {
    private final BeforeOrderService service = new BeforeOrderService();

    @Test
    void testConcurrentOrdersCauseStockMismatch() throws InterruptedException {
        String productName = "apple";
        int initialStock = service.getStock(productName);

        int orderAmount = 8;
        int threadCount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 각 스레드에서 주문을 수행하는 작업 생성
        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    service.order(productName, orderAmount);
                } finally {
                    latch.countDown(); // 작업 완료 후 카운트 감소
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 대기
        latch.await();
        executor.shutdown();

        // 최종 재고 값 확인
        int expectedStock = initialStock % orderAmount;
        int actualStock = service.getStock(productName);

        System.out.println("Expected Stock: " + expectedStock + ", Actual Stock: " + actualStock);
        System.out.println("최근 주문 ["
                + "상품명: " + service.getLatestOrder(productName).getProductName()
                + ", 상품수: " +  service.getLatestOrder(productName).getAmount() + " 건]");

        // 재고 일치 여부 확인
        assertEquals(actualStock, expectedStock);

        // 동시성 이슈로 인해 재고가 맞지 않는 경우를 확인
        //assertNotEquals(expectedStock, actualStock, "재고 불일치 발생!");
    }
}
