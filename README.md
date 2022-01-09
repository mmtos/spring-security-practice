## 예제 URL
- https://spring.io/guides/gs/securing-web/
- https://thecodinglog.github.io/spring/security/2018/05/25/spring-security-2.html
- 레퍼런스
  - https://spring.io/guides/topicals/spring-security-architecture/
  - https://docs.spring.io/spring-security/site/docs/5.0.5.RELEASE/reference/htmlsingle/

## SpringBoot Configuration
#### @Enable Config
- 미리 정의된 설정 파일. 
- https://javacan.tistory.com/entry/spring-at-enable-config
- @Configuration javadoc 
  - Enabling built-in Spring features using @Enable annotations
  - Spring features such as asynchronous method execution, scheduled task execution, annotation driven transaction management, and even Spring MVC can be enabled and configured from @Configuration classes using their respective "@Enable" annotations. See @EnableAsync, @EnableScheduling, @EnableTransactionManagement, @EnableAspectJAutoProxy, and @EnableWebMvc for details.

#### @EnableWebSecurity 살펴보기 
- WebSecurityConfiguration
  - WebSecurity 생성 : setFilterChainProxySecurityConfigurer()
  - FilterChain 생성 : springSecurityFilterChain()
  - 커스터마이징 방법 : Customizations can be made to WebSecurity by extending WebSecurityConfigurerAdapter and exposing it as a Configuration or implementing WebSecurityConfigurer and exposing it as a Configuration.
  
- SpringWebMvcImportSelector
  - 클래스로더 내에 DispatcherServlet있는 경우만 WebMvcSecurityConfiguration 수행
    - csrf, auth principal, security context resolver 등록 : addArgumentResolvers()
    - csrf 관련 처리 : requestDataValueProcessor
- OAuth2ImportSelector (생략)
- HttpSecurityConfiguration
  - httpSecurity default 설정하는 부분.
- EnableGlobalAuthentication
  - @Import({AuthenticationConfiguration.class})

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