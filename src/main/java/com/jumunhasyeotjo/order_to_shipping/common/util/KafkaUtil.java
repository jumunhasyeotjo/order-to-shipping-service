package com.jumunhasyeotjo.order_to_shipping.common.util;
public class KafkaUtil {

	public static String getClassName(String fullTypeName) {
		return fullTypeName.substring(fullTypeName.lastIndexOf('.') + 1);
	}
}
