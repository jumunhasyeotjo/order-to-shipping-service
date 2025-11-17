## EVENT_GUIDE.md

### 포함 내용

1. 이벤트 네이밍 규칙
    - 도메인명 + 동작(과거형) + Event


2. 리스너 작성 가이드
    - 동기/비동기 처리는 상황에 맞게 적용
    - 기본적으로 전역 예외처리 적용 후 각 비지니스 로직별 예외처리 적용


3. 테스트 작성 가이드
    - 이벤트 발행 검증
    - 비동기 처리 검증
    - 이벤트 플로우 정상 작동 검증

---

# 💬 필수 테스트

## 테스트 가이드


1. 이벤트 발행 테스트

```java

@Test
@DisplayName("주문 생성시 OrderCreatedEvent 는 정상 발행 된다.")
void createOrder_shouldPublishOrderCreatedEvent() {
    // given
    setupDefaultMocks();

    // when
    orderService.createOrder(request);

    // then
    verify(orderEventListener, times(1))
            .handleOrderCreated(any(OrderCreatedEvent.class));
}
```

2. 비동기 처리 테스트

```java

@Test
@DisplayName("이벤트 비동기 처리")
void events_ShouldBeProcessedAsynchronously() {
    // given
    setupDefaultMocks();
    log.info("메인 스레드 : {}", Thread.currentThread().getName());

    // when & then
    orderService.createOrder(request); // 이벤트 발행
    await()
            .atMost(Duration.ofSeconds(3)) // 메인 스레드 3초 대기 설정
            .pollInterval(Duration.ofSeconds(1)) // 1 초마다 확인
            .untilAsserted(() -> {
                verify(paymentEventListener, times(1))
                        .handleOrderCreatedAsync(any(OrderCreatedEvent.class));
            });
}
```

3. 전체 플로우 테스트

```java

@Test
@DisplayName("주문 생성 -> 결제 완료 -> 재고 차감 정상 플로우")
void fullOrderFlow_ShouldWorkCorrectly() throws InterruptedException {
    // given
    CreateOrderCommand request = getCreateOrderCommand();
    setUpMock(request);

    // when
    Order response = orderService.createOrder(request);
    Thread.sleep(3000);

    // then
    // 1. 주문 생성 확인
    Order order = orderRepository.findById(response.getId()).orElseThrow();
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

    // 2. 결제 확인
    List<Payment> payments = paymentRepository.findByOrderId(order.getId());
    assertThat(payments).hasSize(1);
    assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);

    // 3. 재고 차감 확인
    Stock updateStock = stockRepository.findByProductId(productId).orElseThrow();
    assertThat(updateStock.getQuantity()).isEqualTo(5);
}
```