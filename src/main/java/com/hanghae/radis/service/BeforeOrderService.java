package com.hanghae.radis.service;

import com.hanghae.radis.model.OrderInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BeforeOrderService {
    // 상품 DB (공유 자원의 동시성 이슈 해결을 위해 ConcurrentMap 사용)
    private final ConcurrentMap<String, Integer> productDatabase = new ConcurrentHashMap<>();
    // 가장 최근 주문 정보를 저장하는 DB
    private final Map<String, OrderInfo> latestOrderDatabase = new HashMap<>();

    public BeforeOrderService() {
        // 초기 상품 데이터
        productDatabase.put("apple", 100);
        productDatabase.put("banana", 50);
        productDatabase.put("orange", 75);
    }

    // 주문 처리 메서드
    public void order(String productName, int amount) {
        try {
            Thread.sleep(1); // 동시성 이슈 유발을 위한 인위적 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        
        //스레드 안전성을 보장하기 위해 특정 키에 대한 연산을 원자적으로 수행하는 compute 사용
        productDatabase.compute(productName, (key, value) -> {
            if (value >= amount) {
                //주문 정보 화면 출력
                System.out.println(Thread.currentThread().getName() + " 주문정보 : \n"
                        + productName + ": " + amount + " 건, 현재 수량: " + value + " , 판매 후 수량: " + (value - amount));
                
                //주문 성공시 최신 주문내역 저장
                latestOrderDatabase.put(productName, new OrderInfo(productName, amount, System.currentTimeMillis()));

                return value - amount; // 상품수가 주문량보다 많을 경우 업데이트
            } else {
                return value; // 아닐 경우 상품수 유지
            }
        });
    }

    // 재고 조회
    public int getStock(String productName) {
        return productDatabase.getOrDefault(productName, 0);
    }

    // 최근 주문 조회
    public OrderInfo getLatestOrder(String productName) {
        return latestOrderDatabase.getOrDefault(productName, null);
    }
}
