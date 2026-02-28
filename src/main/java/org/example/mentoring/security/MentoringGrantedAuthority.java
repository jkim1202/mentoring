package org.example.mentoring.security;

import org.example.mentoring.entity.Role;
import org.springframework.security.core.GrantedAuthority;

public class MentoringGrantedAuthority implements GrantedAuthority {
    private final Role role;

    public MentoringGrantedAuthority(Role role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role.name();
    }
    @Override
    public String toString() {
        return "MentoringGrantedAuthority [role=" + role + "]";
    }
    @Override
    public int hashCode() {
         return role.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof MentoringGrantedAuthority) {
            return role.equals(((MentoringGrantedAuthority) obj).role);
        }
        return false;
    }
}
