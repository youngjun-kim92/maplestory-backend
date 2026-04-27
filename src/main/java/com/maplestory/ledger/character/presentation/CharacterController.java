package com.maplestory.ledger.character.presentation;

import com.maplestory.ledger.character.application.CharacterService;
import com.maplestory.ledger.character.presentation.dto.CharacterROIResponse;
import com.maplestory.ledger.character.presentation.dto.CharacterRequest;
import com.maplestory.ledger.character.presentation.dto.CharacterResponse;
import com.maplestory.ledger.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    @PostMapping
    public ResponseEntity<CharacterResponse> createCharacter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CharacterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(characterService.createCharacter(userDetails.getUserId(), req));
    }

    @GetMapping
    public ResponseEntity<List<CharacterResponse>> getCharacters(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(characterService.getCharacters(userDetails.getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterResponse> updateCharacter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CharacterRequest req) {
        return ResponseEntity.ok(characterService.updateCharacter(userDetails.getUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        characterService.deleteCharacter(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/roi")
    public ResponseEntity<CharacterROIResponse> getCharacterROI(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(characterService.getCharacterROI(userDetails.getUserId(), id));
    }
}