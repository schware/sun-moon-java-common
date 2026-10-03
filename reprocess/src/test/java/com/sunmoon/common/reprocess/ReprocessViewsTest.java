package com.sunmoon.common.reprocess;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 세 서비스가 같은 모양으로 답하는지 — BO 재처리 화면이 이 모양을 읽습니다. */
class ReprocessViewsTest {

    private static final Instant AT = Instant.parse("2026-10-03T11:00:00Z");

    private static Reprocessor kind(String kind, String label, List<FailedItem> items) {
        return new Reprocessor() {
            @Override public String kind() { return kind; }
            @Override public String label() { return label; }
            @Override public List<FailedItem> failed(String storeId) { return items; }
            @Override public Outcome retry(String storeId, String id) { return Outcome.done("다시 보냄"); }
        };
    }

    @Test
    @SuppressWarnings("unchecked")
    void listCountsEveryKindEvenEmptyOnes() {
        ReprocessService service = new ReprocessService(List.of(
                kind("APP_SEND", "주문 다시 보내기",
                        List.of(new FailedItem("12", "store-01", "짜장면 외 1건", "OMS 에 닿지 못함", 1, AT))),
                kind("ORDER_EVENT", "주문 이벤트 다시 발행", List.of())));

        Map<String, Object> view = ReprocessViews.list(service, "store-01");

        assertEquals(List.of("storeId", "total", "kinds"), List.copyOf(view.keySet()));
        assertEquals("store-01", view.get("storeId"));
        assertEquals(1, view.get("total"));
        List<Map<String, Object>> kinds = (List<Map<String, Object>>) view.get("kinds");
        assertEquals(2, kinds.size(), "실패가 없는 종류도 0 으로 보입니다");
        assertEquals(0, kinds.get(1).get("count"));

        Map<String, Object> row = ((List<Map<String, Object>>) kinds.get(0).get("items")).get(0);
        assertEquals(List.of("id", "storeId", "summary", "error", "attempts", "failedAt"), List.copyOf(row.keySet()));
        assertEquals(AT, row.get("failedAt"));
    }

    @Test
    void listWithoutStoreSaysEmptyStore() {
        assertEquals("", ReprocessViews.list(new ReprocessService(List.of()), null).get("storeId"));
    }

    @Test
    void oneAndAllHaveTheContractKeys() {
        Map<String, Object> one = ReprocessViews.one("APP_SEND", "12", Reprocessor.Outcome.notFound("12"));
        assertEquals(List.of("kind", "id", "accepted", "done", "message"), List.copyOf(one.keySet()));
        assertEquals(false, one.get("accepted"));

        Map<String, Object> all = ReprocessViews.all("APP_SEND", new ReprocessService.Retried(3, 1));
        assertEquals(List.of("kind", "queued", "failed"), List.copyOf(all.keySet()));
        assertTrue((int) all.get("queued") == 3 && (int) all.get("failed") == 1);
    }
}
