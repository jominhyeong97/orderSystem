package com.example.ordersystem.member.service;


import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.LoginReqDto;
import com.example.ordersystem.member.dto.MemberCreateDto;
import com.example.ordersystem.member.dto.MemberResDto;
import com.example.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional

public class MemberService {

    public final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Long save(@Valid MemberCreateDto memberCreateDto) {
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(memberCreateDto.getPassword());
        Member member = memberRepository.save(memberCreateDto.toEntity(encodedPassword));
        return member.getId();
    }

    public Member doLogin(LoginReqDto loginReqDto) throws IllegalArgumentException {
        boolean check = true;
        Optional<Member> optionalMember = memberRepository.findByEmail(loginReqDto.getEmail());

        if(!optionalMember.isPresent()) {
            check = false;
        }else {
            if(!passwordEncoder.matches(loginReqDto.getPassword(),optionalMember.get().getPassword())) {
                check = false;
            }
        }
        if(!check) {
            throw new IllegalArgumentException("비밀번호 또는 이메일 불일치.");
        }
        return optionalMember.get();

    }

    public List<MemberResDto> findAll() {
        return memberRepository.findAll().stream().map(MemberResDto::fromEntity).toList();
    }

    public MemberResDto findMyInfo() {
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(myEmail).orElseThrow(()-> new EntityNotFoundException());
        return new MemberResDto().fromEntity(member);

    }

    public void delete() {
        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(myEmail).orElseThrow(()-> new EntityNotFoundException());

        member.deleteUser();



    }



}
