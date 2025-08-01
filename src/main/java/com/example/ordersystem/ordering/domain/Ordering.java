package com.example.ordersystem.ordering.domain;

import com.example.ordersystem.common.domain.BaseTimeEntity;
import com.example.ordersystem.common.domain.OrderStatus;
import com.example.ordersystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder

public class Ordering extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ordering", cascade = CascadeType.PERSIST)
    @Builder.Default
    List<OrderDetail> orderDetailList = new ArrayList<>();

    public void CancelStatus() {
        this.orderStatus = OrderStatus.CANCELED;
    }

}
