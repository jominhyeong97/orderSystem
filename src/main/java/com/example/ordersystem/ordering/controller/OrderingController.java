package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.common.dto.CommonDto;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.service.OrderingService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
@RequiredArgsConstructor
public class OrderingController {

    public final OrderingService orderingService;


    @PostMapping("create")
    public ResponseEntity<?> createConcurrent(@Valid @RequestBody List<OrderCreateDto> orderCreateDtos) {
        Long id = orderingService.createConcurrent(orderCreateDtos);
        return new ResponseEntity<>(CommonDto
                .builder()
                .result(id)
                .status_message("주문완료")
                .status_code(HttpStatus.CREATED.value())
                .build()
                , HttpStatus.CREATED);
    }

//    @PostMapping("createDetail")
//    public ResponseEntity<?> createDetail(@Valid @RequestBody OrderDetailDto orderDetailDto) {
//        System.out.println(orderDetailDto);
//        return null;
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> findAll() {

        return new ResponseEntity<>(CommonDto
                .builder()
                .result(orderingService.findAll())
                .status_message("주문목록조회성공")
                .status_code(HttpStatus.CREATED.value())
                .build()
                , HttpStatus.CREATED);
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders() {

        return new ResponseEntity<>(CommonDto
                .builder()
                .result(orderingService.myOrders())
                .status_message("내 주문목록조회 성공")
                .status_code(HttpStatus.CREATED.value())
                .build()
                , HttpStatus.CREATED);
    }


}
