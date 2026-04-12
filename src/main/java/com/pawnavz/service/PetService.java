package com.pawnavz.service;

import com.pawnavz.dto.request.CreatePetRequest;
import com.pawnavz.dto.request.UpdatePetRequest;
import com.pawnavz.dto.response.PetResponse;
import com.pawnavz.entity.Pet;
import com.pawnavz.entity.User;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.exception.UnauthorizedException;
import com.pawnavz.repository.PetRepository;
import com.pawnavz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PetResponse> getAllPets() {
        User currentUser = getCurrentUser();
        return petRepository.findByUserId(currentUser.getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public PetResponse getPet(String petId) {
        return mapToResponse(findOwnedPet(petId));
    }

    @Transactional
    public PetResponse createPet(CreatePetRequest request) {
        User currentUser = getCurrentUser();

        Pet pet = Pet.builder()
                .user(currentUser)
                .name(request.getName().trim())
                .type(request.getType().trim())
                .breed(request.getBreed())
                .age(request.getAge())
                .weight(request.getWeight())
                .build();

        return mapToResponse(petRepository.save(pet));
    }

    @Transactional
    public PetResponse updatePet(String petId, UpdatePetRequest request) {
        Pet pet = findOwnedPet(petId);

        if (request.getName() != null) {
            pet.setName(request.getName());
        }
        if (request.getType() != null) {
            pet.setType(request.getType());
        }
        if (request.getBreed() != null) {
            pet.setBreed(request.getBreed());
        }
        if (request.getAge() != null) {
            pet.setAge(request.getAge());
        }
        if (request.getWeight() != null) {
            pet.setWeight(request.getWeight());
        }

        return mapToResponse(petRepository.save(pet));
    }

    @Transactional
    public void deletePet(String petId) {
        Pet pet = findOwnedPet(petId);
        petRepository.delete(pet);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }

    private Pet findOwnedPet(String petId) {
        User currentUser = getCurrentUser();
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));

        if (!pet.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to access this pet");
        }

        return pet;
    }

    public PetResponse mapToResponse(Pet p) {
        return PetResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .type(p.getType())
                .breed(p.getBreed())
                .age(p.getAge())
                .weight(p.getWeight())
                .build();
    }
}
