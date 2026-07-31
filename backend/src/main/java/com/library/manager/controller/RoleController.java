package com.library.manager.controller;

import com.library.manager.model.Role;
import com.library.manager.repository.RoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The roles an account can hold. Read only: the three rows come from the
 * migration, and adding a fourth would mean new authorization rules, which is
 * a code change rather than a form.
 *
 * <p>Feeds the role dropdown of the user edit screen. ADMIN only, like
 * /api/users: see SecurityConfig.
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public record RoleSummary(Long id, String name) {}

    // GET /api/roles -> index()
    @GetMapping
    public List<RoleSummary> index() {
        return roleRepository.findAll(Sort.by("name")).stream()
                .map(role -> new RoleSummary(role.getId(), role.getName()))
                .toList();
    }
}
