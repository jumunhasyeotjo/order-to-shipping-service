package com.jumunhasyeotjo.order_to_shipping.shipping.domain.vo;

import lombok.Getter;

@Getter
public enum DelayReason {
    // 1. 도로 및 교통 상황
    TRAFFIC_ACCIDENT("교통사고", "예기치 못한 교통사고가 발생하여 도로가 정체되고 있는 상황"),
    ROAD_CONSTRUCTION("도로 공사", "도로 공사로 인해 우회 도로를 이용하고 있어 시간이 더 소요되는 상황"),
    HEAVY_TRAFFIC("교통 체증", "출퇴근 시간 또는 휴가철 영향으로 차량 이동이 평소보다 매우 더딘 상황"),
    EVENT_BLOCKAGE("행사/집회 통제", "지역 행사나 집회로 인해 도로가 통제되어 진입이 불가능한 상황"),

    // 2. 배송 업무 및 물량 이슈
    HIGH_VOLUME("물량 폭주", "배송 물량이 평소보다 많아 배송 스케줄이 전체적으로 밀리고 있는 상황"),
    PREVIOUS_STOP_DELAY("이전 배송 지연", "앞선 배송지에서 예상치 못한 문제가 생겨 해결하느라 시간이 지체된 상황"),
    HUB_DELAY("터미널 출차 지연", "물류 센터에서 물건을 싣고 나오는 과정 자체가 늦어진 상황"),
    SORTING_ERROR("오분류 재작업", "상품 분류 과정의 오류를 바로잡느라 시간이 소요된 상황"),

    // 3. 차량 및 장비 문제
    VEHICLE_ISSUE("차량 점검/고장", "배송 차량에 이상이 감지되어 안전을 위해 긴급 점검 중인 상황"),

    // 4. 날씨 및 환경
    BAD_WEATHER_RAIN("폭우/폭설", "비나 눈이 많이 와서 시야 확보가 어렵고 도로가 미끄러워 서행 중인 상황"),
    TYPHOON("태풍/강풍", "강한 바람으로 인해 낙하물 위험이 있어 안전하게 천천히 이동 중인 상황"),

    // 5. 기타
    OTHER("기타 사유", "구체적으로 명시되지 않은 불가피한 사유로 지연되는 상황");

    private final String description;
    private final String llmContext;

    DelayReason(String description, String llmContext) {
        this.description = description;
        this.llmContext = llmContext;
    }
}
