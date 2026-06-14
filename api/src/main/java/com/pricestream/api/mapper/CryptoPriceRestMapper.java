package com.pricestream.api.mapper;

import com.pricestream.api.dto.CryptoPriceResponse;
import com.pricestream.api.dto.PagedResponse;
import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for converting domain models to REST API responses.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CryptoPriceRestMapper {

    CryptoPriceResponse toResponse(CryptoPrice domain);

    List<CryptoPriceResponse> toResponseList(List<CryptoPrice> domains);

    PagedResponse<CryptoPriceResponse> toPagedResponse(PagedResult<CryptoPrice> result);
}
