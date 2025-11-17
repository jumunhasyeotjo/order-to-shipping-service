package com.jumunhasyeotjo.order_to_shipping.shipping.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShippingHistoryService {

	@Transactional
	public void changeShippingHistoryStatus(ChangeStatusCommand command){

	}


}
