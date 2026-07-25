package com.library.manager.repository;

import com.library.manager.model.Member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    List<Member> findByActiveTrue();

    List<Member> findByLastNameContainingIgnoreCase(String lastName);
}
