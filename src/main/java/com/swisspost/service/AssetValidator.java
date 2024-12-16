package com.swisspost.service;

import com.swisspost.exception.InvalidAssetQuantityException;
import com.swisspost.exception.InvalidSymbolException;
import com.swisspost.model.Asset;

import java.util.Objects;

public class AssetValidator {

    static void validate(Asset asset) {
        Objects.requireNonNull(asset, "Asset cannot be null");

        if (asset.getSymbol() == null || asset.getSymbol().isBlank()) {
            throw new InvalidSymbolException("Asset symbol cannot be null or blank");
        }
        if (asset.getQuantity() <= 0) {
            throw new InvalidAssetQuantityException("Asset quantity must be greater than 0");
        }
    }
}
