package com.swisspost.controller;

import com.swisspost.model.Asset;
import com.swisspost.model.Wallet;
import com.swisspost.service.WalletService;
import com.swisspost.service.WalletValueCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/wallet")
@Tag(name = "Wallet API", description = "Operations related to wallet management and analysis.")
public class WalletController {

    private final WalletService walletService;
    private final WalletValueCalculator walletValueCalculator;

    public WalletController(WalletService walletService, WalletValueCalculator walletValueCalculator) {
        this.walletService = walletService;
        this.walletValueCalculator = walletValueCalculator;
    }

    @PostMapping("/create-wallet")
    @Operation(summary = "Create a wallet", description = "Creates a new wallet and returns its details.")
    public ResponseEntity<Wallet> createWallet() {
        Wallet aWallet = walletService.createWallet();
        if (aWallet == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(aWallet, HttpStatus.CREATED);
    }

    @PostMapping("/add-asset")
    @Operation(summary = "Add an asset to the wallet",
            description = "Adds a new asset to the wallet or updates the quantity if it already exists.")
    public ResponseEntity<Wallet> addAssetToWallet(@RequestBody @Valid Asset asset) {
        Wallet aWallet = walletService.getWallet();
        if (aWallet == null) {
            return ResponseEntity.notFound().build();
        }
        Wallet updatedWallet = walletService.addAssetToWallet(aWallet, asset);
        return ResponseEntity.ok(updatedWallet);
    }

    @GetMapping("/get-wallet")
    @Operation(summary = "Retrieve wallet details",
            description = "Fetches the details of the current wallet.")
    public ResponseEntity<Wallet> getWallet() {
        Wallet wallet = walletService.getWallet();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(wallet);
    }

    @PutMapping("/remove-asset")
    @Operation(summary = "Remove an asset from the wallet",
            description = "Removes an asset from the wallet or reduces its quantity.")
    public ResponseEntity<Wallet> removeAssetInWallet(@RequestBody Asset asset) {
        Wallet wallet = walletService.getWallet();
        if (wallet == null) {
           return ResponseEntity.notFound().build();
        }
        Wallet updatedWallet = walletService.removeAssetFromWallet(wallet, asset);
        return ResponseEntity.ok(updatedWallet);
    }

    @GetMapping("/wallet-value")
    @Operation(summary = "Get total wallet value",
            description = "Calculates and returns the total financial value of all assets in the wallet.")
    public ResponseEntity<Double> getWalletTotalValue() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        Double value = walletValueCalculator.totalWalletValueOfAssets(wallet);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/best-asset")
    @Operation(summary = "Get best-performing asset",
            description = "Fetches the symbol of the best-performing asset in the wallet.")
    public ResponseEntity<String> getBestAsset() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        String symbol = walletValueCalculator.bestPerformingAsset(wallet);
        return ResponseEntity.ok(symbol);
    }

    @GetMapping("/best-performance")
    @Operation(summary = "Get best performance value",
            description = "Returns the growth rate of the best-performing asset in the wallet.")
    public ResponseEntity<Double> getBestPerformanceValue() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        Double value = walletValueCalculator.bestPerformingAssetValue(wallet);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/worst-asset")
    @Operation(summary = "Get worst-performing asset",
            description = "Fetches the symbol of the worst-performing asset in the wallet.")
    public ResponseEntity<String> getWorstAsset() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        String assetSymbol = walletValueCalculator.worstPerformingAsset(wallet);
        return ResponseEntity.ok(assetSymbol);
    }

    @GetMapping("/worst-performance")
    @Operation(summary = "Get worst performance value",
            description = "Returns the growth rate of the worst-performing asset in the wallet.")
    public ResponseEntity<Double> getWorstPerformanceValue() {
        Wallet wallet = getWallet().getBody();
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        Double value = walletValueCalculator.worstPerformingAssetValue(wallet);
        return ResponseEntity.ok(value);
    }
}
