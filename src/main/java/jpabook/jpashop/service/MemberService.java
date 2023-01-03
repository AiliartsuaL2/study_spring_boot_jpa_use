package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // 컴포넌트 스캔
@Transactional(readOnly = true)// 트랜잭션 안에서 JPA가 처리되어야 하기 때문에
@RequiredArgsConstructor
public class MemberService {
    //@Autowired // 필드 인젝션
    private final MemberRepository memberRepository;

    /**
     * 회원가입
     */

    @Transactional
    public Long join(Member member){

        validateDuplicateMember(member); // 중복회원 검증
       memberRepository.save(member); // 문제 없으면 저장하고 , db pk랑 매핑한게 key가 된다,,
       return member.getId();
    }

    private void validateDuplicateMember(Member member) { //중복회원 로직
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }


    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    /**
     * 회원 단건 조회
     */
    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }


    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id); // member가 영속상태임
        member.setName(name); //변경감지에 의해 업데이트 쿼리 날려버림
    }
}
