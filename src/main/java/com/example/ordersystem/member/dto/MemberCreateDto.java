package com.example.ordersystem.member.dto;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.domain.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MemberCreateDto {

    @NotNull
    private String name;
    @NotNull
    @Column(unique = true)
    private String email;
    private String password;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .name(name)
                .email(email)
                .role(Role.USER)
                .password(encodedPassword)
                .build();
    }

}
