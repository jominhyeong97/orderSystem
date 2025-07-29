package com.example.ordersystem.member.domain;

import com.example.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ValueGenerationType;
import org.hibernate.annotations.Where;
import org.springframework.context.annotation.Primary;

@Entity
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
//jpql을 제외하고 모든 조회쿼리에 where del_yn = "N" 붙이는 효과
@Where(clause = "del_yn = 'N'")

public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;
    @NotNull
    @Column(unique = true)
    private String email;
    private String password;

    @Builder.Default
    private String delYn= "N";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public void deleteUser() {
        this.delYn = "Y";
    }
}
