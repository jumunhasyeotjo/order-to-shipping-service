package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.cache;

import java.util.UUID;

import com.jumunhasyeotjo.order_to_shipping.shipping.application.service.route.WeightStrategy;

public final class RouteCacheKeys {
  public static final String VERSION = "v1";
  public static final String UNREACHABLE_KEY = "unreachable";
  public static final String NODES_KEY = "nodes";
  public static String redisHashKey(WeightStrategy s){ return "hubnet:"+VERSION+":sp:"+s.name(); }
  public static String field(UUID origin, UUID dest){ return origin+":"+dest; }
  private RouteCacheKeys(){}
}