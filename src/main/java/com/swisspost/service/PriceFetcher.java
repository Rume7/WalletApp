package com.swisspost.service;

import com.swisspost.exception.InvalidSymbolException;
import com.swisspost.repository.AssetRepository;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.Map;

@Service
@AllArgsConstructor
public class PriceFetcher {

    private final String COIN_CAP_API_URL = "https://api.coincap.io/v2/assets";

    private final RestTemplate restTemplate = new RestTemplate();

    // Cache to store the latest prices of symbols
    private final Map<String, Double> symbolPriceCache = new ConcurrentHashMap<>();

    private final AssetRepository assetRepository;

    private final List<String> assetSymbols;

    private final Map<String, String> symbolToIdMap = new ConcurrentHashMap<>();

    public PriceFetcher(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
        assetSymbols = new ArrayList<>(this.assetRepository.findAllAssetSymbols());
    }

    /**
     * Fetch the latest price for a cryptocurrency from the cache.
     *
     * @param symbol The cryptocurrency symbol (e.g., BTC, ETH).
     * @return The latest price in USD.
     */
    public double fetchLatestPrice(String symbol) {
        if (symbol == null || symbol.isBlank())
            throw new InvalidSymbolException("Price not available for symbol: " + symbol);

        String upperCaseSymbol = symbol.toUpperCase();
        Double price = symbolPriceCache.get(upperCaseSymbol);
        if (price == null) {
            throw new InvalidSymbolException("Price not available for symbol: " + symbol);
        }
        return price;
    }

    /**
     * Scheduled task to update the prices of all symbols every X seconds.
     */
    @Scheduled(fixedRateString = "${scheduler.fixedDelay}")
    public void updatePrices() {
        // Load symbol-to-ID map if it is empty
        if (symbolToIdMap.isEmpty()) {
            loadSymbolToIdMap();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        List<Callable<Void>> tasks = new ArrayList<>();

        // Fetch prices for all symbols in the cache
        symbolToIdMap.forEach((symbol, assetId) -> {
            tasks.add(() -> {
                try {
                    if (getAssetSymbols().contains(symbol)) {
                        double price = fetchPriceFromApi(assetId);
                        symbolPriceCache.put(symbol, price);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating price for " + symbol + ": " + e.getMessage());
                }
                return null;
            });
        });

        try {
            List<Future<Void>> futures = executorService.invokeAll(tasks);

            // Optionally: Wait for all tasks to complete (or handle results)
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    System.err.println("Task execution failed: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Task execution interrupted: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

    public List<String> getAssetSymbols() {
        return this.assetSymbols;
    }

    /**
     * Load the symbol-to-ID map by fetching all assets from the API.
     */
    private void loadSymbolToIdMap() {
        try {
            String response = restTemplate.getForObject(COIN_CAP_API_URL, String.class);

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);

            jsonResponse.getJSONArray("data").forEach(item -> {
                JSONObject asset = (JSONObject) item;
                String symbol = asset.getString("symbol").toUpperCase();
                String id = asset.getString("id");
                symbolToIdMap.put(symbol, id);
            });
            System.out.println("Loaded symbol-to-ID map with " + symbolToIdMap.size() + " entries.");
        } catch (Exception e) {
            throw new RuntimeException("Error loading asset data from API", e);
        }
    }

    /**
     * Fetch the price of a specific cryptocurrency asset from the API.
     *
     * @param assetId The CoinCap asset ID.
     * @return The latest price in USD.
     */
    private double fetchPriceFromApi(String assetId) {
        String url = COIN_CAP_API_URL + "/" + assetId;
        try {
            String response = restTemplate.getForObject(url, String.class);

            JSONObject jsonResponse = new JSONObject(response);
            JSONObject data = jsonResponse.getJSONObject("data");
            return data.getDouble("priceUsd");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching price for asset ID: " + assetId, e);
        }
    }
}
