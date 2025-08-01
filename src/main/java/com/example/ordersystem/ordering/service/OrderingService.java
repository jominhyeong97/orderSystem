package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.domain.OrderStatus;
import com.example.ordersystem.common.service.SseAlarmService;
import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.common.service.StockRabbitMQService;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.OrderListResDto;
import com.example.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import com.example.ordersystem.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class OrderingService {
    public final OrderingRepository orderingRepository;
    public final ProductRepository productRepository;
    public final MemberRepository memberRepository;
    public final ProductService productService;
    public final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final StockRabbitMQService stockRabbitMQService;
    private final SseAlarmService sseAlarmService;

    public Long create(List<OrderCreateDto> orderCreateDtoList) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder()
                .orderStatus(OrderStatus.ORDERED)
                .member(member)
                .build();

        System.out.println("주문 요청자 이메일: " + email);

        for (OrderCreateDto orderCreateDto : orderCreateDtoList) {
            Product product = productRepository.findById(orderCreateDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("product is not found"));
            if (product.getStockQuantity() < orderCreateDto.getProductCount()) {
//                예외를 강제발생시킴으로서, 모두 임시저장사항들을 rollBack 처리
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

//            1. 동시에 접근하는 상황에서 update값의 정합성이 깨지고 갱신이상이 발생
//            2. Spring 버전이나 mysql버전에 따라 jpa에서 강제 에러(deadlock)를 유발시켜 대부분의 요청실패 발생
            product.updateStockQuantity(orderCreateDto.getProductCount());

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(orderCreateDto.getProductCount())
                    .ordering(ordering)
                    .build();
//            orderDetailRepository.save(orderDetail);
            ordering.getOrderDetailList().add(orderDetail); //cascade 적용시
        }
        orderingRepository.save(ordering);







        return ordering.getId();
    }

    public List<OrderListResDto> findAll() {
        List<OrderListResDto> orderListResDtoList = new ArrayList<>(); //리턴해줘야하는 코드
        List<Ordering> orderingList = orderingRepository.findAll(); //모든주문(안에 상세내역이 있음, 이걸 dto로 변환)

        for (Ordering ordering : orderingList) {
            orderListResDtoList.add(OrderListResDto.fromEntity(ordering));
        }
        return orderingRepository.findAll().stream().map(OrderListResDto::fromEntity).collect(Collectors.toList());

    }


    public List<OrderListResDto> myOrders() {
        List<OrderListResDto> orderListResDtoList = new ArrayList<>(); //리턴해줘야하는 코드
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(email);
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("해당이메일을 가진 유저 없습니다."));

        return orderingRepository.findByMember(member).stream().map(OrderListResDto::fromEntity).collect(Collectors.toList());

    }


    @Transactional(isolation = Isolation.READ_COMMITTED) //격리레벨을 낮춤으로서 성능향상과 lock관련 문제 원천 차단
    public Long createConcurrent(List<OrderCreateDto> orderCreateDtoList) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder()
                .orderStatus(OrderStatus.ORDERED)
                .member(member)
                .build();

        for (OrderCreateDto orderCreateDto : orderCreateDtoList) {
            Product product = productRepository.findById(orderCreateDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("product is not found"));

//            redis에서 재고수량 확인 및 재고수량 감소처리
            int newQuantity = stockInventoryService.decreaseStockQuantity(product.getId(), orderCreateDto.getProductCount());
            if(newQuantity < 0) {
                throw new IllegalArgumentException("재고부족");
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(orderCreateDto.getProductCount())
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetailList().add(orderDetail);
//            rdb에 사후 update를 위한 메시지 발행 (비동기처리)
            stockRabbitMQService.publish(orderCreateDto.getProductId(),orderCreateDto.getProductCount());
        }
        orderingRepository.save(ordering);
//        주문 성공시 admin 유저에게 알림메시지 전송
        sseAlarmService.publishMessage("admin@naver.com",email,ordering.getId());


        return ordering.getId();
    }




    public Ordering cancel(Long id) throws IllegalArgumentException{
//        ordering의 db 시에 값 변경 canceled
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("해당 주문번호 없음"));

        if(ordering.getOrderStatus().equals(OrderStatus.CANCELED)) {
            throw new IllegalArgumentException("이미 취소한 주문입니다.");
        }
        ordering.CancelStatus();

        for(OrderDetail orderDetail : ordering.getOrderDetailList()) {
//            rdb 재고 업데이트
            Product product = orderDetail.getProduct();
            product.cancelOrder(orderDetail.getQuantity());
//        redis에 재고값 증가
            stockInventoryService.increaseStockQuantity(orderDetail.getProduct().getId(),orderDetail.getQuantity());
        }

//        rebbitmq에 재고 증가메시지 발행

        return ordering;
    }

}
