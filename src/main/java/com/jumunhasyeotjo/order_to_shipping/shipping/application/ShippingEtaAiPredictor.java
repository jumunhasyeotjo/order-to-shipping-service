package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.GeminiClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiRequest;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingEtaAiPredictor {
	private final GeminiClient geminiClient;

	@Value("${gemini.api-key}")
	private String apiKey;

	@CircuitBreaker(name = "shippingEtaPredictor", fallbackMethod = "predictEtaFallback")
	public String predictEta(String productInfo, String orderRequest, List<ShippingHistory> shippingHistories, String waypoints) {
		String prompt = buildEtaPredictionPrompt(productInfo, orderRequest, shippingHistories, waypoints);
		return askGemini(prompt);
	}

	private String buildEtaPredictionPrompt(String productInfo, String orderRequest, List<ShippingHistory> shippingHistories, String waypoints) {

		String shippingInfo = "상품정보 : " + productInfo + "\n" +
			"요청사항 : " + orderRequest + "\n" +
			"발송지 : " + shippingHistories.get(0).getOrigin() + "\n" +
			"경유지 : " + waypoints + "\n" +
			"도착지 : " + shippingHistories.get(shippingHistories.size() - 1).getDestination() + "\n";

		return shippingInfo +
			"다음 배송 정보에 기반하여, 요청하신 요청사항을 맞추기 위한 최종 발송 시한이 언제인지 " +
			"'mm월 dd일 오전/오후 n시' 형식으로만 알려주세요.";
	}

	public String predictEtaFallback(String productInfo, String orderRequest, String waypoints, Throwable t) {
		log.error("[shippingEtaPredictor] Failed to predict ETA via Gemini. productInfo={}, orderRequest={}, waypoints={}",
			productInfo, orderRequest, waypoints, t);

		// TODO
		return "ETA 계산 실패";
	}

	private String askGemini(String prompt) {
		GeminiRequest.Part part = new GeminiRequest.Part(prompt);
		GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
		GeminiRequest request = new GeminiRequest(List.of(content));

		GeminiResponse response = geminiClient.generateContent(apiKey, request);
		return response.getCandidates()
			.get(0)
			.getContent()
			.getParts()
			.get(0)
			.getText();
	}
}
