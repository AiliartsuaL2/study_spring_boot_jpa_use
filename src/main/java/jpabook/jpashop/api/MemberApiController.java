package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
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

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
    } // 엔티티 직접 노출해서 응답값을 놓음,
    // 이렇게하면 원하지않는 필드 노출시, JsonIgnored등 필요한데 이러면 엔티티에 Presentation계층(표현) 로직이 추가됨
    // 따라서 DTO를 추가해주는게 맞다!
    @GetMapping("/api/v2/members")
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))//  List Member 를 List MemberDto로 변경
                .collect(Collectors.toList()); //리스트를 컬렉션으로 바로 보내면 Json 배열타입으로 나가기때문에 유연성이 떨어짐,,

        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data //Lombok 게터세터 제공
    static class CreateMemberRequest{
        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse{
        private Long id;
        public CreateMemberResponse(Long id){
            this.id = id;
        }
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id,request.getName());
        Member findMember = memberService.findOne(id); // 업데이트에서 반환받지않고 조회하는 서비스를 하나 더 추가해서 분리시킴
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }
    @Data
    static class UpdateMemberRequest{
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }

}
