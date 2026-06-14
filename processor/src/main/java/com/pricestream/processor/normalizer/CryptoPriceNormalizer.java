package com.pricestream.processor.normalizer;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.dto.CryptoMarketTick;
import com.pricestream.core.port.out.MarketDataNormalizer;
import com.pricestream.processor.domain.CryptoPriceDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper that converts a crypto market tick DTO into a MongoDB document.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CryptoPriceNormalizer extends MarketDataNormalizer<CryptoMarketTick, CryptoPriceDocument> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "baseAsset", source = "baseCurrency")
    @Mapping(target = "quoteAsset", source = "quoteCurrency")
    @Mapping(target = "change24h", source = "priceChange24h")
    @Mapping(target = "changePct24h", source = "priceChangePct24h")
    @Mapping(target = "timestamp", source = "lastUpdated")
    CryptoPriceDocument normalize(CryptoMarketTick source);

    @Override
    default AssetType assetType() {
        return AssetType.CRYPTO;
    }
}
