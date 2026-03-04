package org.example.tourism.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
@Tag(name = "Room Types", description = "Endpoints for managing room types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new room type")
    public RoomTypeResponseDto createRoomType(@Valid @RequestBody RoomTypeRequestDto request) {
        return roomTypeService.createRoomType(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a room type by ID")
    public RoomTypeResponseDto getRoomType(@PathVariable Long id) {
        return roomTypeService.getRoomType(id);
    }

    @GetMapping
    @Operation(summary = "Get all room types with optional hotel filter")
    public List<RoomTypeResponseDto> getAllRoomTypes(
            @RequestParam(required = false) Long hotelId) {
        if (hotelId != null) {
            return roomTypeService.getRoomTypesByHotel(hotelId);
        }
        return roomTypeService.getAllRoomTypes();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a room type")
    public RoomTypeResponseDto updateRoomType(
            @PathVariable Long id,
            @Valid @RequestBody RoomTypeRequestDto request) {
        return roomTypeService.updateRoomType(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a room type")
    public void deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
    }
}