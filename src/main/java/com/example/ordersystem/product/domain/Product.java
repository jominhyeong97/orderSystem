package com.example.ordersystem.product.domain;

import com.example.ordersystem.common.domain.BaseTimeEntity;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter

public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String imagePath;

    public void updateImageUrl(String url) {
        this.imagePath = url;

    }

    public void updateProduct(ProductUpdateDto productUpdateDto) {
        this.name = productUpdateDto.getName();
        this.category = productUpdateDto.getCategory();
        this.price = productUpdateDto.getPrice();
        this.stockQuantity = productUpdateDto.getStockQuantity();
    }

    public void updateStockQuantity(Integer orderQuantity) {
        this.stockQuantity = this.stockQuantity - orderQuantity;
    }

    public void cancelOrder(int orderQuantity) {
        this.stockQuantity = this.stockQuantity + orderQuantity;
    }




}
