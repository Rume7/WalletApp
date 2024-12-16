package com.swisspost.service;

import com.swisspost.exception.AssetNotFoundException;
import com.swisspost.exception.InvalidAssetQuantityException;
import com.swisspost.exception.WalletNotFoundException;
import com.swisspost.model.Asset;
import com.swisspost.model.AssetStatus;
import com.swisspost.model.Wallet;
import com.swisspost.repository.AssetRepository;
import com.swisspost.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final AssetRepository assetRepository;

    private final AssetService assetService;

    public WalletService(WalletRepository walletRepository, AssetRepository assetRepository, AssetService assetService) {
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
        this.assetService = assetService;
    }

    @Transactional
    public Wallet createAWallet() {
        List<Wallet> foundWallets = walletRepository.findAll();
        if (foundWallets.size() == 0) {
            Wallet walletInstance = Wallet.getWalletInstance();
            Wallet savedWallet = walletRepository.save(walletInstance);
            return savedWallet;
        }
        return foundWallets.get(0);
    }

    @Transactional
    public Wallet addAssetToWallet(Wallet aWallet, Asset asset) {
        Wallet foundWallet = walletRepository.findById(aWallet.getId())
                .orElseThrow(() -> new WalletNotFoundException(
                        String.format("Wallet %s was not found.", aWallet.getId())));

        AssetValidator.validate(asset);

        double currentPrice =  assetService.getCurrentAssetPrice(asset.getSymbol());

        Asset newAsset = new Asset(asset.getSymbol(), asset.getQuantity(), currentPrice, AssetStatus.BUY);
        assetRepository.save(newAsset);

        Map<String, Asset> assetMap = foundWallet.getCryptoAssets()
                                        .stream()
                                        .collect(Collectors.toMap(Asset::getSymbol, a -> a));

        assetMap.compute(asset.getSymbol(), (symbol, existingAsset) -> {
           if (existingAsset != null) {
               // updating the quantity of the existing asset in the wallet
               double newQuantity = existingAsset.getQuantity() + newAsset.getQuantity();
               return new Asset(existingAsset.getSymbol(), newQuantity, existingAsset.getPrice());
           }
           return newAsset;
        });

        foundWallet.setCryptoAssets(new HashSet<>(assetMap.values()));
        return walletRepository.save(foundWallet);
    }

    @Transactional
    public Wallet removeAssetFromWallet(Wallet aWallet, Asset asset) {
        Wallet foundWallet = walletRepository.findById(aWallet.getId())
                .orElseThrow(() -> new WalletNotFoundException(
                        String.format("Wallet %s was not found.", aWallet.getId())));

        AssetValidator.validate(asset);

        // Create a map for efficient lookup of assets in the wallet
        Map<String, Asset> assetMap = foundWallet.getCryptoAssets()
                .stream()
                .collect(Collectors.toMap(Asset::getSymbol, a -> a));

        // Check if the asset exists in the wallet
        Asset existingAsset = assetMap.get(asset.getSymbol());
        if (existingAsset == null) {
            throw new AssetNotFoundException(String.format("Asset %s not found in the wallet.", asset.getSymbol()));
        }

        // Calculate the new quantity after removal
        double newQuantity = existingAsset.getQuantity() - asset.getQuantity();

        if (newQuantity < 0) {
            throw new InvalidAssetQuantityException(
                    String.format("Cannot remove more quantity than available for asset %s. Current quantity: %.2f",
                            asset.getSymbol(), existingAsset.getQuantity()));
        } else if (newQuantity == 0) {
            // If the new quantity is zero, remove the asset entirely from the wallet
            assetMap.remove(existingAsset.getSymbol());
            foundWallet.removeCryptoAsset(existingAsset);
        } else {
            // Update the asset with the reduced quantity
            Asset updatedAsset = new Asset(existingAsset.getSymbol(), newQuantity, existingAsset.getPrice());
            assetMap.put(existingAsset.getSymbol(), updatedAsset);
        }

        foundWallet.setCryptoAssets(new HashSet<>(assetMap.values()));

        double currentPrice =  assetService.getCurrentAssetPrice(asset.getSymbol());
        Asset newAsset = new Asset(asset.getSymbol(), asset.getQuantity(), currentPrice, AssetStatus.SELL);
        assetRepository.save(newAsset);

        return walletRepository.save(foundWallet);
    }

    public Wallet getWallet() {
        List<Wallet> walletList = walletRepository.findAll();
        if (walletList.size() > 0) {
            return walletList.get(0);
        }
        throw new WalletNotFoundException("Wallet does not exist");
    }
}
