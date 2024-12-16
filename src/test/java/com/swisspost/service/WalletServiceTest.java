package com.swisspost.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.swisspost.exception.InvalidAssetQuantityException;
import com.swisspost.exception.WalletNotFoundException;
import com.swisspost.model.Asset;
import com.swisspost.model.AssetStatus;
import com.swisspost.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.swisspost.model.Wallet;
import com.swisspost.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private WalletService walletService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = Wallet.getWalletInstance();
        testWallet.setId(1L);
    }

    @Test
    void createWallet_ValidWallet_ReturnsCreatedWallet() {
        // Given & When
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        Wallet result = walletService.createAWallet();

        // Then
        assertEquals(testWallet, result);
        verify(walletRepository, times(1)).save(testWallet);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void createAWallet_WhenNoWalletExists_ShouldCreateNewWallet() {
        // Given
        when(walletRepository.findAll()).thenReturn(new ArrayList<>());
        Wallet newWallet = Wallet.getWalletInstance();
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        // When
        Wallet result = walletService.createAWallet();

        // Then
        assertNotNull(result);
        verify(walletRepository).findAll();
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createAWallet_WhenWalletExists_ShouldReturnExistingWallet() {
        // Given
        Wallet existingWallet = Wallet.getWalletInstance();
        List<Wallet> existingWallets = List.of(existingWallet);
        when(walletRepository.findAll()).thenReturn(existingWallets);

        // When
        Wallet result = walletService.createAWallet();

        // Then
        assertNotNull(result);
        assertEquals(existingWallet, result);
        verify(walletRepository).findAll();
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void addAssetToWallet_EmptyWallet_ShouldAddSuccessfully() {
        // Given
        testWallet.getCryptoAssets().clear(); // Clear existing assets to simulate an empty wallet

        Asset newAsset = new Asset("XRP", 2.3, 1.334);

        when(walletRepository.findById(testWallet.getId())).thenReturn(Optional.of(testWallet));
        when(assetService.getCurrentAssetPrice("XRP")).thenReturn(1.334); // Current BTC price
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Wallet result = walletService.addAssetToWallet(testWallet, newAsset);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCryptoAssets().size());

        Asset resultAsset = result.getCryptoAssets().iterator().next();
        assertEquals("XRP", resultAsset.getSymbol());
        assertEquals(2.3, resultAsset.getQuantity());
        assertEquals(1.334, resultAsset.getPrice());
        assertEquals(AssetStatus.BUY, resultAsset.getAssetStatus());

        verify(walletRepository).findById(testWallet.getId());
        verify(assetService).getCurrentAssetPrice("XRP");
        verify(assetRepository).save(any(Asset.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void addAssetToWallet_ExistingAsset_ShouldUpdateQuantity() {
        // Given
        Asset existingAsset = new Asset("ETH", 2.0, 3000.0);
        testWallet.setCryptoAssets(new HashSet<>(Collections.singletonList(existingAsset)));

        Asset newAsset = new Asset("ETH", 3.0, 3000.0);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(assetService.getCurrentAssetPrice("ETH")).thenReturn(3000.0);
        when(assetRepository.save(any(Asset.class))).thenReturn(newAsset);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // When
        Wallet result = walletService.addAssetToWallet(testWallet, newAsset);

        // Then
        assertNotNull(result);
        assertTrue(result.getCryptoAssets().stream()
                .anyMatch(a -> a.getSymbol().equals("ETH") && a.getQuantity() == 5.0));
        verify(assetRepository).save(any(Asset.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void addAssetToWallet_WalletNotFound_ShouldThrowException() {
        // Given
        Asset asset = new Asset("BTC", 1.0, 50000.0);

        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(WalletNotFoundException.class, () ->
                walletService.addAssetToWallet(testWallet, asset)
        );
    }

    @Test
    void addAssetToWallet_InvalidAssetQuantity_ShouldThrowException() {
        // Given
        Asset asset = new Asset("BTC", -1.0, 50000.0);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));

        // When & Then
        assertThrows(InvalidAssetQuantityException.class, () ->
                walletService.addAssetToWallet(testWallet, asset)
        );
    }

    @Test
    void addAssetToWallet_ZeroQuantity_ShouldThrowException() {
        // Given
        Asset invalidAsset = new Asset("BTC", 0.0, 50000.0);

        // Clear testWallet of all asset
        testWallet.getCryptoAssets().clear();

        when(walletRepository.findById(testWallet.getId())).thenReturn(Optional.of(testWallet));

        // When
        Exception exception = assertThrows(
                InvalidAssetQuantityException.class,
                () -> walletService.addAssetToWallet(testWallet, invalidAsset)
        );

        // Then
        assertEquals("Asset quantity must be greater than 0", exception.getMessage());

        verify(walletRepository).findById(testWallet.getId());
        verifyNoInteractions(assetService);
        verifyNoInteractions(assetRepository);
    }

    @Test
    void removeAssetFromWallet_PartialQuantity_ShouldUpdateSuccessfully() {
        // Given
        testWallet.getCryptoAssets().clear();

        Asset existingAsset = new Asset("SOL", 2.0, 4.0);
        testWallet.addCryptoAsset(existingAsset);

        Asset assetToRemove = new Asset("SOL", 1.0, 4.0);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(assetService.getCurrentAssetPrice("SOL")).thenReturn(4.0);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Wallet result = walletService.removeAssetFromWallet(testWallet, assetToRemove);

        // Then
        assertNotNull(result);
        Asset updatedAsset = result.getCryptoAssets().iterator().next();

        assertEquals("SOL", updatedAsset.getSymbol());
        assertEquals(1.0, updatedAsset.getQuantity());
        assertEquals(4.0, updatedAsset.getPrice());

        verify(walletRepository).findById(1L);
        verify(assetService).getCurrentAssetPrice("SOL");
        verify(assetRepository).save(any(Asset.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void removeAssetFromWallet_EntireQuantity_ShouldRemoveSuccessfully() {
        // Given
        testWallet.getCryptoAssets().clear();

        Asset existingAsset = new Asset("ETH", 1.0, 3000.0);
        testWallet.addCryptoAsset(existingAsset);

        Asset assetToRemove = new Asset("ETH", 1.0, 3000.0); // Remove the entire quantity

        when(walletRepository.findById(1L)).thenReturn(Optional.ofNullable(testWallet));
        when(assetService.getCurrentAssetPrice("ETH")).thenReturn(3000.0);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Wallet result = walletService.removeAssetFromWallet(testWallet, assetToRemove);

        // Then
        assertNotNull(result);
        assertTrue(result.getCryptoAssets().isEmpty());
        assertTrue(result.getCryptoAssets()
                .stream()
                .noneMatch(asset -> asset.getSymbol().equalsIgnoreCase(assetToRemove.getSymbol())));
    }

    @Test
    void removeAssetFromWallet_ZeroQuantity_ShouldThrowInvalidAssetQuantityException() {
        // Given
        testWallet.getCryptoAssets().clear();

        Asset existingAsset = new Asset("BTC", 2.0, 30000.0);
        testWallet.addCryptoAsset(existingAsset);

        Asset assetToRemove = new Asset("BTC", 0.0, 30000.0); // Zero quantity

        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));

        // When & Then
        InvalidAssetQuantityException exception = assertThrows(
                InvalidAssetQuantityException.class,
                () -> walletService.removeAssetFromWallet(testWallet, assetToRemove)
        );

        assertEquals("Asset quantity must be greater than 0", exception.getMessage()); // Ensure the exception message is correct

        verify(walletRepository).findById(1L);
        verifyNoInteractions(assetService);
        verifyNoInteractions(assetRepository);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
