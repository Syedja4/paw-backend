package com.pawnavz.service;

import com.pawnavz.dto.request.CreateAddressRequest;
import com.pawnavz.dto.request.UpdateAddressRequest;
import com.pawnavz.dto.response.AddressResponse;
import com.pawnavz.entity.Address;
import com.pawnavz.entity.User;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.AddressRepository;
import com.pawnavz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private static final int MAX_ADDRESSES = 10;

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses(String userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddress(String userId, String addressId) {
        return mapToResponse(findOwned(userId, addressId));
    }

    @Transactional
    public AddressResponse createAddress(String userId, CreateAddressRequest req) {
        if (addressRepository.countByUserId(userId) >= MAX_ADDRESSES) {
            throw new BadRequestException("Address limit reached (" + MAX_ADDRESSES + " max)");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        boolean makeDefault = Boolean.TRUE.equals(req.getIsDefault())
                || addressRepository.countByUserId(userId) == 0;
        if (makeDefault) addressRepository.clearDefaultByUserId(userId);

        Address address = Address.builder()
                .user(user).label(req.getLabel().toUpperCase())
                .recipientName(req.getRecipientName().trim()).phone(req.getPhone().trim())
                .line1(req.getLine1().trim())
                .line2(req.getLine2() != null ? req.getLine2().trim() : null)
                .city(req.getCity().trim()).state(req.getState().trim())
                .pincode(req.getPincode().trim()).country("India").isDefault(makeDefault)
                .build();
        return mapToResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(String userId, String addressId, UpdateAddressRequest req) {
        Address address = findOwned(userId, addressId);
        if (req.getLabel() != null && !req.getLabel().isBlank())
            address.setLabel(req.getLabel().toUpperCase());
        if (req.getRecipientName() != null && !req.getRecipientName().isBlank())
            address.setRecipientName(req.getRecipientName().trim());
        if (req.getPhone() != null && !req.getPhone().isBlank())
            address.setPhone(req.getPhone().trim());
        if (req.getLine1() != null && !req.getLine1().isBlank())
            address.setLine1(req.getLine1().trim());
        if (req.getLine2() != null) address.setLine2(req.getLine2().trim());
        if (req.getCity() != null && !req.getCity().isBlank())
            address.setCity(req.getCity().trim());
        if (req.getState() != null && !req.getState().isBlank())
            address.setState(req.getState().trim());
        if (req.getPincode() != null && !req.getPincode().isBlank())
            address.setPincode(req.getPincode().trim());
        return mapToResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String userId, String addressId) {
        Address address = findOwned(userId, addressId);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);
        if (wasDefault) {
            addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                    .stream().findFirst().ifPresent(next -> {
                        next.setIsDefault(true);
                        addressRepository.save(next);
                    });
        }
    }

    @Transactional
    public AddressResponse setDefaultAddress(String userId, String addressId) {
        Address address = findOwned(userId, addressId);
        if (Boolean.TRUE.equals(address.getIsDefault())) return mapToResponse(address);
        addressRepository.clearDefaultByUserId(userId);
        address.setIsDefault(true);
        return mapToResponse(addressRepository.save(address));
    }

    private Address findOwned(String userId, String addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found or does not belong to current user"));
    }

    public AddressResponse mapToResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId()).label(a.getLabel()).recipientName(a.getRecipientName())
                .phone(a.getPhone()).line1(a.getLine1()).line2(a.getLine2())
                .city(a.getCity()).state(a.getState()).pincode(a.getPincode())
                .country(a.getCountry()).isDefault(a.getIsDefault())
                .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt()).build();
    }
}
