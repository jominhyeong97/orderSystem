package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.domain.OrderStatus;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.OrderDetailResDto;
import com.example.ordersystem.ordering.dto.OrderListResDto;
import com.example.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import com.example.ordersystem.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class OrderingService {
    public final OrderingRepository orderingRepository;
    public final ProductRepository productRepository;
    public final MemberRepository memberRepository;
    public final ProductService productService;
    public final OrderDetailRepository orderDetailRepository;

    public Long create(List<OrderCreateDto> orderCreateDtoList) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("memeber is not found"));
        Ordering ordering = Ordering.builder()
                .orderStatus(OrderStatus.ORDERED)
                .member(member)
                .build();

        for(OrderCreateDto orderCreateDto : orderCreateDtoList) {
            Product product = productRepository.findById(orderCreateDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("product is not found"));
            if(product.getStockQuantity() < orderCreateDto.getProductCount()) {
//                예외를 강제발생시킴으로서, 모두 임시저장사항들을 rollBack 처리
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(orderCreateDto.getProductCount())
                    .ordering(ordering)
                    .build();
//            orderDetailRepository.save(orderDetail);
            ordering.getOrderDetailList().add(orderDetail); //cascade 적용시

            product.updateStockQuantity(orderCreateDto.getProductCount());
        }
        orderingRepository.save(ordering);
        return ordering.getId();
    }

    public List<OrderListResDto> findAll() {
        List<OrderListResDto> orderListResDtoList = new ArrayList<>(); //리턴해줘야하는 코드
        List<Ordering> orderingList = orderingRepository.findAll(); //모든주문(안에 상세내역이 있음, 이걸 dto로 변환)

        for(Ordering ordering : orderingList) {
            List<OrderDetailResDto> orderDetailResDtoList = new ArrayList<>(); //builder안에 넣어줘야 하는 dto리스트
            List<OrderDetail> orderDetailList = ordering.getOrderDetailList(); //1번 주문에 맞는 상세주문 리스트(1번주문 : 1번상세, 2번상세)

            for(OrderDetail orderDetail : orderDetailList) { //조립해주기
                OrderDetailResDto orderDetailResDto = OrderDetailResDto.builder()
                        .detailId(orderDetail.getId())
                        .productCount(orderDetail.getQuantity())
                        .productName(orderDetail.getProduct().getName())
                        .build();
                orderDetailResDtoList.add(orderDetailResDto); //조립 다했으면 DtoList에 담아주기

            }
            orderListResDtoList.add( //dtoList를 안에 넣어주면 끝
                    OrderListResDto.builder()
                            .id(ordering.getId())
                            .memberEmail(ordering.getMember().getEmail())
                            .orderStatus(ordering.getOrderStatus())
                            .orderDetails(orderDetailResDtoList)
                            .build()
            );

        }
        return orderListResDtoList;

















//        List<Ordering> orderingList = orderingRepository.findAll();
//        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
//        for(Ordering ordering : orderingList) {
//            List<OrderDetail> orderDetailList = ordering.getOrderDetailList();
//            List<OrderDetailResDto>  orderDetailResDtoList = new ArrayList<>();
//            for(OrderDetail orderDetail : orderDetailList) {
//                OrderDetailResDto orderDetailResDto = OrderDetailResDto.builder()
//                        .detailId(orderDetail.getId())
//                        .productName(orderDetail.getProduct().getName())
//                        .productCount(orderDetail.getQuantity())
//                        .build();
//            }
//
//            OrderListResDto dto = OrderListResDto.builder()
//                    .id(ordering.getId())
//                    .memberEmail(ordering.getMember().getEmail())
//                    .orderStatus(ordering.getOrderStatus())
//                    .orderDetails(orderListResDtoList) //여기 부분 미완성
//                    .build();
//        }
//
////        List<OrderDetail> orderDetailList = orderDetailRepository.findAll();
////        List<OrderListResDto> orderListResDtoList = orderDetailList.stream().map(OrderListResDto::fromEntity).toList();
//        return orderListResDtoList;
    }
}
