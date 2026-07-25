package com.library.manager.controller;

import com.library.manager.model.Member;
import com.library.manager.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // GET /api/members -> index()
    // Optional filters: ?active=true, ?lastName=...
    @GetMapping
    public List<Member> index(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String lastName) {

        if (Boolean.TRUE.equals(active)) {
            return memberRepository.findByActiveTrue();
        }
        if (lastName != null) {
            return memberRepository.findByLastNameContainingIgnoreCase(lastName);
        }
        return memberRepository.findAll();
    }

    // GET /api/members/{id} -> show()
    @GetMapping("/{id}")
    public Member show(@PathVariable Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    // POST /api/members -> store()
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Member store(@RequestBody Member member) {
        return memberRepository.save(member);
    }

    // PUT /api/members/{id} -> update()
    @PutMapping("/{id}")
    public Member update(@PathVariable Long id, @RequestBody Member memberDetails) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        member.setFirstName(memberDetails.getFirstName());
        member.setLastName(memberDetails.getLastName());
        member.setEmail(memberDetails.getEmail());
        member.setPhone(memberDetails.getPhone());
        member.setAddress(memberDetails.getAddress());
        member.setMembershipDate(memberDetails.getMembershipDate());
        member.setActive(memberDetails.isActive());

        return memberRepository.save(member);
    }

    // DELETE /api/members/{id} -> destroy()
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
        memberRepository.deleteById(id);
    }
}
