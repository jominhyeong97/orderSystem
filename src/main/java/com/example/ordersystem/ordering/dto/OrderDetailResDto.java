package com.example.ordersystem.ordering.dto;

import com.example.ordersystem.ordering.domain.OrderDetail;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class OrderDetailResDto {
    private Long detailId;
    private String productName;
    private Integer productCount;

    public static OrderDetailResDto fromEntity(OrderDetail orderDetail) {
        return OrderDetailResDto.builder()
                .detailId(orderDetail.getId())
                .productName(orderDetail.getProduct().getName())
                .productCount(orderDetail.getQuantity())
                .build();
    }
}
