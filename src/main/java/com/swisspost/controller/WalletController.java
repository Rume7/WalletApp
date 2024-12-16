package com.swisspost.controller;

import com.swisspost.model.Asset;
import com.swisspost.model.Wallet;
import com.swisspost.service.WalletService;
import com.swisspost.service.WalletValueCalculator;
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
        Wallet aWallet = walletService.createAWallet();
        return aWallet != null
                ? new ResponseEntity<>(aWallet, HttpStatus.CREATED)
                : new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/add-asset")
    public ResponseEntity<Wallet> addAssetToWallet(@RequestBody Asset asset) {
        Wallet aWallet = walletService.getWallet();
        Wallet updatedWallet = walletService.addAssetToWallet(aWallet, asset);
        return new ResponseEntity<>(updatedWallet, HttpStatus.OK);
    }

    @GetMapping("/get-wallet")
    public ResponseEntity<Wallet> getWallet() {
        Wallet wallet = walletService.getWallet();
        return wallet != null
                ? new ResponseEntity<>(wallet, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/remove-asset")
    public ResponseEntity<Wallet> removeAssetInWallet(@RequestBody Asset asset) {
        Wallet wallet = walletService.getWallet();
        if (wallet != null) {
            Wallet updatedWallet = walletService.removeAssetFromWallet(wallet, asset);
            return new ResponseEntity<>(updatedWallet, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/wallet-value")
    public double getWalletTotalValue() {
        return walletValueCalculator.totalWalletValueOfAssets();
    }

    @GetMapping("/best-asset")
    public String getBestAsset() {
        return walletValueCalculator.bestPerformingAsset();
    }

    @GetMapping("/best-performance")
    public double getBestPerformanceValue() {
        return walletValueCalculator.bestPerformingAssetValue();
    }

    @GetMapping("/worst-asset")
    public String getWorstAsset() {
        return walletValueCalculator.worstPerformingAsset();
    }

    @GetMapping("/worst-performance")
    public double getWorstPerformanceValue() {
        return walletValueCalculator.worstPerformingAssetValue();
    }
}
