## 예제 URL
- https://spring.io/guides/gs/securing-web/
- https://thecodinglog.github.io/spring/security/2018/05/25/spring-security-2.html
- 레퍼런스
  - https://spring.io/guides/topicals/spring-security-architecture/
  - https://docs.spring.io/spring-security/site/docs/5.0.5.RELEASE/reference/htmlsingle/

- custom table 사용하기 : https://www.baeldung.com/spring-security-authentication-with-a-database
- 

## TODO 
- [X] Spring Security 기본 테이블이 아닌 custom 테이블 사용하기 (JPA에서 생성..)
- [X] 회원가입 구현
- [ ] JWT 도입

## JPA
### Entity
- @Id : 직접 할당
- @Id @GeneratedValue : 자동생성 
- https://gmlwjd9405.github.io/2019/08/12/primary-key-mapping.html

- Setter없는 Entity : https://velog.io/@aidenshin/%EB%82%B4%EA%B0%80-%EC%83%9D%EA%B0%81%ED%95%98%EB%8A%94-JPA-%EC%97%94%ED%8B%B0%ED%8B%B0-%EC%9E%91%EC%84%B1-%EC%9B%90%EC%B9%99
- 
### table 생성 설정
- spring boot의 경우 spring.jpa로 시작하는 설정프로퍼티를 통해 설정 가능함
- spring.jpa.hibernate.ddl-auto: create
- spring.jpa.show-sql: true
- https://m.blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=rorean&logNo=221587154921

### save & saveandflush 
- save의 경우 persistance context에만 저장하고 추후 한번에 DB로 저장됨. 
- saveandflush의 경우 명시적으로 persistance context에서 DB로 insert, update 쿼리를 보내게됨.



## UserDetails
### 디폴트 구현체 
- org.springframework.security.core.userdetails.User 
### getAuthorities : 
- GrantedAuthority타입의 컬렉션을 반환. return되는 Collection은 null을 포함해서는 안됨.  null을 리턴해서도 안됨.
- SimpleGrantedAuthority, 
#### Authorities 
- SimpleGrantedAuthority (기본)

## userDetailsPasswordService ? 
- 1. 인증 성공후 DaoAuthenticationProvider.createSuccessAuthentication에서 리턴할 Authentication에 들어있는 user password를 변경하는데 쓰임.
- 

## 유용한 디버깅 중단점
- Spring Security 시작점 : DelegatingFilterProxy.doFilter
- SecurityContextPersistenceFilter : 인증시작전에 SecurityContext를 생성, 인증 절차 종료후 clear()
- MyUserDetailService가 제대로 등록되었는지 확인 : AuthenticationManagerBuilder.performBuild에 중단점
  - Spring security가 기본 제공하는 DaoAuthenticationProvider의 멤버로 MyUserDetailService가 등록되어 있다. 
  - 넘기다 보니 AnonymousAuthenticationProvider도 같이 등록되는 걸 확인.
- 인증 시작점 확인 : ProviderManager.authenticate
- UsernamePasswordAuthenticationToken에 대한 인증 시작점 확인 : AbstractUserDetailsAuthenticationProvider.authenticate
  - 토큰 생성 지점 : UsernamePasswordAuthenticationFilter.attemptAuthentication (필터 체인 중 하나 )
  - userDetails 찾은 후 추가 체크 : 
    - preAuthenticationChecks.check(userDetail)
    - DefaultPreAuthenticationChecks - (계정 Lock여부, 계정 만료여부 등.)
    - postAuthenticationChecks.check 수행
    - DefaultPostAuthenticationChecks - User credentials 만료 여부 확인
  - 체크 끝나면 캐시(this.userCache.putUserInCache(user);)
  - createSuccessAuthentication
- 성공 Auth token에서 eraseCredentials 수행
- 
- 인증 오류 처리 지점 : DefaultAuthenticationEventPublisher.publishAuthenticationFailure
  - org.springframework.context.event 에서 처리가 위임됨 
## SpringBoot Configuration
#### @Enable Config
- 미리 정의된 설정 파일. 
- https://javacan.tistory.com/entry/spring-at-enable-config
- @Configuration javadoc 
  - Enabling built-in Spring features using @Enable annotations
  - Spring features such as asynchronous method execution, scheduled task execution, annotation driven transaction management, and even Spring MVC can be enabled and configured from @Configuration classes using their respective "@Enable" annotations. See @EnableAsync, @EnableScheduling, @EnableTransactionManagement, @EnableAspectJAutoProxy, and @EnableWebMvc for details.

#### @Configuration(proxyBeanMethods = false)
```
* javadoc 전문
Specify whether @Bean methods should get proxied in order to enforce bean lifecycle behavior, e.g. to return shared singleton bean instances even in case of direct @Bean method calls in user code. This feature requires method interception, implemented through a runtime-generated CGLIB subclass which comes with limitations such as the configuration class and its methods not being allowed to declare final.
The default is true, allowing for 'inter-bean references' via direct method calls within the configuration class as well as for external calls to this configuration's @Bean methods, e.g. from another configuration class. If this is not needed since each of this particular configuration's @Bean methods is self-contained and designed as a plain factory method for container use, switch this flag to false in order to avoid CGLIB subclass processing.
Turning off bean method interception effectively processes @Bean methods individually like when declared on non-@Configuration classes, a.k.a. "@Bean Lite Mode" (see @Bean's javadoc). It is therefore behaviorally equivalent to removing the @Configuration stereotype.
Since:5.2
```
- 기본값은 true 이며, 이 경우 configuration 클래스 내부에서 @Bean이 붙은 메서드를 직접 호출하거나
다른 configuration 클래스에서 (즉 외부에서) 호출했을때 동일한 '내부-빈'을 참조하도록 허용한다.
- 이는 CGLIB subclassing방식의 method interception으로 구현된다. 따라서 subclassing이 가능해야하므로 해당 configuration의 class나 method에 final을 붙이지 못한다.
- singleton bean이여야 하는 경우 위의 방식을 적용하면 @Bean 메서드 호출을 여러번 해도 단 한번의 bean 생성만 보장할 수 있다.  
- configuration class 내 @Bean메서드가 self contained하거나, @Bean메서드를 단순 factory method로 사용할 목적이면 proxyBeanMethods 값을  false로 둬도 상관없다.
- false인 경우 CGLIB subclassing을 생략하며 @Configuration을 뺀 단순 POJO처럼 동작하게된다. 

#### AutoConfiguration 제대로 알고 사용하기.
https://tecoble.techcourse.co.kr/post/2021-10-14-springboot-autoconfiguration/