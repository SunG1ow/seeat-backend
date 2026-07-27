package com.seeat.seeatapi.domain.member.dto;

import com.seeat.seeatapi.domain.member.dto.response.MemberProfileResponse;
import com.seeat.seeatapi.domain.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "role", expression = "java(member.getRole().name())")
    MemberProfileResponse toProfileResponse(Member member);
}