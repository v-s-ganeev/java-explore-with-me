package ru.practicum.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatsServiceClient extends BaseClient {
    private static final String API_PREFIX_HIT = "/hit";
    private static final String API_PREFIX_STATS = "/stats";

    @Autowired
    public StatsServiceClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addEndpointHit(EndpointHitDto endpointHitDto) {
        return post(API_PREFIX_HIT, endpointHitDto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "uris", uris,
                "unique", unique
        );
        return get(API_PREFIX_STATS + "?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }
}
