package com.library.manager.repository;

import com.library.manager.model.Loan;
import com.library.manager.model.LoanStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByMember_Id(Long memberId);

    List<Loan> findByBook_Id(Long bookId);
}
