package com.jumunhasyeotjo.order_to_shipping.shipping.domain.service;

import static com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;

import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.Shipping;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.RouteInfo;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo.ShippingHistoryStatus;

public class ShippingDomainService {
	/**
	 * 배송 이력(구간) 출발 처리
	 */
	public Integer departHistorySegment(ShippingHistory shippingHistory){
		Shipping shipping = shippingHistory.getShipping();
		Integer currentSequence = shippingHistory.getSequence();
		Integer totalSequence = shipping.getTotalRouteCount();

		shippingHistory.markAsDeparted();
		if(currentSequence == 1){
			shipping.dispatchFromOriginHub();
		}else if(currentSequence.equals(totalSequence)){
			shipping.departFromDestinationHub();
		}

		return totalSequence;
	}

	/**
	 * 배송 이력(구간) 도착 처리
	 */
	public Integer arriveHistorySegment(ShippingHistory shippingHistory, Integer actualDistance){
		Shipping shipping = shippingHistory.getShipping();
		Integer currentSequence = shippingHistory.getSequence();
		Integer totalSequence = shipping.getTotalRouteCount();

		shippingHistory.markAsArrived(actualDistance);
		if(currentSequence.equals(totalSequence)){
			shipping.completeDelivery();
		}else if(currentSequence == totalSequence -1){
			shipping.arriveAtDestinationHub();
		}

		return totalSequence;
	}

	/**
	 * 배송 취소
	 */
	public void cancelDelivery(Shipping shipping, List<ShippingHistory> shippingHistoryList){
		boolean anyMismatch = shippingHistoryList.stream()
			.anyMatch(shippingHistory -> !shippingHistory.getShipping().equals(shipping));
		if(anyMismatch){
			throw new BusinessException(HISTORY_SHIPPING_MISMATCH);
		}

		shipping.cancel();
		shippingHistoryList.forEach(ShippingHistory::cancel);
	}
}
