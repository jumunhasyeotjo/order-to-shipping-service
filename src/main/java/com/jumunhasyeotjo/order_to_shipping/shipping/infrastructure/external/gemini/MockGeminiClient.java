package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiRequest;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto.GeminiResponse;

@Profile("perf")
@Component
public class MockGeminiClient implements GeminiClient {

    @Override
    public GeminiResponse generateContent(String apiKey, GeminiRequest request) {

        GeminiResponse.Part part =
            new GeminiResponse.Part("12월 27일 오후 4시");

        GeminiResponse.Content content =
            new GeminiResponse.Content(List.of(part));

        GeminiResponse.Candidate candidate =
            new GeminiResponse.Candidate(content);

        return new GeminiResponse(List.of(candidate));
    }
}