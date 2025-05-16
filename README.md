# 프로젝트 클린아키텍쳐로 리펙토링

## 도메인 정리
- Cake
- CakeLike
- CakeMenu
- Comment
- Design
- FixNoti
- Noti
- OpenTime
- OrderForm
- PersonalNoti
- Post
- PostLike
- Review
- ReviewImg
- SearchKeyword
- Store
- StoreLike
- StoreOption
- StoreUrl
- Timestamped
- User
- UserOrders

도메인 끼리 묶기
- 게시판
  - Post
  - PostLike
  - Comment
- 상점
  - Store
  - StoreLike
  - StoreOption
  - StoreUrl
  - OpenTime
  - SearchKeyword
- 리뷰
  - Review
  - ReviewImg
- 상품
  - Cake
  - CakeLike
  - CakeMenu
- 알림
  - Noti
  - FixNoti
  - PersonalNoti
- 주문
  - Design
  - OrderForm
  - UserOrders
- 사용자
  - User
- 공통
  - Timestamped

수정
1. model 패키지명 -> domain
2. domain 패키지 내부 도메인별 분류