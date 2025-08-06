# Server-Backend

---


# 목차

[프로젝트 소개 및 배경](#프로젝트-소개-및-배경) </br>
[개발 스택](#개발-스택) </br>
[주요 기능](#주요-기능) </br>
[Contributor](#Contributor) </br>

</br>

# 프로젝트 소개 및 배경

팜포유는 **농업 분야 일손 구하기 및 구직 플랫폼**으로, 농업 현장의 일자리 문제를 디지털과 AI 기술로 해결하는 종합 플랫폼입니다.

농촌 고령화로 인해 인력 수요는 지속적으로 증가하고 있지만, 실제 필요한 정보를 접하거나 적절한 인력을 매칭하는 데 있어 현실적인 한계가 존재합니다. 위 문제의식에서 출발하여 **AI 기반 농촌 일자리 매칭 플랫폼**이라는 기존에 없던 AI 기반 농촌 인력 혁신 모델 서비스를 제공하고자 합니다.

</br>

# 개발 스택

| 파트     | 기술                                                          |
| -------- | ------------------------------------------------------------- |
| BackEnd  | Spring Boot 3.x, JWT, JPA, MySQL 8.x, Redis                   |
| FrontEnd | React, JavaScript, Vite                                       |
| AI       | Python, FastAPI, scikit-learn, LightGBM, Pandas               |
| 기타     | AWS EC2, RDS, Figma, Git, 카카오 지도 API, Swagger, Talend 등 |

<details>
  <summary>DB 보기</summary>

    ## 1. users (회원 기본 정보)

    - **모든 회원의 공통 정보**(전화번호, 현재 역할 등)를 관리합니다.
    - 역할: WORKER(근로자), FARMER(농가), ADMIN(관리자), ANONYMOUS(비회원).

    ## 2. workers (구직자/근로자 정보)

    - 회원 테이블과 1:1 매핑.
    - **근로자 프로필**(성명, 성별, 생년월일, 거주 및 활동지역, 농업 경험, 희망 근무 조건 등)이 저장됩니다.
    - 농가 평가(평점, 재고용률 등) 및 신뢰 점수 관리.

    ## 3. farmers (농가/고용주 정보)

    - 회원 테이블과 1:1 매핑.
    - **농가 정보**(닉네임, 사업자등록번호, 농장명, 위치, 설명 등)와 근로자의 평가(평점, 재고용률 등)가 저장됩니다.

    ## 4. job_postings (공고)

    - 농가가 등록한 **일자리 공고**(작업 내용, 위치, 면적, 임금, 모집 인원, 채용 조건 등)를 관리합니다.

    ## 5. applications (지원 내역)

    - 근로자의 **공고 지원 내역**을 기록하고, 상태(지원, 매칭, 거절, 취소 등)를 추적합니다.

    ## 6. experiences (작업 이력)

    - 근로자별 **실제 근무 이력**(공고 정보 스냅샷, 임금, 기간 등)을 저장합니다.

    ## 7. likes (관심 공고)

    - 근로자가 관심 있는 **공고를 찜(좋아요)** 하면 기록됩니다. 1명당 1공고에 1회만 등록 가능.

    ## 8. notifications (알림)

    - 각종 **서비스 내 알림**(매칭, 지원, 마감 등)을 기록, 관리합니다.

    ## 9. reviews (후기)

    - 근로자-농가 상호 **후기 및 평가**가 남겨집니다.
        - 농가→근로자: 성실, 약속 이행, 업무 역량, 재고용 의사 등
        - 근로자→농가: 소통, 작업 환경, 안내 명확성, 보상 등

    ## 10. reports (신고)

    - 서비스 내 **신고 내역**을 기록하며, 신고 사유 및 처리 상태를 관리합니다.

    ## 11. badges & user_badges (뱃지 및 매핑)

    - **활동 내역, 성과에 따른 뱃지**를 정의하고, 각 회원에게 달성 여부를 저장합니다.

</details>
   
<details> <summary>API 문서 요약 보기</summary>
    </br>

    > 본 요약은 리드미에 넣기에 적합한 핵심 API 루트만 담았습니다.
    >

    ## 인증 (Auth)

    - **인증번호 요청**

        **`POST /auth/request-code`**

        `json{ "phoneNumber": "01012345678" }`

    - **인증번호 확인 및 로그인/회원가입**

        **`POST /auth/verify-code`**

        `json{ "phoneNumber": "01012345678", "code": "000000", "mode": "FARMER" }`

        → JWT 토큰 반환

    - **로그아웃**

        **`POST /auth/logout`** (Authorization: Bearer {jwt})

    ## 회원 (Users)

    - **모드 전환(구직↔농가)**

        **`PATCH /users/me/mode`**

        `json{ "mode": "FARMER" }`

    - **특정 유저 정보 조회(Admin)**

        **`GET /users/{id}`**

    ## 농가 (Farmers)

    - **농가 등록**

        **`POST /farmers`**

        (Authorization 필요)

    - **농가 정보 수정/조회**

        **`PATCH /farmers`**, **`GET /farmers`**

    - **특정 농가 조회**

        **`GET /farmers/{id}`**

    - **농가 리스트 조회(Admin)**

        **`GET /farmers/list`**

    ## 구직자 (Workers)

    - **구직자 등록**

        **`POST /workers`**

    - **정보 수정/조회**

        **`PATCH /workers`**, **`GET /workers`**

    - **특정 구직자 조회(Admin)**

        **`GET /workers/{id}`**

    - **구직자 리스트(Admin)**

        **`GET /workers/list`**

    ## 공고 (Jobs)

    - **공고 자동 생성 (음성 인식)**

        **`POST /jobs/auto-write`** (multipart/form-data)

    - **공고 등록/수정/조회/삭제**

        **`POST /jobs`**, **`PUT /jobs/{id}`**, **`GET /jobs/{id}`**, **`DELETE /jobs/{id}`**

    - **공고 마감**

        **`PATCH /jobs/{id}/close`**

    - **공고 리스트 조회**

        **`GET /jobs`**

    ## 지원/매칭 (Applications)

    - **지원하기**

        **`POST /applications`**

        `json{ "jobId": 5 }`

    - **지원 취소**

        **`PATCH /applications/{id}/cancel`**

    - **내 지원 리스트/상태별 필터링**

        **`GET /applications`**, **`GET /applications?status=matched`**

    - **공고별 지원자 확인(Farmer)**

        **`GET /jobs/{id}/applicants`**

    ## 작업 이력 (Experiences)

    - **작업 이력 리스트/상세**

        **`GET /experiences`**, **`GET /experiences/{id}`**

    ## 관심 공고 (Likes)

    - **관심 등록/해제**

        **`POST /likes/{jobId}`**, **`DELETE /likes/{jobId}`**

    - **내 관심 공고 리스트**

        **`GET /likes`**

    ## 알림 (Notifications)

    - **알림 리스트**

        **`GET /notifications`**

    - **읽음 처리**

        **`PATCH /notifications/{id}/read`**, **`POST /notifications/read`**

    ## 후기 (Reviews)

    - **후기 등록/수정/삭제/조회**

        **`POST /reviews`**, **`PUT /reviews/{id}`**, **`DELETE /reviews/{id}`**, **`GET /reviews/{id}`**

    - **특정 농가/구직자의 후기 목록**

        **`GET /reviews/farmer/{farmerUserId}`**

        **`GET /reviews/worker/{workerUserId}`**

    ## 신고 (Reports)

    - **신고 등록**

        **`POST /reports`**

    - **신고 리스트(Admin)**

        **`GET /reports`**

    ## 뱃지 (Badges)

    - **특정 유저의 뱃지 조회**

        **`GET /users/badges`**

    - **뱃지 전체/상세 조회**

        **`GET /badges`**, **`GET /badges/{id}`**

</details>

</br>

# 주요 기능

1. 회원가입 및 사용자 유형 설정 (농가/구직자)과 로그인
2. 일자리 공고 등록
3. AI 추천 기반 일자리 매칭 시스템
4. 조건 기반 직접 탐색
5. 지도 기반 시각화 탐색
6. 후기 및 신뢰도 기반 평가 시스템

### 앱 화면
- 구직자 화면 예시 </br>
![Image](https://github.com/user-attachments/assets/8e85f6bc-4d4b-4204-a72e-18d3cb752158)

- 농가 화면 예시 </br>
![Image](https://github.com/user-attachments/assets/8d02eb02-83b9-4a46-a244-ba9317aea52b)

- 관리자 화면 예시 </br>
<img width="934" height="494" alt="Image" src="https://github.com/user-attachments/assets/17a996b9-41cc-4811-9e37-76f1056d30e4" />

</br>
</br>


# Contributor

| PM         | 기획 및 디자인 | AI         | BackEnd    | FrontEnd               |
| ---------- | -------------- | ---------- | ---------- | ---------------------- |
| [baewonje](https://github.com/baewonje) | [noeytwi06](https://github.com/noeytwi06)     | [soheean1370](https://github.com/soheean1370) | [meraki6512](https://github.com/meraki6512) | [dajung-y](https://github.com/dajung-y), [ming0o](https://github.com/ming0o) |

