package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.exception.TossRemoteException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class TossFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public Exception decode(String methodKey, Response response) {
        String code = null;
        String msg = null;

        try (InputStream body = response.body() != null ? response.body().asInputStream() : null) {
            if (body != null) {
                JsonNode root = mapper.readTree(body);
                code = root.path("code").asText(null);
                msg = root.path("message").asText(null);

                if (msg != null && msg.contains("message=")) {
                    msg = msg.substring(msg.lastIndexOf("message=") + "message=".length()).trim();
                }
            }
        } catch (IOException ignore) {}

        String combined = "Toss API error (status=" + response.status() + ")";

        return new TossRemoteException(combined, response.status(), code, msg);
    }
}