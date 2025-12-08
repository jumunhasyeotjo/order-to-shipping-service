package com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jumunhasyeotjo.order_to_shipping.common.entity.InboxMessage;
import com.jumunhasyeotjo.order_to_shipping.common.service.InboxService;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub.HubCreatedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub.HubDeletedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.application.event.hub.HubNameUpdatedEvent;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.HubIdCache;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.HubNameCache;
import com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache.ShortestPathCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventHandler {
	private final HubIdCache hubIdCache;
	private final HubNameCache hubNameCache;

	private final ShortestPathCache shortestPathCache;

	@Transactional
	public void hubCreated(HubCreatedEvent event){
		hubIdCache.put(event.name(), event.hubId());
		hubNameCache.put(event.hubId(), event.name());

		shortestPathCache.deleteAll();
	}

	public void hubDeleted(HubDeletedEvent event){
		hubIdCache.delete(event.name());
		hubNameCache.delete(event.hubId());

		shortestPathCache.deleteAll();
	}

	public void hubNameUpdated(HubNameUpdatedEvent event){
		String previousHubName = hubNameCache.get(event.hubId());
		hubIdCache.delete(previousHubName);
		hubIdCache.put(event.name(), event.hubId());
		hubNameCache.put(event.hubId(), event.name());
	}

	public void hubRouteCreated(){
		shortestPathCache.deleteAll();
	}

	public void hubRouteDeleted(){
		shortestPathCache.deleteAll();
	}
}
