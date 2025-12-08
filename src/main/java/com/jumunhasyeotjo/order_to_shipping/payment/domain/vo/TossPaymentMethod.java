package com.jumunhasyeotjo.order_to_shipping.payment.domain.vo;

import java.util.HashMap;
import java.util.Map;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;

public enum TossPaymentMethod {
    VIRTUAL_ACCOUNT("가상계좌"),
    SIMPLE_PAYMENT("간편결제"),
    GAME_GIFT_CARD("게임문화상품권"),
    BANK_TRANSFER("계좌이체"),
    BOOK_GIFT_CARD("도서문화상품권"),
    CULTURE_GIFT_CARD("문화상품권"),
    CARD("카드"),
    MOBILE_PAYMENT("휴대폰");

    private final String label;

    TossPaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // 이름/라벨 모두로 조회 가능하도록 맵 구성
    private static final Map<String, TossPaymentMethod> BY_UPPER_NAME = new HashMap<>();
    private static final Map<String, TossPaymentMethod> BY_LABEL = new HashMap<>();

    static {
        for (TossPaymentMethod m : values()) {
            BY_UPPER_NAME.put(m.name(), m);
            BY_LABEL.put(m.label, m);
        }
        // 흔한 별칭(공백/결제 접미사 등)도 허용
        BY_LABEL.put("간편 결제", SIMPLE_PAYMENT);
        BY_LABEL.put("휴대폰결제", MOBILE_PAYMENT);
        BY_LABEL.put("계좌 이체", BANK_TRANSFER);
        BY_LABEL.put("카드결제", CARD);
    }

    public static TossPaymentMethod from(String method) {
        String trimmed = method.trim();
        TossPaymentMethod byLabel = BY_LABEL.get(trimmed);

        if (byLabel != null) return byLabel;
        TossPaymentMethod byName = BY_UPPER_NAME.get(trimmed.toUpperCase());
        if (byName != null) return byName;
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
