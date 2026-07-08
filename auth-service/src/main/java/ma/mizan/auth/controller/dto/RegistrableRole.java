package ma.mizan.auth.controller.dto;

import ma.mizan.auth.domain.Role;

/**
 * Public registration only ever creates INDIVIDUAL or BUSINESS accounts — ADMIN
 * is deliberately excluded from this enum so it can never be bound from request
 * input.
 */
public enum RegistrableRole {
	INDIVIDUAL, BUSINESS;

	public Role toDomainRole() {
		return Role.valueOf(name());
	}
}
