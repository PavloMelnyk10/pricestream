package com.pricestream.processor.adapter.out.db;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.processor.domain.CryptoPriceDocument;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for converting between MongoDB documents and domain models.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CryptoPriceDocumentMapper {

    CryptoPrice toDomain(CryptoPriceDocument document);

    List<CryptoPrice> toDomainList(List<CryptoPriceDocument> documents);
}
