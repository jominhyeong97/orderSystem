package com.example.ordersystem.product.service;

import com.example.ordersystem.common.config.AwsS3Config;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductCreateDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductSearchDto;
import com.example.ordersystem.product.dto.ProductUpdateDto;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    public final ProductRepository productRepository;
    public final MemberRepository memberRepository;
    public final S3Client s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public Long create(ProductCreateDto productCreateDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("유저없음"));
        Product product = productRepository.save(productCreateDto.toEntity(member));

        MultipartFile image = productCreateDto.getProductImage();
        if(image != null) {

            String fileName = "product-" + product.getId() + "-profileImage-" + image.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(image.getContentType())
                    .build();
            try {

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image.getBytes()));
            }
            catch (Exception e) {
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

            String imgUrl = s3Client.utilities().getUrl(a->a.bucket(bucketName).key(fileName)).toExternalForm();
            product.updateImageUrl(imgUrl);

        }
        return product.getId();
    }

    public Long productUpdate(ProductUpdateDto productUpdateDto,Long productId) {

        Product product = productRepository.findById(productId).orElseThrow(()->new EntityNotFoundException("값없음"));

        MultipartFile image = productUpdateDto.getProductImage();
        product.updateProduct(productUpdateDto);

        if(image != null && !image.isEmpty()) {

//            기존이미지 삭제 : 파일명으로 삭제
            String imgURl = product.getImagePath();
            String fileName = imgURl.substring(imgURl.lastIndexOf("/")+1);
            s3Client.deleteObject(a->a.bucket(bucketName).key(fileName));

//            신규이미지 등록

            String newFileName = "product-" + product.getId() + "-profileImage-" + image.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(newFileName)
                    .contentType(image.getContentType())
                    .build();
            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image.getBytes()));
            }
            catch (Exception e) {
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

            String newImgUrl = s3Client.utilities().getUrl(a->a.bucket(bucketName).key(newFileName)).toExternalForm();
            product.updateImageUrl(newImgUrl);

        }else {
//            s3에서 이미지 삭제 후 url 갱신
            product.updateImageUrl(null);
        }
        return product.getId();
    }

    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto productSearchDto) {
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//                Root : 엔티티의 속성에 접근하기 위한 객체(조건), CriteriaQuery : 쿼리를 생성하기 위한 객체
                List<Predicate> predicateList = new ArrayList<>();


                if (productSearchDto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), productSearchDto.getCategory()));
                }
                if (productSearchDto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + productSearchDto.getProductName() + "%"));
                }

                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateList.size(); i++) {
                    predicateArr[i] = predicateList.get(i);
                }
//                위의 검색 조건들을 하나(한줄)의 Predicate 객체로 만들어서 return
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }

        };
        Page<Product> productList = productRepository.findAll(specification, pageable);
        return productList.map(ProductResDto::fromEntity);
    }

    public ProductResDto findById(Long id) {
        return ProductResDto.fromEntity(productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("해당 값 없습니다.")));
    }



}
