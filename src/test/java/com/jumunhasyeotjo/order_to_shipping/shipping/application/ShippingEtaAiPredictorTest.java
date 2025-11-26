package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.GeminiClient;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiRequest;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiResponse;

@ExtendWith(MockitoExtension.class)
class ShippingEtaAiPredictorTest {

	@Mock
	private GeminiClient geminiClient;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private GeminiResponse geminiResponse;

	@InjectMocks
	private ShippingEtaAiPredictor shippingEtaAiPredictor;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(shippingEtaAiPredictor, "apiKey", "test-api-key");
	}

	@Test
	@DisplayName("ETA 예측 시 프롬프트를 생성하고 GeminiClient 결과 텍스트를 반환한다")
	void predictEta_success() {
		// given
		String productInfo = "노트북 1대";
		String orderRequest = "내일 오전 11시까지 도착 희망";

		ShippingHistory first = org.mockito.Mockito.mock(ShippingHistory.class);
		ShippingHistory middle = org.mockito.Mockito.mock(ShippingHistory.class);
		ShippingHistory last = org.mockito.Mockito.mock(ShippingHistory.class);

		// 발송지 / 경유지 / 도착지 설정
		org.mockito.BDDMockito.given(first.getOrigin()).willReturn("서울 허브");
		org.mockito.BDDMockito.given(last.getDestination()).willReturn("부산 터미널");

		List<ShippingHistory> histories = List.of(first, middle, last);

		String expectedEtaText = "11월 26일 오후 3시";

		given(geminiClient.generateContent(eq("test-api-key"), any(GeminiRequest.class)))
			.willReturn(geminiResponse);

		given(geminiResponse.getCandidates()
			.get(0)
			.getContent()
			.getParts()
			.get(0)
			.getText())
			.willReturn(expectedEtaText);

		// when
		String actual = shippingEtaAiPredictor.predictEta(productInfo, orderRequest, histories, "대전 허브");

		// then
		assertThat(actual).isEqualTo(expectedEtaText);
		verify(geminiClient).generateContent(eq("test-api-key"), any(GeminiRequest.class));
	}

	@Test
	@DisplayName("경유지가 없을 경우 경유지 텍스트는 '-'가 된다")
	void predictEta_withoutWaypoints() {
		// given
		String productInfo = "책 3권";
		String orderRequest = "이번 주 안에만 도착";

		ShippingHistory first = org.mockito.Mockito.mock(ShippingHistory.class);
		ShippingHistory last = org.mockito.Mockito.mock(ShippingHistory.class);

		org.mockito.BDDMockito.given(first.getOrigin()).willReturn("서울 허브");
		org.mockito.BDDMockito.given(last.getDestination()).willReturn("부산 터미널");

		List<ShippingHistory> histories = List.of(first, last);

		String expectedEtaText = "12월 1일 오후 2시";

		given(geminiClient.generateContent(eq("test-api-key"), any(GeminiRequest.class)))
			.willReturn(geminiResponse);

		given(geminiResponse.getCandidates()
			.get(0)
			.getContent()
			.getParts()
			.get(0)
			.getText())
			.willReturn(expectedEtaText);

		// when
		String actual = shippingEtaAiPredictor.predictEta(productInfo, orderRequest, histories, "");

		// then
		assertThat(actual).isEqualTo(expectedEtaText);
		verify(geminiClient).generateContent(eq("test-api-key"), any(GeminiRequest.class));
	}
}