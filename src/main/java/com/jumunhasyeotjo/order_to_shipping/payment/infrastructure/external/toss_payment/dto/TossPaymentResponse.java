package com.jumunhasyeotjo.order_to_shipping.payment.infrastructure.external.toss_payment.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TossPaymentResponse {

    private String mId;
    private String lastTransactionKey;
    private String paymentKey;
    private String orderId;
    private String orderName;

    private Integer taxExemptionAmount;
    private String status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;

    private Boolean useEscrow;
    private Boolean cultureExpense;

    private Card card;                   // 카드 결제인 경우
    private Object virtualAccount;       // 가상계좌인 경우(단순화)
    private Object transfer;             // 계좌이체(단순화)
    private Object mobilePhone;          // 휴대폰결제(단순화)
    private Object giftCertificate;      // 상품권(단순화)
    private Object cashReceipt;          // 현금영수증 단건 (과거)
    private Object cashReceipts;         // 현금영수증 복수 (신규)
    private Object discount;             // 할인정보
    private List<Cancel> cancels;              // 취소내역
    private String secret;

    private String type;
    private EasyPay easyPay;

    private String country;
    private Failure failure;

    private Boolean isPartialCancelable;

    private Receipt receipt;
    private Checkout checkout;

    private String currency;             // "KRW"
    private Integer totalAmount;
    private Integer balanceAmount;
    private Integer suppliedAmount;
    private Integer vat;
    private Integer taxFreeAmount;

    private Object metadata;
    private String method;               // "카드"
    private String version;              // "2022-11-16"

    // ===== Nested Types =====
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Card {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private Integer installmentPlanMonths;
        private Boolean isInterestFree;
        private String interestPayer;
        private String approveNo;
        private Boolean useCardPoint;
        private String cardType;     // "신용"
        private String ownerType;    // "개인"
        private String acquireStatus;// "READY"
        private Integer amount;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EasyPay {
        private String provider;     // "토스페이"
        private Integer amount;
        private Integer discountAmount;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Receipt {
        private String url;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Checkout {
        private String url;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Failure {
        private String code;
        private String message;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Cancel {
        private String transactionKey;
        private String cancelReason;
        private Integer taxExemptionAmount;
        private OffsetDateTime canceledAt;
        private Integer cardDiscountAmount;
        private Integer transferDiscountAmount;
        private Integer easyPayDiscountAmount;
        private String receiptKey;
        private String cancelStatus;
        private String cancelRequestId;
        private Integer cancelAmount;
        private Integer taxFreeAmount;
        private Integer refundableAmount;
    }

}