package com.jumunhasyeotjo.order_to_shipping.common.exception;

public class TossRemoteException extends RuntimeException {
        private final int status;
        private final String code;
        private final String tossMessage;

        public TossRemoteException(String message, int status, String code, String tossMessage) {
            super(message);
            this.status = status;
            this.code = code;
            this.tossMessage = tossMessage;
        }

        public int getStatus() { return status; }
        public String getCode() { return code; }
        public String getTossMessage() { return tossMessage; }
    }