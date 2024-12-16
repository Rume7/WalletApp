package com.swisspost.controller;

import com.swisspost.model.Asset;
import com.swisspost.model.Wallet;
import com.swisspost.service.WalletService;
import com.swisspost.service.WalletValueCalculator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final WalletValueCalculator walletValueCalculator;

    public WalletController(WalletService walletService, WalletValueCalculator walletValueCalculator) {
        this.walletService = walletService;
        this.walletValueCalculator = walletValueCalculator;
    }

    @PostMapping("/create-wallet")
    public ResponseEntity<Wallet> createWallet() {
        Wallet aWallet = walletService.createWallet();
        if (aWallet == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(aWallet, HttpStatus.CREATED);
    }

    @PostMapping("/add-asset")
    public ResponseEntity<Wallet> addAssetToWallet(@RequestBody @Valid Asset asset) {
        Wallet aWallet = walletService.getWallet();
        if (aWallet == null) {
            return ResponseEntity.notFound().build();
        }
        Wallet updatedWallet = walletService.addAssetToWallet(aWallet, asset);
        return ResponseEntity.ok(updatedWallet);
    }

    @GetMapping("/get-wallet")
    public ResponseEntity<Wallet> getWallet() {
        Wallet wallet = walletService.getWallet();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(wallet);
    }

    @PutMapping("/remove-asset")
    public ResponseEntity<Wallet> removeAssetInWallet(@RequestBody Asset asset) {
        Wallet wallet = walletService.getWallet();
        if (wallet == null) {
           return ResponseEntity.notFound().build();
        }
        Wallet updatedWallet = walletService.removeAssetFromWallet(wallet, asset);
        return ResponseEntity.ok(updatedWallet);
    }

    @GetMapping("/wallet-value")
    public ResponseEntity<Double> getWalletTotalValue() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        Double value = walletValueCalculator.totalWalletValueOfAssets(wallet);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/best-asset")
    public ResponseEntity<String> getBestAsset() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        String symbol = walletValueCalculator.bestPerformingAsset(wallet);
        return ResponseEntity.ok(symbol);
    }

    @GetMapping("/best-performance")
    public ResponseEntity<Double> getBestPerformanceValue() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        Double value = walletValueCalculator.bestPerformingAssetValue(wallet);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/worst-asset")
    public ResponseEntity<String> getWorstAsset() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        String assetSymbol = walletValueCalculator.worstPerformingAsset(wallet);
        return ResponseEntity.ok(assetSymbol);
    }

    @GetMapping("/worst-performance")
    public ResponseEntity<Double> getWorstPerformanceValue() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        Double value = walletValueCalculator.worstPerformingAssetValue(wallet);
        return ResponseEntity.ok(value);
    }
}
