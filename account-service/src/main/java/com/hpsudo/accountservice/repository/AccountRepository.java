package com.hpsudo.accountservice.repository;

import com.hpsudo.accountservice.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Page<Account> findAllByDeletedFalse(Pageable pageable);
}
