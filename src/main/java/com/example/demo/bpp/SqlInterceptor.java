package com.example.demo.bpp;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.logging.DefaultJsonQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.QueryLogEntryCreator;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.example.demo.bpp.MetricService.Status.FAILURE;
import static com.example.demo.bpp.MetricService.Status.SUCCESS;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNullElse;

@Component
@RequiredArgsConstructor
@Slf4j
class SqlInterceptor {
    private final QueryLogEntryCreator entryCreator = new DefaultJsonQueryLogEntryCreator();
    private final MetricService metricService;
    private final ObjectMapper objectMapper;

    @SuppressWarnings({"unchecked", "PMD.AvoidCatchingGenericException"})
    public void interceptQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        final var logEntry = entryCreator.getLogEntry(execInfo, queryInfoList, false, false, true);
        try {
            final Map<String, Object> map = objectMapper.readValue(logEntry, Map.class);
            final List<String> query = (List<String>) map.get("query");
            if (query.isEmpty()) {
                log.warn("No queries are present for execInfo={} and queryInfoList={}", execInfo, queryInfoList);
                return;
            }
            if (query.size() > 1) {
                log.warn(
                    "Batched queries interception is not supported yet for execInfo={} and queryInfoList={}. Query size = {}",
                    execInfo,
                    queryInfoList,
                    query.size()
                );
                return;
            }
            final var sql =
                requireNonNullElse(query.get(0), "")
                    .toUpperCase(ROOT)
                    .trim();
            if (Stream.of("CREATE", "COMMENT", "SHOW", "ALTER", "--", "SET", "DROP", "/*").anyMatch(sql::startsWith)) {
                log.debug(
                    "DDL queries should not be recorded as metrics for execInfo={} and queryInfoList={}",
                    execInfo, queryInfoList
                );
                return;
            }
            final String isolation = (String) map.get("isolation");
            final boolean success = (boolean) map.get("success");
            final var timeMillis = (Integer) map.get("time");
            metricService.incSql(
                success ? SUCCESS : FAILURE, sql, isolation, Duration.ofMillis(timeMillis)
            );
        } catch (Exception e) {
            log.warn("Cannot intercept query execInfo={}, queryInfoList={}", execInfo, queryInfoList, e);
        }
    }
}
