package com.swisspost.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private static Wallet walletInstance;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Asset> cryptoAsset;

    private Wallet() {
        this.cryptoAsset = new HashSet<>();
    }

    public static synchronized Wallet getWalletInstance() {
        if (walletInstance == null) {
            walletInstance = new Wallet();
        }
        return walletInstance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Asset> getCryptoAssets() {
        return cryptoAsset;
    }

    public void addCryptoAsset(Asset asset) {
        this.cryptoAsset.add(asset);
    }

    public boolean removeCryptoAsset(Asset asset) {
        if (this.cryptoAsset != null && this.cryptoAsset.contains(asset)) {
            this.cryptoAsset.remove(asset);
            return true;
        }
        return false;
    }

    public void setCryptoAssets(Set<Asset> assets) {
        this.cryptoAsset.clear();
        boolean status = this.getCryptoAssets().addAll(assets);
    }
}
