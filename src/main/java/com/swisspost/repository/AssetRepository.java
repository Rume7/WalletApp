package com.swisspost.repository;

import com.swisspost.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    @Query("SELECT DISTINCT a.symbol FROM Asset a")
    List<String> findAllAssetSymbols();

}
