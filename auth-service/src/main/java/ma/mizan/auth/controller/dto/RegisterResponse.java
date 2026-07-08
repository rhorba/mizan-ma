package ma.mizan.auth.controller.dto;

import java.util.UUID;
import ma.mizan.auth.domain.Role;

public record RegisterResponse(UUID id, String email, Role role) {
}
