package com.example.ordersystem.ordering.dto;

import com.example.ordersystem.common.domain.OrderStatus;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Or;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class OrderListResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    @Builder.Default
    private List<OrderDetailResDto> orderDetails = new ArrayList<>();

    public static OrderListResDto fromEntity(Ordering ordering) {
        List<OrderDetailResDto> orderDetailResDtoList = new ArrayList<>(); //builder안에 넣어줘야 하는 dto리스트

        for (OrderDetail orderDetail : ordering.getOrderDetailList()) { //조립해주기
            OrderDetailResDto orderDetailResDto = OrderDetailResDto.builder()
                    .detailId(orderDetail.getId())
                    .productCount(orderDetail.getQuantity())
                    .productName(orderDetail.getProduct().getName())
                    .build();
            orderDetailResDtoList.add(orderDetailResDto); //조립 다했으면 DtoList에 담아주기

        }
         //dtoList를 안에 넣어주면 끝
               return OrderListResDto.builder()
                        .id(ordering.getId())
                        .memberEmail(ordering.getMember().getEmail())
                        .orderStatus(ordering.getOrderStatus())
                        .orderDetails(orderDetailResDtoList)
                        .build();

    }

}
