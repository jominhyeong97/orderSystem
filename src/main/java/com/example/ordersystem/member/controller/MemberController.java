package com.example.ordersystem.member.controller;

import com.example.ordersystem.common.auth.JwtTokenProvider;
import com.example.ordersystem.common.dto.CommonDto;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dto.*;
import com.example.ordersystem.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")

public class MemberController {

    public final MemberService memberService;
    public final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> save(@Valid @RequestBody MemberCreateDto memberCreateDto) {
        Long id = memberService.save(memberCreateDto);

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("회원가입완료")
                        .build()
                ,HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@Valid @RequestBody LoginReqDto loginReqDto) {
        Member member = memberService.doLogin(loginReqDto);
//        at 토큰 생성
        String accessToken = jwtTokenProvider.createAtToken(member);
//        rt 토큰 생성
        String refreshToken = jwtTokenProvider.createRtToken(member);

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(
                                LoginResDto.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(refreshToken)
                                        .build()
                        )
                        .status_message("토큰 ok")
                        .status_code(HttpStatus.OK.value())
                        .build()
                ,HttpStatus.OK
        );
    }

//    rt를 통한 at 갱신 요청
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
//        rt 검증로직
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());
//        at 신규생성
        String accessToken = jwtTokenProvider.createAtToken(member);
        String refreshToken = jwtTokenProvider.createRtToken(member);

        return new ResponseEntity<>(CommonDto.builder()
                .result(
                        LoginResDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build()
                )
                .status_message("토큰 ok")
                .status_code(HttpStatus.OK.value())
                .build()
                ,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> findAll() {

//        List<MemberResDto> memberResDtoList = memberService.findAll();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.findAll())
                        .status_message("회원목록입니다.")
                        .status_code(HttpStatus.OK.value())
                        .build()
                ,HttpStatus.OK);
    }

    @GetMapping("myinfo")
    public ResponseEntity<?> findMyInfo() {

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.findMyInfo())
                        .status_code(HttpStatus.OK.value())
                        .status_message("사용자정보입니다.")
                        .build()
                ,HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete() {
        memberService.delete();
        return new ResponseEntity<>(CommonDto.builder()
                .result("ok")
                .status_message("탈퇴완료")
                .status_code(HttpStatus.OK.value())
                .build()

                ,HttpStatus.OK );
    }



}
