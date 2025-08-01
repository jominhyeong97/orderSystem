package com.example.ordersystem.product.controller;

import com.example.ordersystem.common.dto.CommonDto;
import com.example.ordersystem.product.dto.ProductCreateDto;
import com.example.ordersystem.product.dto.ProductSearchDto;
import com.example.ordersystem.product.dto.ProductUpdateDto;
import com.example.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    public  final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @ModelAttribute ProductCreateDto productCreateDto) {
        Long id = productService.create(productCreateDto);
        return new ResponseEntity<>(CommonDto
                .builder()
                .result(id)
                .status_message("상품등록완료")
                .status_code(HttpStatus.CREATED.value())
                .build()
                , HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> findAll(@PageableDefault(size=10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable, ProductSearchDto productSearchDto) {
        return new ResponseEntity<>(CommonDto
                .builder()
                .result(productService.findAll(pageable, productSearchDto))
                .status_message("목록조회 성공")
                .status_code(HttpStatus.OK.value())
                .build()
                , HttpStatus.OK);

    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@Valid @PathVariable Long id) {
        return new ResponseEntity<>(CommonDto
                .builder()
                .result(productService.findById(id))
                .status_message("상세조회성공")
                .status_code(HttpStatus.OK.value())
                .build()
                , HttpStatus.OK);


    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<?> productUpdate(@Valid @ModelAttribute ProductUpdateDto productUpdateDto, @PathVariable Long productId) {
        Long id = productService.productUpdate(productUpdateDto,productId);
        return new ResponseEntity<>(CommonDto
                .builder()
                .result(id)
                .status_message("수정완료")
                .status_code(HttpStatus.OK.value())
                .build()
                , HttpStatus.OK);
    }




}
