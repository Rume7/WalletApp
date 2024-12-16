package com.swisspost.service;

import org.springframework.stereotype.Service;

@Service
public class AssetService {

    private final PriceFetcher priceFetcher;

    public AssetService(PriceFetcher priceFetcher) {
        this.priceFetcher = priceFetcher;
    }

    public double getCurrentAssetPrice(String symbol) {
        return priceFetcher.fetchLatestPrice(symbol);
    }
}
