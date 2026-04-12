package com.pawnavz.controller;

import com.pawnavz.dto.request.CreateAddressRequest;
import com.pawnavz.dto.request.UpdateAddressRequest;
import com.pawnavz.dto.response.AddressResponse;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.security.JwtUtil;
import com.pawnavz.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses")
public class AddressController {

    private final AddressService addressService;
    private final JwtUtil jwtUtil;

    private String uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }

    @GetMapping
    @Operation(summary = "Get all addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAll(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.success(addressService.getAllAddresses(uid(auth))));
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "Get single address")
    public ResponseEntity<ApiResponse<AddressResponse>> getOne(
            @RequestHeader("Authorization") String auth,
            @PathVariable String addressId) {
        return ResponseEntity.ok(ApiResponse.success(
                addressService.getAddress(uid(auth), addressId)));
    }

    @PostMapping
    @Operation(summary = "Add a new address")
    public ResponseEntity<ApiResponse<AddressResponse>> create(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Address added",
                        addressService.createAddress(uid(auth), request)));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update an address")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @RequestHeader("Authorization") String auth,
            @PathVariable String addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Address updated",
                addressService.updateAddress(uid(auth), addressId, request)));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("Authorization") String auth,
            @PathVariable String addressId) {
        addressService.deleteAddress(uid(auth), addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted"));
    }

    @PatchMapping("/{addressId}/default")
    @Operation(summary = "Set address as default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @RequestHeader("Authorization") String auth,
            @PathVariable String addressId) {
        return ResponseEntity.ok(ApiResponse.success("Default address updated",
                addressService.setDefaultAddress(uid(auth), addressId)));
    }
}
