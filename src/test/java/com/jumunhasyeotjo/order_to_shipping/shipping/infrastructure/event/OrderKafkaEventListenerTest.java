package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jumunhasyeotjo.order_to_shipping.common.inbox.InboxService;
import com.jumunhasyeotjo.order_to_shipping.common.util.JsonUtil;
import com.jumunhasyeotjo.order_to_shipping.order.domain.vo.RollbackStatus;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderCanceledEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderEventHandler;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event.type.OrderEvent;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumunhasyeotjo.order_to_shipping.common.inbox.InboxService;
import com.jumunhasyeotjo.order_to_shipping.common.util.JsonUtil;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderCanceledEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderEventHandler;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.order.OrderRolledBackEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.event.type.OrderEvent;

	@ExtendWith(MockitoExtension.class)
	class OrderKafkaEventListenerTest {

		@Mock
		private OrderEventHandler orderEventHandler;

		@Mock
		private ObjectMapper objectMapper;

		@Mock
		private InboxService inboxService;

		@Mock
		private JsonUtil jsonUtil;

		@InjectMocks
		private OrderKafkaEventListener listener;

		@Test
		@DisplayName("CREATED 이벤트가 오면 OrderCreatedEvent로 역직렬화하고 인박스 + handler가 호출된다")
		void dispatch_createdEvent_inboxAndHandlerCalled() throws Exception {
			// given
			String payload = "{\"orderId\":\"dummy\"}";
			String simpleClassName = "OrderCreatedEvent";

			UUID orderId = UUID.randomUUID();
			OrderCreatedEvent event = new OrderCreatedEvent(orderId,
				LocalDateTime.now(),
				"test-product",
				"test-message",
				List.of(UUID.randomUUID()),
				UUID.randomUUID(),
				LocalDateTime.now());

			String json = "{\"some\":\"json\"}";

			org.mockito.Mockito.when(objectMapper.readValue(payload, OrderCreatedEvent.class))
				.thenReturn(event);

			org.mockito.Mockito.when(jsonUtil.toJson(event))
				.thenReturn(json);

			ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

			doAnswer(invocation -> {
				Runnable handler = invocation.getArgument(3, Runnable.class);
				handler.run();
				return null;
			}).when(inboxService)
				.process(
					eq(orderId.toString()),
					eq(OrderEvent.CREATED.getEventName()),
					eq(json),
					any(Runnable.class)
				);

			// when
			listener.dispatch(payload, simpleClassName);

			// then
			verify(inboxService, times(1))
				.process(
					eq(orderId.toString()),
					eq(OrderEvent.CREATED.getEventName()),
					eq(json),
					any(Runnable.class)
				);

			// handler 가 실제로 호출되었는지
			verify(orderEventHandler, times(1))
				.orderCreated(event);
		}

		@Test
		@DisplayName("CANCELED 이벤트가 오면 OrderCanceledEvent로 역직렬화하고 인박스 + handler가 호출된다")
		void dispatch_canceledEvent_inboxAndHandlerCalled() throws Exception {
			// given
			String payload = "{\"orderId\":\"dummy\"}";
			String simpleClassName = "OrderCanceledEvent";

			UUID orderId = UUID.randomUUID();
			OrderCanceledEvent event = new OrderCanceledEvent(orderId, List.of(UUID.randomUUID()), LocalDateTime.now());
			String json = "{\"some\":\"json\"}";

			org.mockito.Mockito.when(objectMapper.readValue(payload, OrderCanceledEvent.class))
				.thenReturn(event);

			org.mockito.Mockito.when(jsonUtil.toJson(event))
				.thenReturn(json);

			doAnswer(invocation -> {
				Runnable handler = invocation.getArgument(3, Runnable.class);
				handler.run();
				return null;
			}).when(inboxService)
				.process(
					eq(orderId.toString()),
					eq(OrderEvent.CANCELED.getEventName()),
					eq(json),
					any(Runnable.class)
				);

			// when
			listener.dispatch(payload, simpleClassName);

			// then
			verify(inboxService, times(1))
				.process(
					eq(orderId.toString()),
					eq(OrderEvent.CANCELED.getEventName()),
					eq(json),
					any(Runnable.class)
				);

			verify(orderEventHandler, times(1))
				.orderCanceled(event);
		}

		@Test
		@DisplayName("ROLLED_BACK 이벤트가 오면 OrderRolledBackEvent로 역직렬화하고 인박스 + handler가 호출된다")
		void dispatch_rolledBackEvent_inboxAndHandlerCalled() throws Exception {
			// given
			String payload = "{\"orderId\":\"dummy\"}";
			String simpleClassName = "OrderRolledBackEvent";

			UUID orderId = UUID.randomUUID();
			OrderRolledBackEvent event = new OrderRolledBackEvent(orderId, RollbackStatus.PAYED_ORDER, LocalDateTime.now());
			String json = "{\"some\":\"json\"}";

			org.mockito.Mockito.when(objectMapper.readValue(payload, OrderRolledBackEvent.class))
				.thenReturn(event);

			org.mockito.Mockito.when(jsonUtil.toJson(event))
				.thenReturn(json);

			doAnswer(invocation -> {
				Runnable handler = invocation.getArgument(3, Runnable.class);
				handler.run();
				return null;
			}).when(inboxService)
				.process(
					eq(orderId.toString()),
					eq(OrderEvent.ROLLED_BACK.getEventName()),
					eq(json),
					any(Runnable.class)
				);

			// when
			listener.dispatch(payload, simpleClassName);

			// then
			verify(inboxService, times(1))
				.process(
					eq(orderId.toString()),
					eq(OrderEvent.ROLLED_BACK.getEventName()),
					eq(json),
					any(Runnable.class)
				);

			verify(orderEventHandler, times(1))
				.orderRolledBack(event);
		}
	}