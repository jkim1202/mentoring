# Sprint 1 Handoff (2026-03-01)

## 목적
- 다음 세션에서 바로 재개할 수 있도록, 오늘 구현 완료 범위와 내일 우선 작업을 고정한다.

## 오늘 완료된 내용
- 기반 세팅
  - MySQL Docker 기동 완료 (`healthy`)
  - Flyway `V1__init_users.sql` 적용
  - `.env` + `application.yml`/`application-local.yml` 매핑 유지
- 의존성
  - `jjwt` 추가/사용 중
  - OpenAPI 의존성 반영
  - 취약점 대응: `commons-lang3`를 `3.18.0`으로 고정
- 공통 예외 처리
  - `ErrorCode` 구현 (`AUTH_*`, `COMMON_*`, `USER_*`)
  - `BusinessException` 구현
  - `GlobalExceptionHandler` 구현
    - `BusinessException`
    - `MethodArgumentNotValidException`
    - `Exception` fallback
- Security/JWT 초안 구현
  - `JwtTokenProvider`
    - access/refresh secret 분리
    - access/refresh 만료시간 분리
    - 토큰 생성/검증/클레임 조회
    - 토큰 email과 `UserDetails.username(email)` 교차 검증
  - `JwtAuthenticationFilter`
    - Bearer 토큰 추출
    - 사용자 조회 후 `SecurityContext` 인증 세팅
  - `JwtAuthenticationEntryPoint`
    - 401 JSON 응답(`status`, `code`, `message`)
  - `SecurityConfig`
    - stateless + JWT 필터 등록
    - 경로 권한 규칙 초안 반영
- 사용자 인증 모델 정리
  - `MentoringUserDetails`를 email 중심으로 정리
  - `getUsername()`은 email 반환
  - `isEnabled()`는 `UserStatus.ACTIVE` 기준
  - `MentoringUserDetailsService` 매핑 반영
- Auth API 구현
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
  - 로그인 실패 예외 매핑(`AuthenticationException -> AUTH_LOGIN_FAILED`) 반영
- 도메인 구조 정리
  - `auth/*`, `user/*` 패키지 분리
- DB/마이그레이션 정리
  - `V2__create_user_roles.sql` 추가
  - `user_roles` 테이블 반영
- 검증
  - `./gradlew --no-daemon compileJava` 통과
  - `./gradlew --no-daemon test` 통과

## 현재 남은 이슈/리스크
1. JWT 에러코드 세분화 정책
- 필터/EntryPoint에서 만료(`AUTH_EXPIRED_TOKEN`)와 기타(`AUTH_INVALID_TOKEN`) 분리 응답 유지 여부

2. 로그아웃 정책 미구현
- Redis 기반 refresh 토큰 무효화는 추후 구현 예정

## 다음 작업 (우선순위)
1. Auth 테스트 고도화
- 서비스 단위 테스트 추가(register/login/refresh 실패 케이스 확대)

2. 사용자/프로필 정책 확정
- 닉네임 입력 시점(회원가입 즉시 vs 프로필 완성 단계) 고정

3. 기능 확장 준비
- listing/reservation 도메인 구현 시작 전 공통 응답/에러 포맷 재점검

## 참고 실행 명령
```bash
docker compose up -d
./gradlew --no-daemon compileJava
./gradlew --no-daemon test
```

---

# 작업 업데이트 (2026-03-04)

## 오늘 완료된 내용
- Listing API 3종 구현 점검 완료
  - `GET /api/listings`
  - `GET /api/listings/{id}`
  - `PATCH /api/listings/{id}`
- 수정 API를 `POST` -> `PATCH`로 정정
- 수정 권한 체크 버그 수정
  - 잘못된 비교(`listing.id == loginId`) -> 작성자 비교(`listing.mentor.id == loginId`)
- DTO 오타 정리
  - `reviewCont` -> `reviewCount`
- 컨트롤러 테스트 추가
  - `ListingControllerTest` 4개 케이스 통과

## 다음 세션 시작점
1. 검색 조건(topic/placeType/minPrice/maxPrice) JPA 쿼리 반영
2. Listing 생성 API 구현(`POST /api/listings`)
3. Listing 상태 변경 API 분리(`PATCH /api/listings/{id}/status`)

---

# 작업 업데이트 (2026-03-06)

## 오늘 완료된 내용
- Querydsl 검색 구현
  - `ListingRepositoryCustom`/`Impl` 추가
  - topic/placeType/price 조건 + pageable 정렬 연동
- Listing 생성 API 구현
  - `ListingCreateRequestDto` 추가
  - `POST /api/listings` 구현 및 `201 Created` 응답
  - `OFFLINE` + `placeDesc` 필수 비즈니스 검증 반영
- Listing 상태 변경 API 구현
  - `ListingStatusUpdateRequestDto` 추가
  - `PATCH /api/listings/{id}/status` 구현
  - `ListingStatus.canChangeTo` + `Listing.setStatus`로 전이 규칙 캡슐화
  - `ErrorCode.LISTING_INVALID_STATUS_TRANSITION` 추가
- 테스트
  - `ListingControllerTest` 케이스 보강 및 통과 확인
- 협업 방식
  - `feat/listing-create` 브랜치 생성
  - PR 방식으로 전환(브랜치 push 완료)

## 현재 진행률(체감)
- Sprint 2 기준 약 30~40% 완료
  - Listing 도메인 핵심 API는 구현됨
  - Application/Reservation 본체가 남아있음

## 2시간 기준 다음 세션 TODO
1. `feat/*` 브랜치에서 PR 생성/병합/브랜치 정리
2. 상태 변경 API 실패 케이스(권한/전이 불가) 테스트 보강
3. Application/Reservation 엔티티/상태전이 초안 시작

---

# 작업 업데이트 (2026-03-07)

## 리포 리뷰 반영 결과
- `mentoring-repo-review.md`를 로컬 문서로만 유지
  - `.gitignore`에 `mentoring-repo-review.md` 추가
  - 현재 파일은 원래 미추적 상태라 `rm --cached` 조치 불필요
- 계획 방향 조정
  - Listing 확장보다 핵심 도메인(`Application -> Reservation`) 우선으로 전환
  - 평가 포인트를 “기능 수”에서 “상태전이 + 중복방지 + 테스트 + 문서화”로 고정

## 다음 작업 우선순위(확정)
1. Application 생성/조회 + 멘토 수락/거절 구현
2. 수락 시 Reservation 생성 + slot 중복 예약 방지 검증
3. Reservation 상태 전이(`PENDING_PAYMENT -> CONFIRMED -> COMPLETED`, 취소)
4. 서비스/통합 테스트 보강(동시성/권한/상태 실패 케이스)
5. README 보강(ERD/상태전이/핵심 트랜잭션/실행 방법)

---

# 작업 업데이트 (2026-03-15)

## 오늘 완료된 내용
- `ApplicationServiceTest` 작성 및 통과
  - 신청 생성 성공
  - 중복 신청 실패
  - slot-listing 불일치 실패
  - `BOOKED` 슬롯 실패
  - 신청 수락 성공 + 예약 생성 호출
  - 권한 실패
  - 상태 전이 실패
  - 거절 성공 + 예약 미생성 검증
- `ReservationServiceTest` 작성 및 통과
  - 예약 생성 성공
  - 예약 상태 변경 성공
  - 예약 당사자 아님 실패
  - 예약 잘못된 상태 전이 실패
- `README.md` 재작성
  - 핵심 도메인 흐름 정리
  - 상태 전이 정리
  - Flyway 마이그레이션 정리
  - ERD 추가
  - 예시 요청/응답 추가
- Flyway 마이그레이션 추가
  - `V7__create_applications.sql`
  - `V8__create_reservations.sql`
- `.gitignore` 수정
  - 일반 `*.sql` 무시는 유지
  - Flyway migration 경로는 예외 처리

## 현재 브랜치/PR 상태
- 작업 브랜치: `feat/listing-create`
- 최근 커밋
  - `b062a66` `test(service): 신청 및 예약 서비스 테스트 추가`
  - `54895ee` `docs(readme): ERD 및 API 예시 정리`
- PR 링크
  - `https://github.com/jkim1202/mentoring/pull/new/feat/listing-create`

## 현재 체감 진행률
- 전체 프로젝트 기준 약 45% 전후
- 이유
  - auth / listing / application / reservation 핵심 흐름은 구현됨
  - 조회 API 확장, 통합 테스트, 정책 구체화, 채팅/리뷰는 남아 있음

## 다음 세션 시작점
1. PR self review 및 merge
2. Flyway 실제 적용 전 최종 점검
3. Reservation 조회 API 설계 시작
4. README 과장 문구 여부 최종 정리
5. DB/ERD 학습 TODO 시작

---

# 작업 업데이트 (2026-03-16)

## 오늘 완료된 내용
- PR #10 머지 완료
  - `refactor/application-reservation-consistency`
  - 머지 후 원격 브랜치 삭제 완료
- Reservation/Slot 상태 일관성 정리
  - `Application ACCEPTED -> Reservation 생성 -> Slot BOOKED` 흐름 정리
  - `Slot.book()` / `Slot.reopen()` 도입
  - 예약 취소 시 슬롯 `OPEN` 복귀 반영
- 동시성/정합성 보강
  - `SlotRepository.findByIdForUpdate(...)` 추가
  - 예약 생성 시 슬롯 비관적 락 적용
  - `existsBySlotIdAndStatusIn(...)`로 활성 예약 사전 체크 반영
- DB 제약 재설계
  - `slot_id UNIQUE` 제거 방향 확정
  - `V9__add_active_slot_unique_constraint.sql` 추가
  - `active_slot_id` generated column + unique 제약으로 활성 예약만 중복 방지
- JWT 설정 정리
  - `JwtProperties` 추가
  - `JwtTokenProvider`가 `security.jwt.*` 설정만 사용하도록 통일
- 문서 반영
  - README 상태 전이/트러블슈팅 요약 업데이트
  - `troubleshooting-log.md` 추가 및 오늘 사례 기록
- 테스트 보강 및 검증
  - `ApplicationServiceTest`
    - 동일 슬롯 수락 실패 전파 케이스 추가
  - `ReservationServiceTest`
    - 예약 취소 후 재예약 성공
    - active 예약 중복 생성 실패
  - `ReservationControllerTest`
    - `slotStatus` 응답 반영
  - 실행 검증
    - `./gradlew --no-daemon test --tests org.example.mentoring.application.service.ApplicationServiceTest --tests org.example.mentoring.reservation.service.ReservationServiceTest` 통과

## 현재 체감 진행률
- 전체 프로젝트 기준 약 70% 전후
- 이유
  - auth / listing / application / reservation 핵심 흐름과 상태 일관성 정리가 완료됨
  - 예약 조회 API
    - 목록 조회 paging/필터/정렬 구현 완료
    - 상세 조회 구현 완료
  - 신청 조회 API
    - 목록 조회 paging/필터/정렬 구현 완료
    - 상세 조회 구현 완료
  - Reservation 핵심 통합 테스트 4개 시나리오 검증 완료
  - 정책 세부화, 리뷰/메시지 기능은 남아 있음

## 다음 세션 시작점
1. 로컬 `main` 최신화
2. 새 브랜치 `test/application-reservation-integration` 생성
3. 실제 DB 기준 통합 테스트 진행 결과 정리
- `Application ACCEPTED -> Reservation 생성 -> Slot BOOKED` 통과
- `Reservation CANCELED -> Slot OPEN` 통과
- 취소 후 같은 슬롯 재예약 가능 통과
- 활성 예약 기준 동일 슬롯 중복 예약 실패 통과
4. 추가 통합 테스트 검토
- 동일 슬롯 동시 수락 경쟁 상황
- 필요 시 `flush/clear` 기반 검증 강화
5. 통합 테스트 이후 우선순위
- 리뷰 도메인 구현
- 채팅은 Reservation 기반 1:1 REST 메시지 기능부터 시작하고, WebSocket/STOMP는 후속 확장으로 분리
