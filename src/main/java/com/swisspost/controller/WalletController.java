package com.swisspost.controller;

import com.swisspost.model.Asset;
import com.swisspost.model.Wallet;
import com.swisspost.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/wallet")
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/create-wallet")
    public ResponseEntity<Wallet> createWallet() {
        Wallet aWallet = walletService.createAWallet();
        return new ResponseEntity<>(aWallet, HttpStatus.OK);
    }

    @PostMapping("/add-asset")
    public ResponseEntity<Wallet> addAssetToWallet(@RequestBody Asset asset) {
        // Consider adding the wallet id to the path variable
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
        Wallet aWallet = walletService.getWallet();
        Wallet updatedWallet = walletService.removeAssetFromWallet(aWallet, asset);
        return new ResponseEntity<>(updatedWallet, HttpStatus.OK);
    }
}
