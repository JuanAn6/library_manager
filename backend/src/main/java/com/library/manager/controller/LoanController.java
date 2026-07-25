package com.library.manager.controller;

import com.library.manager.model.Book;
import com.library.manager.model.Loan;
import com.library.manager.model.LoanStatus;
import com.library.manager.model.Member;
import com.library.manager.repository.BookRepository;
import com.library.manager.repository.LoanRepository;
import com.library.manager.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanController(LoanRepository loanRepository,
                          BookRepository bookRepository,
                          MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    // GET /api/loans -> index()
    // Optional filters: ?status=ACTIVE, ?memberId=..., ?bookId=...
    @GetMapping
    public List<Loan> index(
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long bookId) {

        if (status != null) {
            return loanRepository.findByStatus(status);
        }
        if (memberId != null) {
            return loanRepository.findByMember_Id(memberId);
        }
        if (bookId != null) {
            return loanRepository.findByBook_Id(bookId);
        }
        return loanRepository.findAll();
    }

    // GET /api/loans/{id} -> show()
    @GetMapping("/{id}")
    public Loan show(@PathVariable Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }

    // POST /api/loans -> store()
    // Expects { "book": { "id": .. }, "member": { "id": .. }, "dueDate": "yyyy-MM-dd" }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Loan store(@RequestBody Loan loanDetails) {
        if (loanDetails.getBook() == null || loanDetails.getBook().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A book id is required");
        }
        if (loanDetails.getMember() == null || loanDetails.getMember().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A member id is required");
        }

        Book book = bookRepository.findById(loanDetails.getBook().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        Member member = memberRepository.findById(loanDetails.getMember().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No copies available for this book");
        }

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setLoanDate(loanDetails.getLoanDate() != null ? loanDetails.getLoanDate() : LocalDate.now());
        loan.setDueDate(loanDetails.getDueDate() != null ? loanDetails.getDueDate() : LocalDate.now().plusWeeks(2));
        loan.setStatus(LoanStatus.ACTIVE);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    // POST /api/loans/{id}/return -> marks a loan as returned and frees a copy
    @PostMapping("/{id}/return")
    @Transactional
    public Loan returnLoan(@PathVariable Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Loan is already returned");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    // DELETE /api/loans/{id} -> destroy()
    // Frees the copy again if the loan was still active
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void destroy(@PathVariable Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        if (loan.getStatus() != LoanStatus.RETURNED) {
            Book book = loan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        }

        loanRepository.delete(loan);
    }
}
