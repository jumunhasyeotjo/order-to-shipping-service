package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.DelayShippingCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ProductInfoName;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.GeminiClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiRequest;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingDelayAiMessageGenerator {

    private final GeminiClient geminiClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    @CircuitBreaker(name = "shippingDelayMessageGenerator", fallbackMethod = "messageGeneratorFallback")
    public String generateMessage(List<ProductInfoName> products, DelayShippingCommand command) {
        String prompt = generateDelayPrompt(products, command);
        return askGemini(prompt);
    }

    private String generateDelayPrompt(List<ProductInfoName> products, DelayShippingCommand command) {
        // 데이터 포맷팅
        String productListString = formatProductList(products);
        String formattedTime = command.delayTime().format(DateTimeFormatter.ofPattern("M월 d일 a h시 m분"));
        String location = command.location() != null ? command.location() : "배송지 인근";
        String etcInfo = command.etc() != null && !command.etc().isEmpty() ? command.etc() : "없음";
        String reasonDescription = command.reason().getDescription();
        String reasonContext = command.reason().getLlmContext();

        return String.format("""
            당신은 배송 기사님을 대신해 고객에게 정중한 배송 지연 안내 메시지를 작성하는 AI 비서입니다.
            아래 정보를 바탕으로 고객이 안심할 수 있는 문자 메시지를 작성해주세요.

            [배송 정보]
            1. 주문 상품: %s
            2. 현재 위치: %s
            3. 지연 사유: %s
               - 상황 맥락: %s
            4. 도착 예정 시간: %s
            5. 기사님 추가 전달 사항: %s

            [작성 지침]
            - 톤앤매너: 매우 정중하고, 사과하는 태도를 갖추되, 책임을 다하겠다는 의지를 보여줄 것.
            - 상품 특성 반영: 상품 목록을 보고 신선식품이나 파손 주의 물품이 있다면 안심시키는 문구를 포함할 것.
            - 핵심 전달: '지연 사유'와 '도착 예정 시간'은 명확하게 전달할 것.
            - 길이: 공백 포함 200자 이내로 핵심만 작성할 것.
            - 형식: '안녕하세요, 고객님.'으로 시작할 것.
            """,
                productListString,
                location,
                reasonDescription,
                reasonContext,
                formattedTime,
                etcInfo
        );
    }

    private String formatProductList(List<ProductInfoName> products) {
        if (products == null || products.isEmpty()) {
            return "주문하신 상품";
        }
        return products.stream()
                .map(p -> String.format("%s (%d개)", p.productName(), p.quantity()))
                .collect(Collectors.joining(", "));
    }

    private String askGemini(String prompt) {
        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
        GeminiRequest request = new GeminiRequest(List.of(content));

        // 예외(FeignException 등)가 발생하면 바로 Circuit Breaker 감지
        GeminiResponse response = geminiClient.generateContent(apiKey, request);

        if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            return response.getCandidates()
                    .get(0)
                    .getContent()
                    .getParts()
                    .get(0)
                    .getText();
        }

        // 응답이 비어있다면 비즈니스 예외로 간주하여 throw
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String messageGeneratorFallback(List<ProductInfoName> products, DelayShippingCommand command, Throwable t) {
        log.error("배송 지연 메시지 AI 생성 실패 - 사유: {}, 에러: {}", command.reason(), t.getMessage());
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}