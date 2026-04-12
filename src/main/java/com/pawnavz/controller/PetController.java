package com.pawnavz.controller;

import com.pawnavz.dto.request.CreatePetRequest;
import com.pawnavz.dto.request.UpdatePetRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.PetResponse;
import com.pawnavz.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
@Tag(name = "Pets")
public class PetController {

    private final PetService petService;

    @GetMapping
    @Operation(summary = "Get all pets for current user")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(petService.getAllPets()));
    }

    @GetMapping("/{petId}")
    @Operation(summary = "Get single pet")
    public ResponseEntity<ApiResponse<PetResponse>> getOne(@PathVariable String petId) {
        return ResponseEntity.ok(ApiResponse.success(petService.getPet(petId)));
    }

    @PostMapping
    @Operation(summary = "Add a new pet")
    public ResponseEntity<ApiResponse<PetResponse>> create(
            @Valid @RequestBody CreatePetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Pet added", petService.createPet(request)));
    }

    @PutMapping("/{petId}")
    @Operation(summary = "Update a pet")
    public ResponseEntity<ApiResponse<PetResponse>> update(
            @PathVariable String petId,
            @Valid @RequestBody UpdatePetRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pet updated",
                petService.updatePet(petId, request)));
    }

    @DeleteMapping("/{petId}")
    @Operation(summary = "Delete a pet")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String petId) {
        petService.deletePet(petId);
        return ResponseEntity.ok(ApiResponse.success("Pet deleted"));
    }
}
