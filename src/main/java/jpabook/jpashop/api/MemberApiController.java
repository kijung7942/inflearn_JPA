package jpabook.jpashop.api;

import jpabook.jpashop.domain.member.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 회원 등록 API
     *
     * @param member
     * @return deprecated 엔티티를 파라미터로 사용하면 안됨. => 별개의 dto 클래스 필요
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member finded = memberService.findOne(id);
        return new UpdateMemberResponse(finded.getId(), finded.getName());
    }

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result<List<MemberDto>> membersV2() {
        List<MemberDto> collect = memberService.findMembers().stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result<List<MemberDto>>(collect.size(), collect);
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    private static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    private static class UpdateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static private class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static private class MemberDto {
        private String name;
    }
}
