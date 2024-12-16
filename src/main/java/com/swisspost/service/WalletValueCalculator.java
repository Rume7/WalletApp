package com.swisspost.service;

import com.swisspost.model.Asset;
import com.swisspost.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletValueCalculator {

    private final AssetRepository assetRepository;

    private final PriceFetcher priceFetcher;

    public WalletValueCalculator(AssetRepository assetRepository, PriceFetcher priceFetcher) {
        this.assetRepository = assetRepository;
        this.priceFetcher = priceFetcher;
    }

    private Map<String, Double> totalValueOfAssetAtPurchasePrice() {
        List<Asset> assetList = assetRepository.findAll();
        return assetList.parallelStream()
                .collect(Collectors.groupingBy(
                        Asset::getSymbol,
                        Collectors.summingDouble(asset -> asset.getQuantity() * asset.getPrice())
                ));
    }

    private Map<String, Double> totalValueOfAssetAtCurrentPrice() {
        List<Asset> assetList = assetRepository.findAll();
        return assetList.parallelStream()
                .collect(Collectors.groupingBy(
                        Asset::getSymbol,
                        Collectors.summingDouble(asset ->
                                asset.getQuantity() * priceFetcher.fetchLatestPrice(asset.getSymbol())
                )));
    }

    public double totalWalletValueOfAssets() {
        List<Asset> assetList = assetRepository.findAll();
        return assetList.parallelStream()
                .mapToDouble(asset -> asset.getQuantity() * asset.getPrice())
                .sum();
    }

    private Map.Entry<String, Double> bestPerformingAssetWithGrowthRate() {
        Map<String, Double> initialValueMap = totalValueOfAssetAtPurchasePrice();
        Map<String, Double> currentValueMap = totalValueOfAssetAtCurrentPrice();

        return currentValueMap.entrySet().stream()
                .filter(entry -> {
                    Double initialValue = initialValueMap.get(entry.getKey());
                    return initialValue != null && initialValue > 0;
                })
                .max(Comparator.comparingDouble(entry -> {
                    String symbol = entry.getKey();
                    double currentValue = entry.getValue();
                    double initialValue = initialValueMap.get(symbol);

                    return ((currentValue - initialValue) * 100) / initialValue;
                }))
                .map(entry -> {
                    String symbol = entry.getKey();
                    double currentValue = entry.getValue();
                    double initialValue = initialValueMap.get(symbol);
                    double growthRate = ((currentValue - initialValue) * 100) / initialValue;

                    BigDecimal roundedGrowthRate = new BigDecimal(growthRate).setScale(2, RoundingMode.HALF_UP);

                    return Map.entry(symbol, roundedGrowthRate.doubleValue());
                })
                .orElse(null); // Return null if no valid asset is found
    }

    public String bestPerformingAsset() {
        return bestPerformingAssetWithGrowthRate().getKey();
    }

    public double bestPerformingAssetValue() {
        return bestPerformingAssetWithGrowthRate().getValue();
    }

    private Map.Entry<String, Double> worstPerformingAssetWithGrowthRate() {
        Map<String, Double> initialValueMap = totalValueOfAssetAtPurchasePrice();
        Map<String, Double> currentValueMap = totalValueOfAssetAtCurrentPrice();

        return currentValueMap.entrySet().stream()
                .filter(entry -> {
                    Double initialValue = initialValueMap.get(entry.getKey());
                    return initialValue != null && initialValue > 0;
                })
                .min(Comparator.comparingDouble(entry -> {
                    String symbol = entry.getKey();
                    double currentValue = entry.getValue();
                    double initialValue = initialValueMap.get(symbol);
                    return ((currentValue - initialValue) * 100) / initialValue;
                }))
                .map(entry -> {
                    String symbol = entry.getKey();
                    double currentValue = entry.getValue();
                    double initialValue = initialValueMap.get(symbol);
                    double growthRate = ((currentValue - initialValue) * 100) / initialValue;

                    BigDecimal roundedGrowthRate = new BigDecimal(growthRate).setScale(2, RoundingMode.HALF_UP);

                    return Map.entry(symbol, roundedGrowthRate.doubleValue());
                })
                .orElse(null);
    }

    public String worstPerformingAsset() {
        return worstPerformingAssetWithGrowthRate().getKey();
    }

    public double worstPerformingAssetValue() {
        return worstPerformingAssetWithGrowthRate().getValue();
    }
}
