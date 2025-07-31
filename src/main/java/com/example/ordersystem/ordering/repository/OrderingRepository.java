package com.example.ordersystem.ordering.repository;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findByMember(Member member);
}
