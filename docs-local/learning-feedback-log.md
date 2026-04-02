# Learning Feedback Log

## 목적
- 프로젝트 진행 중 기술 질문/코드 수정 요청마다:
- 내가 제안한 수정안
- 네가 부족했던 지점(개념/설계/구현)
- 다음에 공부하면 좋은 주제
를 계속 누적 기록한다.

## 운영 규칙
- 기본 원칙: 네가 먼저 구현하기 전에, 내가 수정본(설계/코드 방향)을 먼저 제안한다.
- 각 기록은 `날짜`, `주제`, `부족 포인트`, `수정 제안`, `학습 추천`을 포함한다.
- 구현 난이도는 MVP 기준으로 유지하고, 확장 아이디어는 분리해서 적는다.

---

## 2026-02-27

### 주제
- 멘토-멘티 매칭 서비스 설계도 1차 리뷰

### 현재 강점
- 범위 관리가 명확함(in-scope / out-of-scope 분리)
- 상태 모델과 핵심 전이 정의가 좋음
- 동시성 핵심 포인트(`reservations.slot_id UNIQUE`)를 정확히 짚음
- 구현 순서와 일정이 실천 가능한 수준으로 정리됨

### 보완 필요 포인트(현재 기준)
- 시간대(Timezone) 저장/표시 규칙이 문서에 명시되지 않음
- 취소 정책이 상태별/시점별로 API 에러코드 수준까지 구체화되지 않음
- 리뷰 캐시(avg_rating, review_count) 갱신 시 동시성 처리 방식 확정 필요

### 수정 제안(우선순위)
1. 시간 정책 추가
- DB 저장: UTC
- API 응답: ISO-8601(UTC) + 클라이언트에서 로컬 변환
- 문서에 "서버/DB/클라이언트 시간 처리 규칙" 섹션 추가

2. 취소 정책을 상태 전이표로 명문화
- 누가(멘토/멘티), 언제(시작 N시간 전), 어떤 상태에서 취소 가능한지 표로 고정
- 허용/비허용 케이스에 대한 에러코드 정의

3. 리뷰 캐시 갱신 전략 결정
- MVP: 리뷰 생성 트랜잭션에서 listing row lock 후 증분 갱신
- 데이터 커지면 배치 재계산 전략으로 전환

### 지금 공부하면 좋은 부분
- 트랜잭션 격리 수준과 `SELECT ... FOR UPDATE` 동작
- 유니크 제약 기반 동시성 제어(낙관적 실패 처리)
- 상태 머신 기반 서비스 로직 구성 방법
- 인덱스 설계 기초(정렬 + 필터 복합 인덱스)

---

## 2026-02-27

### 주제
- 프로젝트 시작 전 민감정보 관리(`.gitignore`, `.env`)

### 보완 필요 포인트(현재 기준)
- 환경변수 파일/인증서/덤프 파일이 기본 `.gitignore`에서 누락될 수 있음
- 로컬 개발 중 비밀값을 코드/설정 파일에 하드코딩할 위험이 있음

### 수정 제안
1. `.gitignore`에 민감정보/시크릿 파일 패턴 추가
- `.env`, `.env.*`, `secrets/`, 인증서/키(`*.pem`, `*.key`, `*.jks` 등), 백업/덤프(`*.sql`, `*.dump`) 무시

2. 프로젝트 루트에 `.env` 생성
- DB/JWT 기본 키를 플레이스홀더로 분리
- 실제 값은 로컬에서만 관리하고 저장소에는 커밋하지 않음

### 지금 공부하면 좋은 부분
- 12-Factor App의 Config 원칙(환경변수 분리)
- Spring Boot에서 환경변수 바인딩(`application.yml` + `${ENV_VAR}`)
- 시크릿 로테이션과 개발/운영 환경 분리 전략

---

## 2026-02-27

### 주제
- `.env`와 Spring 설정(`application.yml`) 매핑

### 보완 필요 포인트(현재 기준)
- 환경변수 키 이름과 Spring placeholder 키가 다르면 실행 시 설정 누락 위험
- `.env` 파일은 만들어도 Spring이 자동 로드하지 않으면 실제 반영되지 않을 수 있음

### 수정 제안
1. `application.yml`에 `.env` import 설정
- `spring.config.import: optional:file:.env[.properties]`
- 프로필/포트/타임존을 `${APP_*}`로 바인딩

2. `application-local.yml`을 `.env` 키 기준으로 통일
- datasource: `${DB_HOST}`, `${DB_PORT}`, `${DB_NAME}`, `${DB_USERNAME}`, `${DB_PASSWORD}`
- jwt: `${JWT_ACCESS_SECRET}`, `${JWT_REFRESH_SECRET}` 등으로 연결

3. `.env`에 누락된 앱 키 보강
- `APP_TIMEZONE` 추가

### 지금 공부하면 좋은 부분
- Spring Config Data import 동작 순서와 우선순위
- profile 분리(`application.yml` vs `application-local.yml`) 전략
- 설정 키 네이밍 컨벤션(환경별 일관성 유지)

---

## 2026-02-27

### 주제
- Flyway 적용 시작(초기 마이그레이션 생성)

### 보완 필요 포인트(현재 기준)
- Flyway 의존성은 있어도 `V1` 마이그레이션이 없으면 스키마 버전관리가 시작되지 않음
- JPA `ddl-auto: update`를 유지하면 스키마 변경 주체가 혼재될 수 있음(Flyway vs Hibernate)

### 수정 제안
1. `V1__init_users.sql` 생성
- `users` 테이블과 유니크 인덱스(`email`)를 SQL로 명시
- DB 구조의 “첫 버전”을 기록 시작

2. 로컬 설정에서 스키마 주체를 Flyway로 고정
- `spring.jpa.hibernate.ddl-auto=validate`
- `spring.flyway.enabled=true`, `locations=classpath:db/migration`

### 지금 공부하면 좋은 부분
- Flyway 버전 규칙(`V1`, `V2`...)과 롤포워드 방식(되돌리기 대신 다음 버전으로 수정)
- `ddl-auto` 옵션 차이(`none`/`validate`/`update`/`create`)
- 마이그레이션 파일 작성 시 idempotency/순서 의존성 주의점

---

## 2026-02-28

### 주제
- Sprint 1 진행 전, 현재 본인이 어렵다고 명시한 항목

### 부족하다고 느끼는 부분(사용자 직접 명시)
1. JWT 개념은 이해하지만, 파싱/생성 코드를 직접 구현하는 방법이 익숙하지 않음
2. Error Enum 설계/구현 방법이 익숙하지 않음

---

## 2026-02-28

### 주제
- `JwtTokenProvider` 구현 피드백

### 내가 몰랐던 핵심 포인트
1. `JwtTokenProvider`는 단순 유틸 클래스가 아니라, 토큰 발급/검증 규칙을 한 곳에서 관리하는 보안 핵심 컴포넌트다.
2. access/refresh 토큰은 목적이 다르기 때문에 secret key와 만료시간을 분리해야 한다.
3. `.env`에서 받은 secret은 문자열이므로, 런타임에 `SecretKey`로 변환해서 써야 한다.
4. 토큰 검증 시 라이브러리 예외를 그대로 노출하지 말고 `ErrorCode` 기반 `BusinessException`으로 변환해야 한다.
5. 토큰 생성 시 `expiration(now)`로 넣으면 즉시 만료되므로, 계산한 만료 시각 변수(`expiration`)를 넣어야 한다.

### JwtTokenProvider가 해야 하는 일
1. Access/Refresh 토큰 생성
2. 토큰에서 사용자 식별 정보(subject/email, userId claim) 추출
3. 토큰 유효성 검증(만료, 위변조, 형식 오류)
4. 검증 실패를 서비스 에러 코드(`AUTH_EXPIRED_TOKEN`, `AUTH_INVALID_TOKEN` 등)로 매핑
5. access/refresh 각각 다른 키로 파싱/검증

### 구현에 쓰는 핵심 클래스
1. `Jwts`
- JJWT 라이브러리의 진입점 클래스
- `builder()`로 토큰 생성, `parser()`로 토큰 파싱/검증 수행
- 실제 서명/검증 흐름을 구성할 때 가장 중심이 되는 클래스

2. `Claims`
- JWT payload(클레임) 데이터를 담는 객체
- 표준 클레임(`sub`, `exp`, `iat`)과 커스텀 클레임(`userId`, `role`)을 조회할 때 사용
- 토큰에서 사용자 정보와 만료시간을 꺼내는 작업의 핵심 타입

### 다음에 구현할 때 체크할 것
1. `@Value("${...}")` placeholder 문법 확인
2. access/refresh 만료시간 상수 분리
3. 검증 메서드에서 예외 매핑 누락 여부 점검
4. 토큰 생성 시 `issuedAt`, `expiration`, `signWith` 3요소 누락 없이 작성

---

## 2026-02-28

### 주제
- `JwtAuthenticationFilter` 구현 피드백

### 내가 몰랐던 핵심 포인트
1. 필터의 목적은 "토큰 검증" 자체보다, 검증이 끝난 사용자 정보를 `SecurityContext`에 넣어 인증 상태를 완성하는 것이다.
2. `Authorization` 헤더 파싱(`Bearer ` 접두사 제거)에서 null/빈값/형식 오류를 안전하게 처리해야 한다.
3. 이미 인증 정보가 `SecurityContext`에 있으면 중복 인증 세팅을 피해야 한다.
4. 토큰 검증 실패를 필터 내부에서 임의 응답 처리하기보다, 보통 `AuthenticationEntryPoint`와 예외 처리 흐름으로 일관되게 넘기는 편이 유지보수에 좋다.
5. 필터는 인가(권한 결정) 로직을 직접 하지 않고, 인증 객체 생성까지 책임진다.

### JwtAuthenticationFilter가 해야 하는 일
1. 요청 헤더에서 Bearer 토큰 추출
2. 토큰 유효성 검증(`JwtTokenProvider`)
3. 토큰에서 사용자 식별값(email 등) 추출
4. `UserDetailsService`로 사용자 조회
5. `UsernamePasswordAuthenticationToken` 생성 후 `SecurityContextHolder`에 저장
6. 이후 `filterChain.doFilter()`로 요청 계속 전달

### 구현에 쓰는 핵심 클래스
1. `SecurityContextHolder`
- 현재 요청 스레드의 인증 정보를 저장/조회하는 컨텍스트
- 여기에 `Authentication`을 넣어야 이후 인가 로직이 인증된 사용자로 동작

2. `UsernamePasswordAuthenticationToken`
- Spring Security의 표준 인증 객체
- principal(`UserDetails`), credentials(null), authorities를 담아 `SecurityContext`에 저장할 때 사용

### 다음에 구현할 때 체크할 것
1. `Authorization` 헤더가 없거나 `Bearer ` 형식이 아니면 바로 다음 필터로 넘기기
2. `SecurityContextHolder.getContext().getAuthentication() == null` 조건 확인 후 세팅
3. 토큰에서 꺼낸 email과 조회된 `UserDetails` 정합성 확인
4. 필터 등록 순서 확인(`UsernamePasswordAuthenticationFilter` 이전)

---

## 2026-03-01

### 주제
- Auth API 구현 및 컨트롤러 테스트 정리

### 내가 몰랐던 핵심 포인트
1. `AuthenticationManager`를 별도 구현하지 않아도 `AuthenticationConfiguration#getAuthenticationManager()`로 기본 인증 매니저를 사용할 수 있다.
2. 로그인 실패 시 `AuthenticationException`을 `BusinessException(AUTH_LOGIN_FAILED)`로 변환해야 에러 응답이 일관된다.
3. refresh 토큰 파싱은 access 키가 아니라 refresh 키 경로로 분리해야 한다.
4. `@WebMvcTest`에서도 커스텀 보안 필터 빈이 컨텍스트 로딩을 깨뜨릴 수 있어, 테스트에서 `@MockitoBean`으로 필터를 대체하는 방식이 필요하다.
5. 컨트롤러에서 예외를 `try-catch`로 처리해 `200 OK`를 반환하면 안 되고, `GlobalExceptionHandler`로 위임해야 한다.

### 수정 제안(적용)
1. Auth 도메인 분리
- `auth/controller`, `auth/service`, `auth/dto`로 구조 분리

2. Auth API 구현
- `register/login/refresh` 컨트롤러/서비스 구현
- JWT access/refresh 발급/재발급 흐름 연결

3. 테스트 안정화
- `AuthControllerTest`에서 `@MockitoBean JwtAuthenticationFilter` 추가
- 보안 필터 의존성으로 인한 `NoSuchBeanDefinitionException` 해결

### 지금 공부하면 좋은 부분
- `@WebMvcTest` 슬라이스 테스트 범위와 보안 자동구성의 상호작용
- `AuthenticationException`과 도메인 예외 매핑 전략
- refresh token rotation(재발급 시 refresh 재발행) 정책 비교

---

## 2026-03-04

### 주제
- Listing API(목록/상세/수정) 및 컨트롤러 테스트

### 내가 몰랐던 핵심 포인트
1. 부분 수정은 `POST`보다 `PATCH`가 의미적으로 맞고, DTO도 nullable 필드 중심으로 설계한다.
2. 수정 권한 검증은 리소스 ID가 아니라 작성자 ID(`mentor_user_id`) 기준으로 해야 한다.
3. `@WebMvcTest`에서 `@AuthenticationPrincipal`이 있을 때는 테스트에서 인증 컨텍스트를 명시적으로 넣어야 한다.
4. Mockito 스텁은 실제 호출 인자와 정확히 일치해야 하며, 인자 매칭이 빗나가면 `null` 반환으로 응답 body가 비어 보일 수 있다.

### 수정 제안(적용)
1. `PATCH /api/listings/{id}`로 수정 API 정정
2. 권한 검증 로직 수정
- `listing.getId().equals(loginId)` 제거
- `listing.getMentor().getId().equals(loginId)` 적용
3. `ListingControllerTest` 추가
- 목록 조회 성공
- 상세 조회 성공
- 수정 성공
- 수정 권한 실패(403)

### 지금 공부하면 좋은 부분
- Spring MVC에서 `@ModelAttribute` vs `@RequestBody` 선택 기준
- `@AuthenticationPrincipal` 동작 원리와 테스트 주입 방법
- Page 응답 직렬화 안정화 방식(`PageImpl` 직접 노출 vs 전용 응답 DTO)

---

## 2026-03-05

### 주제
- Listing 검색 API(Querydsl)와 서비스/레포지토리 역할 분리

### 내가 몰랐던 핵심 포인트
1. 검색 모델은 단일 `Reservation`이 아니라 `Application -> Reservation` 2단계 상태전이를 기준으로 설계해야 한다.
2. Querydsl 필터 메서드는 `null` 반환으로 동적 where 조건을 조합할 수 있다.
3. `Sort.Order`(Spring)와 `Order`(Querydsl)는 이름이 같아 충돌할 수 있어 import/타입을 명확히 분리해야 한다.
4. `PathBuilder.get(...)` 동적 정렬은 제네릭 타입 이슈가 생길 수 있어, 화이트리스트 `switch` 방식이 안전하고 유지보수에 유리하다.
5. `minPrice > maxPrice` 검증은 repository가 아니라 service에서 처리하는 게 책임 분리에 맞다.
6. API 응답은 엔티티(`List<Listing>`)를 직접 반환하지 않고 DTO로 변환해서 내려야 한다.
7. `@QueryProjection`(`QDto`) 방식은 타입 안정성이 높지만 DTO가 Querydsl에 결합되므로, MVP 단계에서는 과도할 수 있다.
8. Querydsl custom repository 구현 클래스의 생성자 주입은 단일 생성자면 `@Autowired` 없이도 동작한다.

### 수정 제안(적용/권장)
1. `ListingSearchRequestDto.placeType`을 `String`에서 `PlaceType` enum으로 변경
2. topic 검색은 `containsIgnoreCase` 사용
3. 정렬은 서비스에서 `Pageable` 생성 후, repository에서 Querydsl `orderBy`로 반영
4. 가격 범위 오류는 `BusinessException(ErrorCode.LISTING_INVALID_PRICE_RANGE)`로 서비스에서 처리

### 나중에 퀴즈로 풀 문제 후보
1. 왜 `minPrice > maxPrice` 검증을 repository가 아닌 service에서 해야 하는가?
2. `Sort.Order`와 Querydsl `Order` 충돌이 발생하는 이유와 해결법은?
3. Querydsl에서 동적 where 조합 시 `BooleanExpression` 메서드가 `null`을 반환하면 어떤 동작을 하는가?
4. `PATCH /api/listings/{id}`가 `PUT`보다 적합한 이유는?
5. `List<Listing>` 직접 반환과 DTO 반환의 차이(보안/결합도/유지보수)는?

---

## 2026-03-06

### 주제
- Listing 생성/상태변경 API 구현 + PR 워크플로우 적용

### 내가 몰랐던 핵심 포인트
1. `@WebMvcTest` 통과는 컨트롤러 입출력 검증이며, 엔티티 상태전이 로직 검증과는 별개다.
2. mock 테스트에서 서비스를 스텁하면 실제 도메인 로직(`setStatus`)은 실행되지 않는다.
3. Mockito에서 matcher(`any`, `eq`)와 raw value를 섞으면 `InvalidUseOfMatchersException`이 발생한다.
4. 생성 API는 일반적으로 `200 OK`보다 `201 Created`가 의미적으로 맞다.
5. 상태 전이는 서비스가 직접 조건문으로 처리해도 되지만, 엔티티 메서드(`setStatus`)로 캡슐화하면 규칙 누락을 줄일 수 있다.
6. `Able to merge`는 충돌 여부만 보장하고, 테스트 통과/품질 보장은 아니다.
7. PR 흐름의 핵심은 `작업 브랜치 -> 커밋 -> 원격 브랜치 push -> PR 생성 -> self-review -> merge` 순서다.

### 수정 제안(적용)
1. 생성 API 응답 코드를 `201 Created`로 정정
2. `ListingStatusUpdateRequestDto` 분리
3. `LISTING_INVALID_STATUS_TRANSITION` 에러코드 추가
4. `ListingStatus.canChangeTo` + `Listing.setStatus` 조합으로 상태 전이 규칙 반영
5. ListingController 테스트 보강(생성/상태변경)

### 지금 공부하면 좋은 부분
- 컨트롤러 테스트와 서비스/도메인 테스트의 책임 경계
- Mockito matcher 사용 규칙(`eq`, `any` 혼용 주의)
- HTTP 상태코드 설계 원칙(생성/수정/삭제)
- GitHub PR 기반 협업 절차와 self-review 체크리스트

### 나중에 퀴즈로 풀 문제 후보
1. 왜 `@WebMvcTest`만으로는 상태전이 버그를 놓칠 수 있는가?
2. `given(service.method(1L, any(), req))`가 실패하는 이유는?
3. `POST /api/listings`에서 `201`을 쓰는 이유는?
4. `Able to merge`가 의미하는 것과 의미하지 않는 것은?
5. 상태전이 로직을 엔티티에 둘 때 장점/주의점은?

---

## 2026-03-16

### 주제
- 예약 취소 후 슬롯 재사용 정책 반영 과정에서 락과 DB 정합성 재설계

### 내가 몰랐던 핵심 포인트
1. 서비스 레벨 `exists` 체크는 사용자에게 읽기 좋은 도메인 예외를 주기 위한 사전 검증이고, 동시 요청까지 완전히 막는 최종 장치는 아니다.
2. "취소 후 슬롯 재사용" 정책을 적용하면 기존 `reservations.slot_id UNIQUE`는 더 이상 맞는 제약이 아니며, 비즈니스 정책과 DB 제약이 같은 의미를 말해야 한다.
3. `active_slot_id` 같은 generated column은 애플리케이션 핵심 상태가 아니라 DB 정합성을 위한 파생 값이므로, 엔티티에 굳이 매핑하지 않아도 된다.
4. `PESSIMISTIC_WRITE` 락은 같은 슬롯을 동시에 예약하려는 요청을 직렬화해서, `조회 -> 검증 -> 예약 생성 -> 슬롯 BOOKED` 흐름을 한 트랜잭션 단위로 안전하게 묶어준다.
5. 슬롯은 재사용 가능한 자원이고, 예약은 이력이다. 그래서 재예약은 기존 `Reservation` 재사용이 아니라 새 `Reservation` row 생성으로 봐야 한다.

### 수정 제안(적용)
1. `Slot` 상태 전이 메서드 분리
- `book()` / `reopen()`으로 목적형 메서드 분리

2. 예약 생성 책임 정리
- `ReservationService.createReservation()`에서 슬롯 락 획득, 활성 예약 존재 확인, 슬롯 `BOOKED`, 예약 저장까지 한 흐름으로 처리

3. DB 제약 재설계
- `slot_id UNIQUE` 제거
- `active_slot_id` generated column + unique 제약 추가
- 활성 상태(`PENDING_PAYMENT`, `CONFIRMED`)에서만 슬롯 중복 예약 방지

4. 서비스/테스트 보강
- `existsBySlotIdAndStatusIn(...)` 사전 체크 추가
- 동일 슬롯 수락 실패 전파
- active 예약 중복 생성 실패
- 예약 취소 후 재예약 성공 테스트 추가

### 지금 공부하면 좋은 부분
- 비관적 락(`PESSIMISTIC_WRITE`)과 `SELECT ... FOR UPDATE` 동작 방식
- 서비스 체크와 DB 제약의 역할 분리
- generated column과 unique constraint를 이용한 조건부 유니크 제약
- "자원(Slot)"과 "이력(Reservation)"을 분리해서 모델링하는 방법

---

## 2026-03-19

### 주제
- Reservation 조회 API 설계에서 “예약”을 1회성 수업 일정으로 해석하기

### 내가 몰랐던 핵심 포인트
1. 현재 `Reservation`은 반복 수업 패키지가 아니라, 시간/장소가 확정된 **1회성 멘토링 일정**으로 보는 것이 현재 모델과 가장 잘 맞는다.
2. 반복 수업(매주/격주)을 지금 넣기 시작하면 `Reservation` 하나가 아니라 반복 규칙과 회차(Session) 모델까지 새로 설계해야 해서 범위가 크게 커진다.
3. 예약 탭에서 중요한 건 생성일보다 실제 수업 시각인 경우가 많아서, 기본 정렬 후보로 `startAt ASC`를 먼저 고려하는 게 자연스럽다.
4. 메인 탭을 더 세밀하게 보면 `PENDING`, `UPCOMING`, `COMPLETED`로 나누는 편이 현재 상태 모델과 더 잘 맞고, `CANCELED`는 메인 탭에 섞기보다 분리하는 편이 UX상 더 낫다.
5. 조회 관점의 `MENTOR` / `MENTEE`는 사용자 권한(role)이 아니라, 해당 예약에서 내가 어떤 입장으로 참여했는지를 뜻한다.

### 수정 제안(적용/확정)
1. Reservation 조회 기준 확정
- `view=MENTOR | MENTEE`

2. 메인 탭 정책 확정
- `PENDING`: 결제/확정 대기 중인 일정(`PENDING_PAYMENT`)
- `UPCOMING`: 진행 예정인 일정(`CONFIRMED`)
- `COMPLETED`: 완료된 수업(`COMPLETED`)
- `CANCELED`: 메인 탭에서 분리

3. 정렬 우선순위 확정
- `SOONEST`: `startAt ASC`
- `LATEST`: `createdAt DESC`

### 지금 공부하면 좋은 부분
- 일정/예약 도메인에서 “신청”, “예약”, “반복 수업” 모델을 어떻게 분리하는지
- 목록 API에서 필터와 정렬을 어떤 사용자 시나리오 기준으로 정하는지
- 페이지네이션을 초기에 넣는 것과 나중에 붙이는 것의 비용 차이

---

## 2026-03-21

### 주제
- Reservation 목록/상세 조회 API 구현 마무리

### 내가 확인한 핵심 포인트
1. 목록 조회는 `view`, `filter`, `sort`, `page`, `size`를 명확히 분리하면 사용자 시나리오와 API 구조가 같이 정리된다.
2. `PENDING_PAYMENT`는 “곧 진행될 수업”보다 “결제/확정 대기” 성격이 강해서 `UPCOMING`과 분리하는 게 더 자연스럽다.
3. 예약 상세 조회는 단순 `findById()`보다 fetch join 기반 쿼리로 `listing`, `mentor`, `mentee`, `slot`을 한 번에 가져오는 편이 DTO 변환과 쿼리 수 측면에서 더 적절하다.
4. 목록 조회는 빈 페이지 반환이 자연스럽고, 단건 조회는 `NOT_FOUND`와 `AUTH_FORBIDDEN`을 명확히 나누는 쪽이 API 의미가 선명하다.

### 적용/확정
1. `GET /api/reservations`
- `view=MENTOR|MENTEE`
- `filter=PENDING|UPCOMING|COMPLETED`
- `sort=SOONEST|LATEST`
- paging(`page`, `size`) 반영

2. `GET /api/reservations/{id}`
- 예약 당사자(멘토/멘티)만 조회 가능
- 상세 응답에 일정/상대방/장소 정보 포함

3. 테스트
- `ReservationControllerTest`: 목록/상세 조회 성공 케이스 반영
- `ReservationServiceTest`: 목록/상세 조회 성공 및 상세 조회 권한 실패 반영

### 추가 판단
1. 현재 프로젝트에서 채팅은 핵심 실시간 도메인이라기보다 Reservation 이후 일정 조율을 위한 보조 기능에 가깝다.
2. 그래서 MVP 채팅은 WebSocket/STOMP보다 Reservation 기반 1:1 REST 메시지 저장/조회로 먼저 가는 편이 구현 난이도와 프로젝트 중심을 더 잘 통제한다.
3. 실시간 WebSocket/STOMP는 후속 확장 범위로 두는 것이 현재 단계에 더 적절하다.

### 추가 학습 필요
1. Querydsl을 문법 암기보다 “왜 필요한지” 기준으로 다시 정리하기
- JPQL 문자열 기반 쿼리의 한계
- 동적 조건 추가가 필요한 이유
- Repository 메서드명 기반 조회 방식의 한계

2. Querydsl 핵심 구성요소 익히기
- `select / from / where / fetch`
- `BooleanExpression`
- `OrderSpecifier`
- `offset / limit`
- `PageableExecutionUtils`

3. 현재 프로젝트 코드로 반복 학습하기
- `ReservationRepositoryCustomImpl`
- `ApplicationRepositoryCustomImpl`
- `Listing` 검색 Querydsl 코드

4. 특히 이해할 포인트
- 조건 메서드 분리 패턴 (`mentorIdEq`, `statusEq` 등)
- `join`과 `fetchJoin` 차이
- 목록 조회의 content query / count query 분리 이유
- 왜 DTO 변환 시 필요한 연관 엔티티는 fetch join으로 당겨오는지

---

## 2026-03-07

### 주제
- 리포지토리 리뷰 결과 반영(우선순위 재정렬, 설계/테스트/문서화 보강)

### 내가 몰랐던 핵심 포인트
1. 포트폴리오에서 점수를 크게 올리는 구간은 기능 수보다 핵심 도메인 완성도(상태전이/중복방지/트랜잭션)다.
2. Listing/Auth 안정화는 기반 공사이고, 본 프로젝트의 핵심 평가는 `Application -> Reservation` 흐름에서 결정된다.
3. JWT 설정 키를 여러 경로(`application-local.yml` vs 코드 하드코딩)로 섞으면 운영 시점에 설정 꼬임이 생긴다.
4. JPA 엔티티 `@Data`는 LAZY 연관에서 `toString/equals/hashCode` 부작용을 만들 수 있어 실무에서 자주 지적된다.
5. `@WebMvcTest` 통과만으로는 도메인 일관성 보장이 부족하며, 서비스/통합 테스트가 반드시 필요하다.
6. GitHub 첫 화면에서 README/ERD/상태전이 설명이 없으면 코드 품질 대비 프로젝트 가치가 낮게 보일 수 있다.

### 수정 제안(적용/확정)
1. 우선순위 전환
- Listing 추가 기능보다 `Application/Reservation` 구현을 먼저 진행
2. 설정 정리 계획
- JWT 설정을 `@ConfigurationProperties`로 단일화
3. 엔티티 설계 정리 계획
- `@Data` -> `@Getter` 중심으로 점진 전환
4. 테스트 전략 강화
- 컨트롤러 테스트 유지 + 서비스/통합 테스트 추가
5. 문서화 강화
- README에 ERD/상태전이/핵심 트랜잭션/실행 방법 반영

### 나중에 퀴즈로 풀 문제 후보
1. 왜 이 프로젝트에서 가장 중요한 구현 순서는 `Application -> Reservation`인가?
2. JWT 설정을 코드와 yml에 이중으로 두면 어떤 장애가 날 수 있는가?
3. JPA 엔티티에서 `@Data`가 왜 위험할 수 있는가?
4. 컨트롤러 테스트와 서비스/통합 테스트의 역할 차이는?
5. 포트폴리오 README에 반드시 들어가야 할 5가지 항목은?

---

## 2026-03-15

### 주제
- 나중에 따로 공부할 필요가 있는 기반 역량 정리

### 추가 학습 필요 항목
1. ERD 설계
- 엔티티 관계를 도메인 흐름에 맞게 어떻게 끊는지
- 1:1 / 1:N / N:1 관계를 왜 그렇게 잡는지 설명하는 연습
- 상태 전이와 유니크 제약을 ERD/테이블 설계에 반영하는 방법

2. DB 관리 및 SQL
- Flyway 마이그레이션 버전 관리 방식
- 테이블 생성/수정 시 FK, UNIQUE, INDEX를 어떤 기준으로 두는지
- JPA 엔티티와 실제 SQL 스키마가 어떻게 대응되는지
- 운영 중 스키마 변경 시 롤포워드 방식으로 관리하는 방법

### 지금 공부하면 좋은 부분
- 정규화 기초와 실무적인 비정규화 기준
- 인덱스 설계 기초: 조회 조건, 정렬, 복합 인덱스
- SQL DDL 읽기/쓰기 연습: `CREATE TABLE`, `ALTER TABLE`, `CREATE INDEX`
- ERD를 보고 API/도메인 흐름으로 연결하는 연습

---

## 2026-03-15

### 주제
- 서비스 테스트 작성과 README 문서화 정리

### 내가 몰랐던 핵심 포인트
1. 서비스 테스트는 `@WebMvcTest`가 아니라 `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` + `@Mock` 조합으로 작성한다.
2. `given(...).willReturn(...)`은 실제 DB 조회를 대신하는 가짜 동작을 미리 정의하는 것이다.
3. `void` 메서드는 반환값이 아니라 `save()` 호출 여부나 저장된 객체 내용을 검증해야 한다.
4. `ArgumentCaptor`를 쓰면 `reservationRepository.save(...)`에 전달된 실제 엔티티 값을 꺼내 검증할 수 있다.
5. 서비스 테스트에서는 `message` 비교보다 `ErrorCode` 비교가 더 안정적이다.
6. 상태 전이 실패 테스트는 권한 실패가 먼저 나지 않도록 로그인 사용자를 올바르게 설정해야 한다.
7. README는 템플릿형 설명보다 현재 구현된 흐름과 규칙을 명확히 보여주는 서비스형 문서가 더 적합하다.
8. `.gitignore`에 `*.sql`이 있으면 Flyway migration까지 같이 무시될 수 있으므로 예외 처리 경로를 정확히 넣어야 한다.

### 수정 제안(적용)
1. `ApplicationServiceTest` 추가
- 성공/실패/권한/상태전이/예약 생성 호출 검증
2. `ReservationServiceTest` 추가
- 예약 생성, 상태 변경 성공, 권한 실패, 상태 전이 실패 검증
3. README 재작성
- 프로젝트 목표, 상태 전이, ERD, 예시 요청/응답, 테스트 실행 방법 반영
4. Flyway migration 경로 예외 처리
- `.gitignore`에서 `!src/main/resources/db/migration/*.sql` 추가

### 나중에 퀴즈로 풀 문제 후보
1. 서비스 테스트에서 `@InjectMocks`와 `@Mock`의 역할 차이는?
2. `given(...).willReturn(...)`이 정확히 무엇을 의미하는가?
3. `void` 메서드를 테스트할 때 `ArgumentCaptor`는 왜 필요한가?
4. 왜 서비스 테스트에서는 예외 메시지보다 `ErrorCode`를 검증하는 것이 더 나은가?
5. 상태 전이 실패 테스트에서 권한 실패가 먼저 발생하지 않게 하려면 무엇을 맞춰야 하는가?
6. `.gitignore`의 `*.sql` 규칙이 Flyway migration에 어떤 영향을 주는가?

---

## 2026-03-23

### 주제
- Application 조회 API 구현과 Reservation 조회 패턴 재사용

### 내가 정리한 포인트
1. `Application` 조회도 `Reservation` 조회와 비슷하게 `view / filter / sort / page / size` 구조로 맞추면 API 일관성이 좋아진다.
2. `Application`은 시간보다 처리 상태가 더 중요해서 필터를 `PENDING / PROCESSED`로 잡는 게 사용자 관점에 맞다.
3. `PENDING`은 `APPLIED`, `PROCESSED`는 `ACCEPTED / REJECTED / CANCELED`로 매핑하면 멘토/멘티 모두 자연스럽게 읽힌다.
4. 단건 상세 조회는 조건이 단순하면 `@Query + fetch join`으로도 충분히 괜찮고, 목록 조회처럼 동적 조건/정렬/페이징이 복잡한 경우에 Querydsl이 더 적합하다.
5. Querydsl custom repository에서는 멘토/멘티 public 메서드만 남기고 내부 공통 메서드로 content/count query를 합치면 중복을 줄이기 좋다.
6. `@RequestBody`와 `@ModelAttribute`는 쓰임이 다르며, 조회 API의 query parameter 바인딩은 `@ModelAttribute`가 맞다.

### 적용한 것
1. `GET /api/applications`
- `view=MENTOR|MENTEE`
- `filter=PENDING|PROCESSED`
- `sort=LATEST|OLDEST`
- paging 구조 반영
2. `GET /api/applications/{id}`
- 신청 당사자만 상세 조회 가능하도록 권한 체크 반영
3. `ApplicationDetailResponseDto`, `ApplicationSummaryResponseDto` 추가
4. `ApplicationRepositoryCustomImpl` 공통 쿼리 로직 추출
5. `ApplicationControllerTest`, `ApplicationServiceTest` 조회 테스트 추가 및 통과

### 다음 학습 포인트
1. 통합 테스트에서 mock 기반 서비스 테스트와 실제 DB 검증의 차이 체감하기
2. Querydsl fetch join / count query 분리 패턴을 Listing, Reservation, Application 세 곳에서 비교하기

---

## 2026-04-02

### 주제
- Reservation 핵심 통합 테스트 1차 완료

### 내가 정리한 포인트
1. `@SpringBootTest` 통합 테스트는 서비스 메서드를 직접 호출하고 repository 재조회로 DB 상태를 확인하는 방식이 현재 프로젝트 목적에 더 잘 맞는다.
2. `contextLoads()`에 시나리오를 넣는 것보다 별도 통합 테스트 클래스로 분리하는 편이 읽기 쉽고 유지보수에 유리하다.
3. Testcontainers 기반 테스트를 쓰려면 테스트 클래스에 설정을 연결하고, 테스트용 `application-test.yml`도 별도로 준비해야 한다.
4. `@CreatedDate`, `@LastModifiedDate` 같은 감사 컬럼이 있다면 auditing 설정 유무가 통합 테스트 실패 원인이 될 수 있다.
5. 통합 테스트에서는 로컬 객체 상태보다 repository 재조회 결과를 검증하는 편이 “실제 DB 반영”을 더 명확하게 보여준다.
6. given 데이터가 반복되면 fixture record + helper 메서드로 묶어 테스트 본문에 시나리오 차이만 남기는 게 좋다.

### 이번에 검증한 시나리오
1. `Application ACCEPTED -> Reservation 생성 -> Slot BOOKED`
2. `Reservation CANCELED -> Slot OPEN`
3. 취소 후 같은 슬롯 재예약 가능
4. 활성 예약 기준 동일 슬롯 중복 예약 실패

### 다음 학습 포인트
1. 동일 슬롯 동시 수락 경쟁 상황을 통합 테스트로 어떻게 재현할지
2. `flush/clear`를 언제 추가해야 검증 강도가 더 올라가는지
