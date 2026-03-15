package com.bank.assets.modules.user;

import com.bank.assets.common.enums.UserRole;
import com.bank.assets.common.enums.UserStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> withFilters(UserStatus status, UserRole role, String search) {
        return Specification
            .where(hasStatus(status))
            .and(hasRole(role))
            .and(matchesSearch(search));
    }

    private static Specification<User> hasStatus(UserStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<User> hasRole(UserRole role) {
        if (role == null) return null;
        return (root, q, cb) -> cb.equal(root.get("role"), role);
    }

    private static Specification<User> matchesSearch(String search) {
        if (search == null || search.isBlank()) return null;
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, q, cb) -> {
            q.distinct(true);
            var dept = root.join("department", JoinType.LEFT);
            return cb.or(
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(dept.get("name")), pattern)
            );
        };
    }
}
