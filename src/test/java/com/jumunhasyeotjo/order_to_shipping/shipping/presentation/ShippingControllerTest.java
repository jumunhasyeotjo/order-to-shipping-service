package com.jumunhasyeotjo.order_to_shipping.shipping.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.ShippingHistoryService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.ShippingService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.command.GetAssignedShippingHistoriesCommand;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.dto.ShippingResult;
import com.jumunhasyeotjo.order_to_shipping.shipping.domain.entity.ShippingHistory;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.ArriveShippingReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.ChangeDriverReq;
import com.jumunhasyeotjo.order_to_shipping.shipping.presentation.dto.request.CreateShippingReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShippingController.class)
class ShippingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ShippingService shippingService;

	@MockitoBean
	private ShippingHistoryService shippingHistoryService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("배송 생성 API")
	void createShipping_ValidRequest_ReturnsCreatedShippingId() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();
		UUID shippingId = UUID.randomUUID();
		UUID supplierCompanyId = UUID.randomUUID();
		UUID receiverCompanyId = UUID.randomUUID();


		CreateShippingReq request = new CreateShippingReq(
			orderId,
			LocalDateTime.now(),
			"테스트 상품 정보",
			"테스트 요청사항",
			supplierCompanyId,
			receiverCompanyId
		);

		given(shippingService.createShipping(any())).willReturn(shippingId);

		// when & then
		mockMvc.perform(post("/api/v1/shippings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(shippingId.toString()));

		verify(shippingService, times(1)).createShipping(any());
	}

	@Test
	@DisplayName("배송 취소 API")
	void cancelShipping_ValidId_ReturnsShippingId() throws Exception {
		// given
		UUID shippingId = UUID.randomUUID();

		// when & then
		mockMvc.perform(patch("/api/v1/shippings/{shippingId}/cancel", shippingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value(shippingId.toString()));

		verify(shippingService, times(1)).cancelShipping(any());
	}

	@Test
	@DisplayName("배송 단건 조회 API")
	void getShipping_ValidId_ReturnsShipping() throws Exception {
		// given
		UUID shippingId = UUID.randomUUID();
		List<ShippingHistory> result = Collections.singletonList(Mockito.mock(ShippingHistory.class));

		given(shippingService.getShipping(any())).willReturn(result);

		// when & then
		mockMvc.perform(get("/api/v1/shippings/{shippingId}", shippingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists());

		verify(shippingService, times(1)).getShipping(any());
	}

	@Test
	@DisplayName("배송 출발 API")
	void departShipping_ValidHistoryId_ReturnsHistoryId() throws Exception {
		// given
		UUID shippingHistoryId = UUID.randomUUID();

		// when & then
		mockMvc.perform(patch("/api/v1/shippings/histories/{shippingHistoryId}/depart", shippingHistoryId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value(shippingHistoryId.toString()));

		verify(shippingHistoryService, times(1)).departShippingHistory(any());
	}

	@Test
	@DisplayName("배송 도착 API")
	void arriveShipping_ValidRequest_ReturnsHistoryId() throws Exception {
		// given
		UUID shippingHistoryId = UUID.randomUUID();

		ArriveShippingReq request = new ArriveShippingReq(
			shippingHistoryId,
			1200 // actualDistance
		);

		// when & then
		mockMvc.perform(patch("/api/v1/shippings/histories/{shippingHistoryId}/arrive", shippingHistoryId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value(shippingHistoryId.toString()));

		verify(shippingHistoryService, times(1)).arriveShippingHistory(any());
	}

	@Test
	@DisplayName("배송자 변경 API")
	void changeDriver_ValidRequest_ReturnsHistoryId() throws Exception {
		// given
		UUID shippingHistoryId = UUID.randomUUID();

		ChangeDriverReq request = new ChangeDriverReq(
			shippingHistoryId,
			10L // newDriverId
		);

		// when & then
		mockMvc.perform(patch("/api/v1/shippings/histories/{shippingHistoryId}/driver", shippingHistoryId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value(shippingHistoryId.toString()));

		verify(shippingHistoryService, times(1)).changeDriver(any());
	}

	@Test
	@DisplayName("담당 배송 내역 조회 API (배송자)")
	void getAssignedShippingHistories_ValidRequest_ReturnsPage() throws Exception {
		// given
		ShippingHistory history1 = org.mockito.Mockito.mock(ShippingHistory.class);
		ShippingHistory history2 = org.mockito.Mockito.mock(ShippingHistory.class);
		ShippingHistory history3 = org.mockito.Mockito.mock(ShippingHistory.class);

		Page<ShippingHistory> page = new PageImpl<>(
			List.of(history1, history2, history3),
			PageRequest.of(0, 10),
			3
		);

		given(shippingHistoryService.getAssignedShippingHistories(
			any(GetAssignedShippingHistoriesCommand.class),
			any(Pageable.class)
		)).willReturn(page);

		// when & then
		mockMvc.perform(get("/api/v1/shippings/assigned-histories")
				.param("page", "0")
				.param("size", "10")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content.length()").value(3));

		verify(shippingHistoryService, times(1))
			.getAssignedShippingHistories(any(GetAssignedShippingHistoriesCommand.class), any(Pageable.class));
	}
}