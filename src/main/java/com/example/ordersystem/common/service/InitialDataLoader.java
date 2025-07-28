package com.example.ordersystem.common.service;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.domain.Role;
import com.example.ordersystem.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InitialDataLoader implements CommandLineRunner {

    public final MemberRepository memberRepository;
    public final PasswordEncoder passwordEncoder;


    @Autowired
    public InitialDataLoader(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@naver.com";
        String adminPassword = passwordEncoder.encode("12341234");

        if(memberRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }
        Member member = Member.builder()
                .email(adminEmail)
                .password(adminPassword)
                .role(Role.ADMIN)
                .name("admin")
                .build();
        memberRepository.save(member);


    }
}
