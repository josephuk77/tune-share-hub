package com.tunesharehub.spotify;

import com.tunesharehub.exception.SpotifyApiException;
import java.net.URI;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SpotifyClient {

    private static final Logger log = LoggerFactory.getLogger(SpotifyClient.class);

    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    private static final String TRACK_SEARCH_TYPE = "track";
    private static final String DEFAULT_MARKET = "KR";
    private static final long TOKEN_EXPIRATION_BUFFER_SECONDS = 60;

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUrl;
    private final String apiBaseUrl;

    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    public SpotifyClient(
            RestClient.Builder restClientBuilder,
            @Value("${spotify.client-id}") String clientId,
            @Value("${spotify.client-secret}") String clientSecret,
            @Value("${spotify.token-url}") String tokenUrl,
            @Value("${spotify.api-base-url}") String apiBaseUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUrl = tokenUrl;
        this.apiBaseUrl = apiBaseUrl;
    }

    public SpotifySearchResponse searchTracks(String query, int limit, int offset) {
        URI uri = UriComponentsBuilder
                .fromUriString(apiBaseUrl)
                .path("/search")
                .queryParam("q", query)
                .queryParam("type", TRACK_SEARCH_TYPE)
                .queryParam("market", DEFAULT_MARKET)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .build()
                .toUri();

        try {
            return restClient.get()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(getAccessToken()))
                    .retrieve()
                    .body(SpotifySearchResponse.class);
        } catch (RestClientResponseException | ResourceAccessException exception) {
            log.warn("Spotify track search request failed: {}", exception.getMessage());
            throw new SpotifyApiException("Spotify 트랙 검색에 실패했습니다.");
        }
    }

    private String getAccessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedAccessToken;
        }

        synchronized (this) {
            if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiresAt)) {
                return cachedAccessToken;
            }

            validateCredentials();
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", CLIENT_CREDENTIALS_GRANT_TYPE);

            SpotifyTokenResponse response;
            try {
                response = restClient.post()
                        .uri(tokenUrl)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                        .body(body)
                        .retrieve()
                        .body(SpotifyTokenResponse.class);
            } catch (RestClientResponseException | ResourceAccessException exception) {
                log.warn("Spotify access token request failed: {}", exception.getMessage());
                throw new SpotifyApiException("Spotify access token 발급에 실패했습니다.");
            }

            if (response == null || response.accessToken() == null || response.expiresIn() == null) {
                throw new SpotifyApiException("Spotify access token 응답이 올바르지 않습니다.");
            }

            cachedAccessToken = response.accessToken();
            long cacheSeconds = Math.max(1L, response.expiresIn() - TOKEN_EXPIRATION_BUFFER_SECONDS);
            tokenExpiresAt = Instant.now().plusSeconds(cacheSeconds);
            return cachedAccessToken;
        }
    }

    private void validateCredentials() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new SpotifyApiException("Spotify client 설정이 필요합니다.");
        }
    }
}
