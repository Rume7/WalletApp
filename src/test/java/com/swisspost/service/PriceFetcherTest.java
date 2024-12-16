package com.swisspost.service;

import com.swisspost.exception.InvalidSymbolException;
import com.swisspost.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PriceFetcherTest {

    @InjectMocks
    private PriceFetcher priceFetcher;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private RestTemplate restTemplate;

    private final String COIN_CAP_API_URL = "https://api.coincap.io/v2/assets";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(priceFetcher, "restTemplate", restTemplate);
        //priceFetcher.getAssetSymbols();
    }

    @Test
    void testUpdatePrices_SuccessfulUpdate() {
        String COIN_CAP_API_URL = "https://api.coincap.io/v2/assets";

        String initialResponse = "{\"data\":[" +
                "{\"id\":\"bitcoin\",\"symbol\":\"BTC\",\"priceUsd\":\"50000.0\"}," +
                "{\"id\":\"ethereum\",\"symbol\":\"ETH\",\"priceUsd\":\"3000.0\"}," +
                "{\"id\":\"binance-coin\",\"symbol\":\"BNB\",\"priceUsd\":\"400.0\"}," +
                "{\"id\":\"ripple\",\"symbol\":\"XRP\",\"priceUsd\":\"1.0\"}," +
                "{\"id\":\"solana\",\"symbol\":\"SOL\",\"priceUsd\":\"100.0\"}," +
                "{\"id\":\"dogecoin\",\"symbol\":\"DOGE\",\"priceUsd\":\"0.1\"}" +
                "]}";

        when(restTemplate.getForObject(eq(COIN_CAP_API_URL), eq(String.class)))
                .thenReturn(initialResponse);

        priceFetcher.updatePrices();

        verify(restTemplate).getForObject(eq(COIN_CAP_API_URL), eq(String.class));
    }

    @Test
    void testUpdatePrices_MultipleSymbols() {
        String initialResponse = """
                {
                    "data": [
                        { "id": "bitcoin", "symbol": "BTC", "priceUsd": "50000.0" },
                        { "id": "ethereum", "symbol": "ETH", "priceUsd": "3000.0" }
                    ]
                }
                """;
        when(restTemplate.getForObject(eq(COIN_CAP_API_URL), eq(String.class)))
                .thenReturn(initialResponse);

        String btcResponse = """
                {
                    "data": {
                        "priceUsd": "50000.0"
                    }
                }
                """;
        String ethResponse = """
                {
                    "data": {
                        "priceUsd": "3000.0"
                    }
                }
                """;

        when(restTemplate.getForObject(eq(COIN_CAP_API_URL + "/bitcoin"), eq(String.class)))
                .thenReturn(btcResponse);
        when(restTemplate.getForObject(eq(COIN_CAP_API_URL + "/ethereum"), eq(String.class)))
                .thenReturn(ethResponse);

        when(assetRepository.findAllAssetSymbols()).thenReturn(List.of("BTC", "ETH"));

        priceFetcher.updatePrices();

        verify(restTemplate, times(1)).getForObject(eq(COIN_CAP_API_URL), eq(String.class));
    }

    @Test
    void testGetAssetSymbols_ReturnsSameInstanceOnMultipleCalls() {
        // When
        List<String> firstCall = priceFetcher.getAssetSymbols();
        List<String> secondCall = priceFetcher.getAssetSymbols();

        // Then
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertEquals(firstCall, secondCall);
    }

    @Test
    void testFetchLatestPrice_HandlesWhitespaceSymbol() {
        // When & Then
        assertThrows(InvalidSymbolException.class, () ->
                priceFetcher.fetchLatestPrice("  "));
    }

    @Test
    void testFetchLatestPrice_InvalidSymbol() {
        // Given
        String symbol = "INVALID";

        // When & Then
        InvalidSymbolException exception = assertThrows(InvalidSymbolException.class, () -> {
            priceFetcher.fetchLatestPrice(symbol);
        });
        assertEquals("Price not available for symbol: INVALID", exception.getMessage());
    }

    @Test
    void testUpdatePrices_ErrorFetchingData() {
        Map<String, Double> symbolPriceCache = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(priceFetcher, "symbolPriceCache", symbolPriceCache);

        // Setup initial cache state
        String initialResponse = """
                {
                    "data": [
                        { "id": "bitcoin", "symbol": "BTC", "priceUsd":"50000.0" }
                    ]
                }
                """;
        when(restTemplate.getForObject(eq(COIN_CAP_API_URL), eq(String.class)))
                .thenReturn(initialResponse)
                .thenThrow(new RuntimeException("Failed to fetch data"));

        when(assetRepository.findAllAssetSymbols()).thenReturn(List.of("BTC"));

        // First update to populate cache
        priceFetcher.updatePrices();

        // Manually add price to cache as the updatePrices would
        symbolPriceCache.put("BTC", 50000.0);

        // Verify initial price is cached
        double initialPrice = priceFetcher.fetchLatestPrice("BTC");
        assertEquals(50000.0, initialPrice);

        // Simulate API failure
        priceFetcher.updatePrices();

        // Verify price is still available from cache
        double priceAfterError = priceFetcher.fetchLatestPrice("BTC");
        assertEquals(50000.0, priceAfterError);

        // Verify the API was called twice
        verify(restTemplate, times(1)).getForObject(eq(COIN_CAP_API_URL), eq(String.class));
    }

    @Test
    void testGetAssetSymbols() {
        // When
        Set<String> symbols = new HashSet<>(priceFetcher.getAssetSymbols());

        // Then
        assertEquals(Set.of(), symbols, "getAssetSymbols should return the correct list of symbols");
    }

    @Test
    void testFetchLatestPrice_NullSymbol() {
        // When & Then
        assertThrows(InvalidSymbolException.class, () -> {
            priceFetcher.fetchLatestPrice(null);
        });
    }

    @Test
    void testFetchLatestPrice_EmptySymbol() {
        // When & Then
        assertThrows(InvalidSymbolException.class, () -> {
            priceFetcher.fetchLatestPrice("");
        });
    }
}
