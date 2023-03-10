# study_spring_boot_jpa_use
인프런 김영한 '실전! 스프링 부트와 JPA활용 1,2'

1. JPA 웹 애플리케이션 개발 필기
환경설정 

- Gradle은 라이브러리를 의존관계에 필요한 라이브러리들을 자동으로 다운받아옴

- 라이브러리 설정
    - preference > annotation 검색 > enable annotation

	라이브러리
    - 핵심 라이브러리
        - 스프링 MVC
        - 스프링 ORM
        - JPA, 하이버네이트
        - 스프링 데이터 JPA
    - 기타 라이브러리
        - H2 데이터베이스 클라이언트
        - 커넥션 풀 : 스프링 디폴트 ,, HikariCP
        - WEB (View): tymeleaf // 그래들에 디펜던시만 설정해주면 알아서 스프링부트가 다 설정해줌
            - 스프링이 밀고있는 엔진 ? 스프링이랑 통합됨
            - Natural templates라고, 웹 브라우저를 열면 그대로 열림
            - 단점 : 2.0에서는 태그를 다 닫아주는 스타일로 작성해야함 // 3.0에서는 괜찮아짐 <BR> 같은거
        - 로깅 : slf4j
        - 테스트

- 정적인 view : static 아래
- 동적인 view : templates 아래

- 라이브러리에 devtools 추가하면 build에 recompile 누르면 화면도 동기화됨 (원래는 jsp처럼 자동 동기화 안됨)

- 로그 세팅
    - yml 파일에서 loggin.level.org.hibernate.type:trace
        - 이건 파라미터에 뭐가 들어가는지 알려줌
    - p6spy - 쿼리 정보 파라미터 보여주는 외부 라이브러리
        - 파라미터값이 아닌 통 SQL 다 보여줌


요구사항 분석 및 모델링
- 다대다 사용 안하는게 좋음(관계형 DB에서는 다대다(서로 컬렉션을 갖고있는 관계)가 성립이 안되어서 중간에 인터페이스처럼 매핑 테이블을 두고 1:다 , 다:1로 매핑을 시켜줘야함 ) >> 객체에서는 ManyToMany로 매핑이 가능, 하지만 DB는 안되어서 실무에서는 사용을 안하는것이 좋다.,, 다대다이면 Single Row 어쩌구 뜨니까
- 양방향 연관관계보다는 단방향이 좋음
- 상속관계 
    - SINGLE_TABLE : 싱글테이블 전략(한 테이블에 상속되는 속성의 필드들을 다 넣고 DTYPE이라는 구분값을 넣어줌)
    - TABLE_PER_CLASS : 모아주느곳이 없는??
    - JOINED : 정규화된 스타일
- 연관관계의 주인은 단순히 외래키를 누가 관리하냐의 문제이지, 비즈니스상 우위에 있다고 주인으로 정하면 안됨.
- 일대다 다대일의 양방향 관계시(다 쪽에 외래키가 있음, 외래키가 있는쪽이 연관관계의 주인)  
- 일대일 경우 외래키를 Access를 자주 하는쪽에 둔다(주문, 배송 관계의 경우, 비즈니스 로직이 주문을 조회시 배송을 조회 하는 경우가 많으면 배송의 키 외래키로 주문의 컬럼에 둔다.)

엔티티 클래스 개발
- 객체는 필드명을 id 라고 설정해줘도 좋지만 테이블 컬럼은 구분을 지어주는게 좋아 table명_id가 좋음 
- 실무에서는 가급적 Getter를 열어두고, Setter는 꼭 필요한 경우에만 사용하는것을 추천,,
    - 변경시에는 변겅 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야함,,
- 내장 타입(타입을 만들어서 사용하는경우) 클래스에 @Embeddable 어노테이션을 붙여주고, 해당 타입을 사용하는 필드에 @Embedded를 붙여준다.
    - 값 타입은 변경 불가능하게 설계해야함, @Setter을 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스로 만들어버림, 기본 생성자가 있어야 JPA가 리플렉션, 프록시등을 사용하기 위해 기본 생성자가 필요함, protected로 설계해야 안전 
- 양방향 연관관계시 연관관계 주인을 정해줘야함
- Enum사용시, Enumerated 어노테이션을 넣어줘야함, EnumType은 디폴트로 ORDINAL로 되어있음,,(숫자로 들어가는데 중간에 스테이터스가 추가될 경우 숫자가 꼬임) >> STRING으로 써야함

엔티티 설계시 주의 점
- 엔티티에는 Setter 웬만하면 사용 금지, 변경 포이트가 너무 많아지면 유지보수가 어려움
- 모든 연관관계는 지연 로딩(Lazy)으로 설정!!!!
    - 즉시 로딩(Eager)는 예측이 어렵고, 어떤 SQL이 실행될지 추적 하기 어렵다,
    - 연관된 엔티티를 함께 DB에서 조회해야한다면 fetch join 또는 엔티티 그래프 기능을 사용한다.
    - @XToOne(OneToOne,ManyToOne) 관계는 default가 즉시로딩이므로 반드시 직접 지연로딩으로 설정해준다. 
- 컬렉션은 필드 생성시 바로 초기화 해주는것이 안전하다.
    - null 문제에서 안전하다.
    - hibernate가 엔티티를 영속화 할 때 컬렉션을 감싸서 hibernate용 내장 컬렉션으로 변경한다.
        - 따라서 컬렉션을 가급적 변경하면 안됨(hibernate용으로 래핑을 따로 하기 때문에,,)
- 테이블 명 , 컬럼명 생성 전략
    - 하이버 네이트 기존 구현은 엔티티 필드명을 그대로 테이블 명으로 사용,,
    - 스프링 부트 신규 설정
        - 카멜 케이스를 언더 스코어로 변경 
        - .은 언더스코어로 변경
        - 대문자 > 소문자로 변경
        - 설정을 바꾸고싶으면
            - 물리명 : spring.jpa.hibernate.naming.physical-strategy : 모든 논리명에 적용되고, 실제 테이블에 적용 
            - 논리명 : spring.jpa.hibernate.naming.implicit-strategy : 테이블이나 컬럼명을 명시하지 않을 때 논리명 적용
- cascade 속성 : 해당 필드 저장시 연관되어있는 필드도 같이 저장,삭제시켜버림(원래는 각각 해야하는데) 
    - 참조하는 도메인이 하나만있는경우에만 그 도메인에서 사용 
- 연관관계 편의 메서드 : 양방향 연관관계가 설정된 컬럼에서, 둘 다 저장을 시켜야 하는데, 연관관계 메서드를 통해 아예 둘을 묶어버려서 실수를 방지 


애플리케이션 구현 요구사항
- 회원 기능
    - 회원 등록 
    - 회원 조회
- 상품 기능
    - 상품 등록
    - 상품 수정
    - 상품 조회
- 주문 기능
    - 상품 주문
    - 주문 내역 조회
    - 주문 취소

애플리케이션 아키텍처
- 컨트롤러 > 서비스 > 레포지토리 > DB 순이고, Domain을 참조 할 수 있게끔,
- 간단 조회같은경우, 컨트롤러에서 바로 레포를 부를 수 있도록 설계(유연하게,, 딱딱하게 서비스에서 단순 레포로만 가는건 비효율적인 설계! )
- 개발 순서
    - 서비스 ,레포 계층 갭라 후 테스트 케이스를 통해 검증
    - 이후 웹(컨트롤러)
 

회원 기능 구현
- Repository
    - EntityManager에 @PersistenceContext 어노테이션 (자바 빈에 자동으로 em 등록을 해줌)
- Service
    - @Transactional : JPA 처리가 트랜잭션 안에서 이루어져야하기때문에 클래스 레벨 에서 선언해줌(이러면 해당 클래스 내 메서드가 전부 어노테이션 적용을 받음 ) 
        - javax와 spring이 제공하는 어노테이션이 있는데, spring이 제공하는걸로 써야 쓸 수 있는 기능이 더 많음
        - readOnly=true 옵션을 주면 조회하는 성능이 좋아짐 가급적이면 넣어주면 더 좋음
        - 보통 조회 메서드가 더 많기 때문에 클래스 레벨에선 readOnly 옵션을 주고, insert,update,delete 메서드 에서는 @Transactional 재선언 해준다.
    - @Autowired 스프링이 스프링 빈에 등록된 Repository(해당 애노테이션이 달린 객체)를 인젝션 해줌(필드 인젝션)
        - 단점 : 등록된 객체를 바꿀 수 없음,,
        - setter인젝션을 사용도 함(테스트코드 작성시 목을 직접 주입 가능)
            - 치명적인 단점 : 런타임시 변경하면 문제가됨?,, 
        - 요즘 권장하는것 : 생성자 인젝션을 사용, (롬복 사용)
            - 필드는 변경할 일이 없으니, 해당 필드를 final로 처리하는것을 권장함
            - @AllArgsConstructor 해당 클래스 내에 있는 모든 필드의 생성자를 만들어줌
            - @RequiredArgsConstructor : final이 있는 필드의 생성자를 만들어줌
                - 해당 생성자 인젝션 같은경우 스프링 부트의 SpringDATA JPA가 지원하므로, Repository에서도 일관성있게 사용 가능
- Test
    - JPA에서 persist시, commit을 하지않으면 영속성 컨텍스트에서 갖고있다가 commit이 되면 그 순간 insert 쿼리가 나감(플러쉬)
        - 롤백되면 플러쉬(영속성 컨텍스트에 있는걸 db에 반영하는것) 처리를 안함
        - @Transactional은 테스트케이스에 있으면 데이터 커밋을 안하고 롤백을 시켜버림 >> Rollback(false)를 주면 커밋 처리함
    - @Test(expected = 예외 에러)  처리해주면 해당 에러의 try-catch 처리를 해줌,
    - @RunWith(SpringRunner.class) : JUnit 실행시 스프링이랑 같이 실행한다
    - @SpringBootTest 스프링부트 띄운 상태로 (스프링 컨테이너 내에서) 테스트를 진행시키기 위해 
    - @Transactional이 테스트 케이스에 있으면, 데이터 커밋 없이 롤백을 시켜버림
    - 테스트는 메모리 DB를 사용하는게 좋은데 스프링 부트는 해당 기능을 제공,
        - 테스트 폴더 아래에 resources를 만들어 application.yml을 만듬(테스트케이스 실행시, 해당 디렉토리 아래의 파일이 우선권을 가져서 main 아래의 application.yml파일을 무시함)
        - 스프링 부트는 자체적으로 h2의 메모리 db를 지원하기때문에, 해당 yml파일만 만들어놓고 내용을 다 지워놓으면 메모리 db를 사용하게됨
        - yml 설정을 테스트 케이스용으로 따로 해주는게 좋음

상품 기능 구현
- 도메인 모델 패턴 설계를 위해 도메인에 비즈니스 로직 추가(상품 재고 )
    - 객체지향적인 설계를 고려했을 때 , 데이터를 갖고있는 쪽에 비즈니스 로직이 추가되면, 응집력이 올라감 
    - 세터를 안하고, 도메인 내부에서 addStock, removeStock 등을 통해 비즈니스 로직 처리,,

주문 기능 구현
- 도메인에 비즈니스 로직 추가 - 도메인 모델 패턴 ,, ORM 쓸 경우에는 Transaction Script Pattern 보다는 Domain model Pattern을 더 많이 씀
    - OrderItem 에 추가,, 
- Repository 
- Service
- 테스트

JPA에서 동적 쿼리 사용 방법
- 동적 쿼리를 createQuery를 통해 String으로 동적쿼리를 만드는건 일일이 치는건 너무 비효율적, 휴먼버그를 만들 수 있음,
- jpa에서 표준으로 제공하는 Criteria도 비효율적 ,, 무슨 쿼리인지 모름 !! 강사 권장 X 
- 이런 고민을 해결해주기 위해 QueryDSL이라는 라이브러리가 생김
    - 유료 강의 결ㅈㅔ 하래욤

컨트롤러 개발
- 데이터를 받는 폼을 따로 만들어서 폼받는 VO에 파라미터를 넣어줌,, 기존 도메인과는 받는 데이터가 전부다는 안맞기 때문에,, 또한 Validation처리도 애매해지기때문
    - 웹 서비스는 도메인으로 왔다갔다 해도되는데,, DTO로 변환해서 하는게 제일 깔끔함 화면을 왔다갔다하는 DTO를 권장, 엔티티는 순수하게 폼 데이터로 따로 만들어줘야 깔끔함,,  심플하면 직접 써도되는데 실무에서는 따로 만들음 폼 VO를
    - API 개발시, 절대 엔티티를 외부로 반환하면 안된다!! API는 스펙이기 때문에,, 비밀번호(민감정보) 같은거 유출도 될 수 있고 API스펙이 바뀌고 불완전해짐
        - 따라서 DTO로 변환해서 반환해줘야함,
- 로그 처리
    - Slf4j 어노테이션을 쓰면 자동으로 log 생성자가 생김
- Tymeleaf 
    - fragment : jsp의 include같은 역할
        - 실무에서는 계층형을 사용함,(hierachical-style)
        - recompile : 커맨드 쉬프트 F9
        - th:object =“${memberForm}” jsp와 비슷, th 오브젝트로 사용하겠다는 뜻, th:field=“” : id랑 name을 설정해주는것,
- Model : 컨트롤러에서 뷰로 넘어갈때 데이터 실어날라주는 역할
- @Valid : 메서드의 파라미터에 넘어오는 값 앞에 @Valid를 달아주면, 해당 타입의 필터링을 처리해준다.
    - @Valid 파라미터 뒤에 BindingResult가 있으면 오류가 해당 파라미터에 담겨서 실행이 된다. 
    - BindingResult와 Thymeleaf를 통해 에러 메세지를 화면단으로 전달시킬 수 있음
- 수정 
    - JPA에서 수정 하는 방법은 변경 감지, 병합(Merge)이 있음.
        -  JPA 가이드는 변경 감지를 권장함
            - dirty Checking,, 엔티티 매니저가 변경을 체크해서 DB에 업데이트를 자동으로 날림 (flush 하는 순간)
        - 준영속 엔티티 : 영속성 컨텍스트가 더이상 관리하지않는 엔티티
            - 새로 만든 객체여도, 식별자가 DB에 있으면 준영속 상태가 된다.(식별 할 수 있는 ID를 갖고있음)
            -  영속 상태가 아니기 때문에 영속성 컨텍스트가 자동으로 update 쿼리를 날리지 않음
            - 준영속 엔티티를 수정하는 방법
                - 변경 감지 기능 사용
                    - 영속상태의 객체를 불러와서 파라미터로 넘어온(바꿀 값) 객체의 속성을 get으로 꺼내서 영속상태의 객체에 set을 처리하면 자동으로 변경 감지에 의해 데이터가 변경 됨
                    - save를 따로 안해도 자동으로 update 쿼리가 날라감
                - 병합 (merge) 사용 : 준영속 상태의 엔티티를 영속상태로 바꿈
                    - 영속성 컨텍스트에서 객체를 찾아서 파라미터의 값으로 바꾸고, 커밋 되는 순간 변경해줌,,
                    - 모든 속성이 변경되기 때문에 일부 속성의 값이 없으면 null로 업데이트 처리 할 수 있다.(실무에서 매우 매우 위험);; 
        - PathVariable에서 id를 임의로 변경 할 수 있음, user 권한이 있는지 확인을 해야함, 
            - 요즘 세션 객체를 잘 안씀


	
2. JPA API 설계 및 최적화 필기

회원 등록 API
- 화면이랑 API간의 공통 에러처리같은부분이 다른게 있기 때문에 패키지를 분리시킴
- @ResponseBody + @Controller = @RestController
- @RequestBody : Json으로 온 Body를 해당 어노테이션이 있는 필드로 매핑해줌
- @Valid를 넣어서 검증을 해주고싶으면, VO단에 해당 필드에 대한 제약조건을 걸어준다( @NotEmpty라던지,, @min 이런거) 
    - 검증을 도메인에서 하면,, 화면마다 필요한 검증이 다를 수 있고,(어디서는 NotEmpty이런거 필요  ) 도메인을 변경하면 API 스펙 자체가 바뀌어버림 ,, 도메인은 자주 변경 될 수 있다,, 따라서 API 스펙만을 위한 DTO를 만들어야함
    - 그래서 엔티티를 외부에 노출시켜선 안됨 (받는거랑 리턴하는거랑 둘 다 !!)
    - 항상 엔티티를 파라미터로 받지말고 DTO를 새로 만들어서 사용한다.
    - 검증또한 DTO에다가 처리해주면 그때그때 필요한 검증을 사용 할 수 있음.

회원 수정 API
- 등록과 수정의 API 스펙은 보통 다름(수정은 일부만 진행하기때문)
- 엔티티에는 애노테이션 잘 안쓰고, DTO에는 많이 써도 괜찮음
- 

회원 조회 API
- 엔티티 자체를 Return해주면, 원하지않는 데이터를 노출시킬 수 있음
    - 이걸 수정하려면 JsonIgnore 쓰면 되는데 그러면 다른 API에서는 그 필드가 필요할 수 있음,,  
    - 그냥 DTO 하나를 더 만들어서 그걸로 쓰자

API 성능 최적화 : 등록과 수정은 성능 문제가 발생하지않는다.(단건이 많기 때문), 장애의 90%는 조회에서 나온다!
- 조회용 샘플 데이터 입력
    - 
- 지연 로딩과 조회 성능 최적화 : 앰플러스의 문제?,, 쿼리 한두개면 될 것을 수십개가 나가게됨;; 실무에서 JPA의 90% 성능저하는 해결이 된다.
    - 1:1 , N:1 관계에서 조회 성능을 최적화 시킨다. 
    - 주문 API를 만듬, 주문+배송정보+회원을 조회하는 API를 만듬, 실무에서 JPA를 사용하려면 100% 이해해야함
    - 간단한 주문 조회 V1 : 엔티티를 직접 노출
        - 엔티티가 직접 노출 되는 경우, 양방향 연관관계가 걸린 곳은 한곳을 JsonIgnore 처리를 해야함, 안그러면 서로 호출하면서 무한 루프 걸림
            - 그냥 엔티티 직접 노출 안해야하고 DTO로 변환해야함 무조건
        - 지연 로딩시 new(DB에서 안가져옴) 해서 객체를 가져오는게 아닌, 프록시 라이브러리를 써서 임시 객체를 생성해서 만들어냄> 에러남 (ByteBuddy) 
            - 지연 로딩을 피하기위해 즉시로딩(Eager)으로 설정하면 안됨 절대! 항상 지연 로딩을 기본으로 하고 최적화 할 경우 fetchJoin 해라;;
        - 하이버네이트5 모듈을 빈을 등록해야함, 지연로딩인경우 (프록시 객체 상태이기 때문에 걍 null 처리해버림)
        - 말했듯 엔티티를 직접 노출하면 API 스펙이 계속 수정되기 때문에;;
        - 근데 이렇게하면 Lazy-Loading으로 인한 쿼리가 너무 많이 호출된다.. > 성능 저하!
    - 주문 조회시 
        - 1. Order 조회 (SQL 1번 해서 결과가 2개가 나옴)
        - 2. 그럼 stream 루프가 2번 돌음
            - 여기서 멤버와 delivery가 각각 2번씩 돌음 (루프 1회에 member,delivery 1회씩 조회)
        - 총 5번 조회한것,, >> N(회원 N, 배송N)+1(첫번째 쿼리) 문제, >> 총 5번
        - 지연 로딩은 영속성 컨텍스트에서 1차 조회하기 때문에 이미 조회한 쿼리는 조회 생략함,
    - 이걸 최적화 하기 위해서는 Repo에 새로운 메서드 생성 후 쿼리를 생성해서 fetch join을 통해 쿼리를 한번에 부른다
        - fetch join 할 경우 연관관계에 있는 Lazy 무시하고, 데이터를 한 번에 쿼리로 다 가져온다.  - fetch join 공부하기
        - 이렇게하면 VO 값으로 조회하고, DTO로 변환시키는데 , 이럴 필요 없이 바로 DTO로 조회해주면 조회하는 컬럼의 수가 더 적어지기 때문에 최적화가 된다 (생각보다 미비함)
    - QueryDTO를 새로 만들고, Repo에 createQuery를 직접 짜서 메서드 생성, 파라미터를 필요한것만 넘겨서 생성하면 불필요한 데이터를 가져오지 않아도 됨 >> 조회 성능이 더 최적화 된다
        - 근데 이렇게하면 재사용성이 떨어진다(해당 API에서만 최적화 되어있음)
        - 따라서 해당 엔티티를 활용해 fetch join을 사용한 v3이 더 괜찮다..(트래픽에 따라 달라지는데 크게 차이는 없음!)
        - 또한 레포지토리는 순수한 엔티티 조회용이 좋음.
    - ㄷ트래픽 때문에 쿼리를 일일이 설정을 해줘야하는 상황인경우 query용 repo를 패키지를 새로 만들어서 그쪽에 넣어두고 사용.>!!
    - 쿼리 방식 선택 권장 순서
        - 1. 우선 엔티티를 DTO로 변환하는 방법을 선택
        - 2. 필요하면 페치 조인으로 성능을 최적화 한다. > 대부분의 성능 이슈 해결
        - 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
        - 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 Spring JDBC Template을 사용해서 SQL을 직접 사용한다.
- 컬렉션 조회 최적화 : N:N, 1:N,, 데이터 조인을 했을때 데이터 뻥튀기 방지..
    - 기존 조회했던 주문내역에서 추가로 주문한 상품 정보를 조회한다.
        - v1. 엔티티를 직접 노출
            - 안됨
        - v2. 엔티티를 DTO로 변환
            - 내부에 다른 엔티티가 있는 List같은경우(컬렉션) 해당 엔티티 또한 DTO로 래핑해줘야함,, 1:N 관계기 때문에 해당 컬렉션의 엔티티도 노출하면 안됨
            - 개별 컬렉션마다 또 쿼리가 나가기때문에 더 성능이 안좋아짐 >> 페치조인으로 해결
        - v3. 페치조인 최적화
            - 똑같이 하면 join을 하면서 다른 컬렉션과도 Join이 되기 때문에 중복 데이터가 다른 컬렉션 개수만큼 뻥튀기가 됨
            - createQuery에 중복을 원하지 않는 컬럼에 distinct를 붙여주면 JPA가 중복을 제거해줌, 기존 DB에서는 distinct는 한 row가 다 똑같아야 중복이 제거된다, 하지만 JPA의 createQuery의 distinct는 기존 DB에 distinct를 날려주면서 추가로 같은 엔티티가 조회되면 애플리케이션단에서 중복을 제거해줌
            - 근데 치명적인 단점은 ,, 컬렉션 페치 조인은 페이징이 불가능하다;; (메모리에서 처리함,, DB를 애플리케이션에서 다 올리고, 애플리케이션 단에서 페이징처리를 해버림;; )
            - 앞전에 distinct가 엔티티에서 애플리케이션 단에서 처리되기 때문에, db단에서는 처리가 안됨,,
            - 따라서 컬렉션 페치조인은 페이징 처리 안하는곳에서,, 또한 컬렉션이 하나만 있는곳에서만 사용한다.(1:N ,, 1:N:M일 경우 데이터 정합성이 떨어 질 수 있거나 에러가 뜸)
        - v3.1 페이징과 한계 돌파
            - 1. ToOne 관계를 모두 페치 조인을 처리한다.(데이터 뻥튀기가 되지 않기 때문에 페이징 쿼리에 영향을 주지 않음) 
            - 2. 컬렉션은 지연 로딩으로 조회한다. 
                - 지연로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size (글로벌 설정), @BatchSize (개별 설정)를 적용한다
                    - yml 에서 hibernate.default_batch_fetch_size 설정: where in을 설정하게 될 때 컬렉션의 개수를 지정함
                    - @BatchSize :  전역 설정이 아니고 개별 설정을 하는 경우에는 @BatchSize(size = 1000) 이런식으로 설정을 해주면 된다.
                        - 컬렉션이 아닌경우에는 클래스단에 적으면 된다. 
                    - 주로 전역 설정을 해준다. 사이즈가 중요함 ,..
            - 장점 : 쿼리 호출수가 1+N에서 1+1로 최적화가 된다.
                - 페치 조인 방식과 비교해서 쿼리 호출수가 약간 증가하지만, DB 데이터 전송량이 감소한다.
                - 컬렉션 페치 조인은 페이징이 불가능하지만, 이 방법은 페이징이 가능하다.
            - ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다, 따라서 ToOne 관계는 페치 조인으로 쿼리수를 줄이고, 나머지는 전역 설정으로 최적화 하자,
                - 단, maxSize는 최대가 1000개임 , 100~1000 사이를 선택하는것을 권장,
                - 이 전략은 SQL IN 절을 사용하는데, DB에 따라 In 파라미터를 1000으로 제한 하는 경우도 있음.
                - 또한 애플리케이션에서 데이터를 1000개씩 조회하면 순간적으로 부하가 늘 수 있기 때문에 스파이크 테스트 이런걸 해서 확인해보면 좋음
                - 순간 부하가 괜찮다면 1000으로 설정, 좀 부족하다하면 100으로 설정해서 증가시킴.. (메모리 사용은 100개씩 10번이나 1000개씩 1번이나 똑같음)
        - v4 : JPA에서 DTO 직접 조회,, >> 단건조회는 괜찮지만 다건인경우 1+N 발생한다.
        - v5 : ToOne 관계를 모두 페치 조인해서 가져오고, 가져온 식별자에 대한 컬렉션 데이터를 메모리에 적재 후 메모리에서 map으로 변경해서 데이터를 추출함 (쿼리 2번), 성능 좋음,,
        - v6 플랫 데이터.. : 조인을 다 해버려서 조인용 DTO로 만들고, API 스펙에 맞게 스트림 맵으로 DTO간 매핑을 한다,,
            - 장점 : 쿼리 1번이다
            - 애플리케이션 추가작업이 큼,, 
            - 페이징 불가능,, 
            - 데이터에 중복 데이터가 추가되므로 v5보다 더 느릴 수 있다.. 
    - 엔티티 조회방식은 페치조인이나 전역설정 등 코드를 거의 수정 안하고, 옵션만 약간 변경해서 다양한 성능 최적화를 시도 할 수 있다.하지만 DTO를 직접 조회하는 방식은 성능을 최적화 방식을 변경 할 때 많은 코드를 변경해야함. >> 페치조인이면 웬만하면 성능이 잘 나옴
- OSIV와 성능 최적화 : OpenSessionInView 사용시 지연로딩이 최적화 되는데,, 이걸 어느 상황에서 키고 끄는지 확인,, 
    - Open Session In View : JPA 의 Em이 하이버네이트의 Session,,
    - 스프링 부트 시작시 warn 로그로 spring.jpa.open-in-view 어쩌구가 뜸,, 
        - DB 커넥션을 획득하는 시기는 데이터베이스 트랜잭션 시작 할 때 JPA의 영속성 컨텍스트가 DB 커넥션을 가져온다.
        - OSIV가 켜져있으면 트랜잭션이 끝나도 영속성 컨텍스트가 살아있음,, (client에게 Response가 나갈때까지 계속) 이것때문에 지연 로딩이 가능한것
            - 지연 로딩은 영속성 컨텍스트가 살아있어야 가능하고, 영속성 컨텍스트는 기본적으로 데이터베이스와 커넥션을 유지한다.
            - 치명적인 단점 : 너무 오랜시간동안 DB 커넥션 리소스를 사용, 실시간 트래픽이 중요한 애플리케이션에서는 커넥션이 모자랄 수 있다,, 이것이 결국 장애로 이어짐
        - Off 시킬 수 있음
            - Transactional의 메서드가 끝나는 순간 영속성 컨텍스트와 DB 커넥션을 종료시켜버림
            - 커넥션을 유연하게 사용 가능,,
            - OSIV를 끄면 지연 로딩을 트랜잭션 안에서 처리해야되기 때문에 많은 지연 로딩 코드를 트랜잭션 안으로 넣어야함,.,.
            - 커맨드와 쿼리를 분리한다 : 화면에 맞춤 서비스를 새로 생성해서 쿼리용 서비스를 패키지를 따로 분리해서 만들어준다..
                - 이후 컨트롤러는 영속성 컨텍스트 밖이니 컨트롤러 내용을 서비스로 묶어서 처리한다.
                - OrderService : 핵심 비즈니스 로직
                - OrderQueryService : 화면이나 API에 맞춘 서비스,, (주로 읽기 전용 트랜잭션 사용)
        - OSIV를 켜면 커넥션 이슈가 있긴 하지만(커넥션 개수 제한적), 지연로딩을 아무데서나 할 수 있기 때문에 개발이 굉장히 편해짐
        - OSIV를 끄면 개발이 어려워지지만,, 성능이 좋아짐,,
            - 고객 서비스의 실시간 API는 OSIV를 끄고 , ADMIN처럼 커넥션을 많이 하지 않는 곳에서는 OSIV를 켠다..  
- QueryDSL : JPQL을 자바 객체로 변경시켜줌,, 동적쿼리!! 가능 !
    - 컴파일 시점에 문법 오류 발견
    - 동적 쿼리 가능
    - 직관적인 문법
    - 코드 재사용성,, 등 선택이 아닌 필수
