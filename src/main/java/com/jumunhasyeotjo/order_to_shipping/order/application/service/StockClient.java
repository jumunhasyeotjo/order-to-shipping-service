package com.jumunhasyeotjo.order_to_shipping.order.application.service;

import com.jumunhasyeotjo.order_to_shipping.order.application.dto.StockApiReq;

public interface StockClient {
    boolean decreaseStock(StockApiReq orderProducts, String idempotentKey);
    void restoreStocks(StockApiReq orderProducts, String idempotentKey);
}
