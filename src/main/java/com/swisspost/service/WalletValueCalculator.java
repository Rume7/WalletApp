package com.swisspost.service;

import com.swisspost.model.Asset;
import com.swisspost.model.Wallet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WalletValueCalculator {

    private final PriceFetcher priceFetcher;

    public WalletValueCalculator(PriceFetcher priceFetcher) {
        this.priceFetcher = priceFetcher;
    }

    private Map<String, Double> totalValueOfAssetAtPurchasePrice(Wallet wallet) {
        Set<Asset> walletCryptoAssets = wallet.getCryptoAssets();
        return walletCryptoAssets.parallelStream()
                .collect(Collectors.groupingBy(
                        Asset::getSymbol,
                        Collectors.summingDouble(asset -> asset.getQuantity() * asset.getPrice())
                ));
    }

    private Map<String, Double> totalValueOfAssetAtCurrentPrice(Wallet wallet) {
        Set<Asset> walletCryptoAssets = wallet.getCryptoAssets();
        return walletCryptoAssets.parallelStream()
                .collect(Collectors.groupingBy(
                        Asset::getSymbol,
                        Collectors.summingDouble(asset ->
                                asset.getQuantity() * priceFetcher.fetchLatestPrice(asset.getSymbol())
                )));
    }

    public double totalWalletValueOfAssets(Wallet wallet) {
        Map<String, Double> assetsAtCurrentPrice = totalValueOfAssetAtCurrentPrice(wallet);
        return assetsAtCurrentPrice.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private Map.Entry<String, Double> bestPerformingAssetWithGrowthRate(Wallet wallet) {
        Map<String, Double> initialValueMap = totalValueOfAssetAtPurchasePrice(wallet);
        Map<String, Double> currentValueMap = totalValueOfAssetAtCurrentPrice(wallet);

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

    public String bestPerformingAsset(Wallet wallet) {
        return bestPerformingAssetWithGrowthRate(wallet).getKey();
    }

    public double bestPerformingAssetValue(Wallet wallet) {
        return bestPerformingAssetWithGrowthRate(wallet).getValue();
    }

    private Map.Entry<String, Double> worstPerformingAssetWithGrowthRate(Wallet wallet) {
        Map<String, Double> initialValueMap = totalValueOfAssetAtPurchasePrice(wallet);
        Map<String, Double> currentValueMap = totalValueOfAssetAtCurrentPrice(wallet);

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

    public String worstPerformingAsset(Wallet wallet) {
        return worstPerformingAssetWithGrowthRate(wallet).getKey();
    }

    public double worstPerformingAssetValue(Wallet wallet) {
        return worstPerformingAssetWithGrowthRate(wallet).getValue();
    }
}
