package com.sunmoon.common.reprocess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 재처리 주소의 응답 모양 — 세 서비스(Device Server · Order · APP-SERVER)가 <b>같은 모양</b>으로 답하게.
 *
 * <p>BO 재처리 화면은 세 곳을 불러 한 화면에 모읍니다. 모양이 서비스마다 다르면 BO 가 셋을 따로 읽어야 하고,
 * 한쪽을 고칠 때 다른 쪽이 조용히 어긋납니다. 그래서 모양을 여기 한 곳에서 만들고, 각 서비스의 HTTP 어댑터
 * (Netty 의 RestEndpoint, Spring 의 @RestController)는 이 {@link Map} 을 JSON 으로 내보내기만 합니다.
 *
 * <table>
 *   <tr><td>{@code GET  /reprocess?storeId=}</td><td>{@link #list} — {@code {storeId, total, kinds:[{kind, label, count, items:[…]}]}}</td></tr>
 *   <tr><td>{@code POST /reprocess/one?kind=&id=&storeId=}</td><td>{@link #one} — {@code {kind, id, accepted, done, message}}</td></tr>
 *   <tr><td>{@code POST /reprocess/all?kind=&storeId=}</td><td>{@link #all} — {@code {kind, queued, failed}}</td></tr>
 * </table>
 *
 * <p>{@code failedAt} 은 {@link java.time.Instant} 그대로 둡니다. Device Server(Jackson + JavaTimeModule,
 * 날짜를 숫자로 쓰지 않음)와 Spring(기본값)이 둘 다 ISO 문자열로 내보냅니다.
 */
public final class ReprocessViews {

    private ReprocessViews() {
    }

    /** 실패 목록 — 종류마다 건수와 건들. 실패가 없는 종류도 0 으로 보입니다(종류가 있다는 것 자체가 정보). */
    public static Map<String, Object> list(ReprocessService reprocess, String storeId) {
        Map<String, List<FailedItem>> failed = reprocess.allFailed(storeId);

        List<Map<String, Object>> kinds = new ArrayList<>();
        int total = 0;
        for (Reprocessor reprocessor : reprocess.kinds()) {
            List<FailedItem> items = failed.getOrDefault(reprocessor.kind(), List.of());
            total += items.size();

            List<Map<String, Object>> rows = new ArrayList<>();
            for (FailedItem item : items) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.id());
                row.put("storeId", item.storeId());
                row.put("summary", item.summary());
                // 사유를 그대로 내보냅니다. 요약하면 다시 돌려도 되는지 판단할 근거가 사라집니다.
                row.put("error", item.error());
                row.put("attempts", item.attempts());
                row.put("failedAt", item.failedAt());
                rows.add(row);
            }

            Map<String, Object> kind = new LinkedHashMap<>();
            kind.put("kind", reprocessor.kind());
            kind.put("label", reprocessor.label());
            kind.put("count", items.size());
            kind.put("items", rows);
            kinds.add(kind);
        }

        Map<String, Object> answer = new LinkedHashMap<>();
        answer.put("storeId", storeId == null ? "" : storeId);
        answer.put("total", total);
        answer.put("kinds", kinds);
        return answer;
    }

    /**
     * 한 건 다시. 못 찾은 것도 오류가 아니라 답입니다 — 목록을 열어 둔 사이에 배치가 처리했을 수 있습니다.
     * 그래서 어댑터는 이것을 늘 200 으로 내보냅니다.
     */
    public static Map<String, Object> one(String kind, String id, Reprocessor.Outcome outcome) {
        Map<String, Object> answer = new LinkedHashMap<>();
        answer.put("kind", kind);
        answer.put("id", id);
        answer.put("accepted", outcome.accepted());
        answer.put("done", outcome.done());
        answer.put("message", outcome.message());
        return answer;
    }

    /** 그 종류 전부 다시. */
    public static Map<String, Object> all(String kind, ReprocessService.Retried retried) {
        Map<String, Object> answer = new LinkedHashMap<>();
        answer.put("kind", kind);
        answer.put("queued", retried.queued());
        answer.put("failed", retried.failed());
        return answer;
    }
}
