package com.swisspost.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private double quantity;
    private double price;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private AssetStatus assetStatus;


    public Asset(String symbol, double quantity, double price) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }

    public Asset(String symbol, double quantity, double price, AssetStatus status) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.assetStatus = status;
        this.createdAt = LocalDateTime.now();
    }

    public String getSymbol() {
        return symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public AssetStatus getAssetStatus() {
        return assetStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Double.compare(asset.quantity, quantity) == 0 &&
                Double.compare(asset.price, price) == 0 &&
                Objects.equals(symbol, asset.symbol) &&
                assetStatus == asset.assetStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, quantity, price, assetStatus);
    }
}
