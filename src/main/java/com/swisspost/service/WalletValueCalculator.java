package com.swisspost.service;

import com.swisspost.model.Asset;
import org.springframework.stereotype.Service;

@Service
public class WalletValueCalculator {

    public double getBestPrice() {

        return 0.0;
    }

    public double totalWalletValue() {


        return 0.0;
    }

    public Asset bestPerformingAsset() {

        // consider returning the symbol
        return null;
    }

    // Calculate price of the asset at creation versus current price
    // and comparing each asset in terms of percentage increase.
    public double bestPerformingAssetValue() {


        return 0.0;
    }

    public Asset worstPerformingAsset() {

        // consider returning the symbol
        return null;
    }

    // Calculate price of the asset at creation versus current price
    // and comparing each asset in terms of percentage decrease.
    public double worstPerformingAssetValue() {


        return 0.0;
    }

}
