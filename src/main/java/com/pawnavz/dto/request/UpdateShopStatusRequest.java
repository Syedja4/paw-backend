package com.pawnavz.dto.request;

import com.pawnavz.entity.Shop;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateShopStatusRequest {

    @NotNull
    private Shop.ShopStatus status;
}
