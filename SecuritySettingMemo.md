# String Security 설정파일 작성방법

## 1. 자바 설정파일 생성
  - WebSecurityConfigurerAdapter 서브 클래스 작성
  - @EnableWebSecurity 어노테이션 붙이기
  - ```java
    @EnableWebSecurity
    public class MySecurityConfig extends WebSecurityConfigurerAdapter {
    }
    ```

## 2. 설정 Method Overriding
  - EnableWebSecurity 의 source를 보면 예시코드를 얻을 수 있다.
  - ```java
    @Configuration
    @EnableWebSecurity
    public class MyWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    
        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring()
            // Spring Security should completely ignore URLs starting with /resources/
                    .antMatchers("/resources/**");
        }
      
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().antMatchers("/public/**").permitAll().anyRequest()
                    .hasRole("USER").and()
                    // Possibly more configuration ...
                    .formLogin() // enable form based log in
                    // set permitAll for all URLs associated with Form Login
                    .permitAll();
        }
      
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth
            // enable in memory based authentication with a user named "user" and "admin"
            .inMemoryAuthentication().withUser("user").password("password").roles("USER")
                    .and().withUser("admin").password("password").roles("USER", "ADMIN");
        }
      
        // Possibly more overridden methods ...
    }
    ```
  - 총 3가지의 overloading된 configure메서드를 이용해서 Security 설정을 할 수 있다.
    - configure(WebSecurity web) : WebSecurity를 설정한다.
    - configure(HttpSecurity http) : HttpSecurity를 설정한다.
    - configure(AuthenticationManagerBuilder auth)를 설정한다. 
  - 3가지 메서드로 설정할 수 있는 것들을 살펴보자

## 3-1 WebSecurity
### WebSecurity는 AbstractConfiguredSecurityBuilder<Filter, WebSecurity>를 extends하고 있다.
  - AbstractConfiguredSecurityBuilder<O,B> 소스 주석에 따르면 O는 빌드 결과물의 타입이며, B는 빌더의 타입이다.
  - WebSecurity는 **Filter를 Build하기위한 Builder**라고 볼 수 있다.
  - 처음에 작성한 WebSecurityConfigurerAdapter의 서브클래스는 WebSecurity라는 SecurityBuilder를 설정하는 역할을 한다. 
    - Spring Security에서는 이러한 역할을 SecurityConfigurer로 정의하고 있다. 
    - SecurityConfigurer 소스를 보면 자신이 설정하고자 하는 SecurityBuilder를 init하거나 configure하는 메서드를 가지고 있다. 
    - 모든 SecurityConfigurer의 init이 호출된 후에 각각의 configure가 호출된다. (After all init(SecurityBuilder) methods have been invoked, each configure(SecurityBuilder) method is invoked.)
    - init 메서드에서는 SecurityBuilder의 properties는 수정할 수 없으며 단지 SecurityBuilder들이 공유할 sharedObject를 생성 및 수정할 수 있다.
    - configure 메서드에서는 SecurityBuilder의 properties를 수정 할 수 있다. 
    - -> Spring Security를 설정하기 위해서 왜 configure 재정의하는지 알 수 있다. 
  - AbstractSecurityBuilder는 Build목표 객체를 단 한번만 빌드 할 수 있도록 보장한다. 
    - 소스: the object being built is only built one time.
  - 결론적으로 WebSecurity는 Spring Security 적용의 시작점이 될 FilterChainProxy를 딱 한번 빌드하는 역할을 맡고 있다.

### WebSecurity.performBuild()
  - 실질적으로 FilterChainProxy를 빌드하는 곳이다.
    1. ignoredRequests으로 filterChain을 생성한다.
    2. HttpSecurity으로 filterChain을 생성한다. 
       - 이 filterchain에 포함된 filter들을 보려면 WebSecurityConfigurerAdapter의 applyDefaultConfiguration메서드를 참고하자.
       - 만일 WebSecurityConfigurerAdapter의 서브클래스를 만들지 않았거나 @EnableWebSecurity를 붙이지 않았다면, HttpSecurityConfiguration에 있는 httpSecurity() 빈이 사용된다.
    3. filterChainProxy를 생성하고 위의 filterChain들을 주입한다.
    4. filterChainProxy에 firewall을 등록한다. 
    5. filterChainProxy에 requestRejectedHandler를 등록한다.
       - RequestRejectedException을 핸들링하는 역할을 한다. 요청자에게 요청실패 메시지를 전달 할 수 있다. 
    6. postBuildAction을 실행한다. (WebSecurityConfigurerAdapter의 init을 보면 postBuildAction을 통해 FilterSecurityInterceptor를 WebSecurity에 넣어주고 있다.)
    7. filterChainProxy를 return한다. 
       - 만일 WebSecurityConfigurerAdapter에 @EnableWebSecurity(true)로 명시되어있다면 debug용 필터를 리턴한다.

### 설정용 메서드
  1. web.ignoring()
     - Spring Security를 적용하지 않을 경로를 지정한다.
     - antMatchers나 mvcMatchers를 이용한다. 
     - antMatchers vs mvcMatchers : https://stackoverflow.com/questions/50536292/difference-between-antmatcher-and-mvcmatcher
  2. web.debug(true)
     - 설정시 Spring Security를 debug할 수 있다. (Production에서는 절대 사용하지 말것)
  3. web.expressionHandler()
     - expressionHandler를 설정 할 수 있다. 
     - 설정 하지 않으면 DefaultWebSecurityExpressionHandler를 사용한다. 
     - DefaultWebSecurityExpressionHandler에서는 Role prefix와 AuthenticationTrustResolver를 이용해 SecurityExpressionRoot를 생성한다.  
     - AuthenticationTrustResolver
       - AuthenticationTrustResolver의 디폴트 구현체는 AuthenticationTrustResolverImpl이다. 
       - AuthenticationTrustResolver의 인터페이스 : isAnonymous, isRememberMe
       - Authentication token을 평가하는 역할을 맡고 있다. Makes trust decisions based on whether the passed Authentication is an instance of a defined class.
  4. web.httpFirewall()
     - 필요한 경우 직접만든 HttpFireWall을 사용할 수 있다. (performBuild() 4번 과정)
     - HttpFireWall은 request를 filterchain으로 넘기기 전에 체크하는 역할을 한다. 문제가 있다면 RequestRejectedException를 throw한다.
     - 또한 filterchain으로 응답을 넘겨주기전에 응답의 행동을 제한하기 위한 방법을 제공한다.
     - 기본 구현체는 StrictHttpFirewall이며 DefaultHttpFirewall을 대신 사용할 수 있다.
  5. web.addSecurityFilterChainBuilder()
     - 필요한 경우 직접만든 filterChain을 추가 할 수 있다. (ignore,http 외의 filterchain. performBuild() 3번 과정)
  6. web.postBuildAction(), web.privilegeEvaluator(), web.securityInterceptor()
     - 생략

## 3-2 HttpSecurity
### HttpSecurity extends AbstractConfiguredSecurityBuilder<DefaultSecurityFilterChain, HttpSecurity>
  - HttpSecurity는 DefaultSecurityFilterChain를 build한다.
  - HttpSecurity의 performBuild() 참고 

### HttpSecurity 살펴보기
  1. HttpSecurity implements HttpSecurityBuilder<HttpSecurity>
     - HttpSecurity는 자기자신을 빌드할 수 있는 HttpSecurityBuilder이기도 하다.
  2. HttpSecurityBuilder<H extends HttpSecurityBuilder<H>>
     - 이펙티브자바 30-6 재귀적타입한정
     - HttpSecurityBuilder는 H를 빌드할 수 있다 (HttpSecurityBuilder<H ...) 
     - 그 H는 동시에 HttpSecurityBuilder이기도 하다.(... extends HttpSecurityBuilder<H>>) 
     - 즉 자기자신을 빌드하는 것도 가능하며 자기자신을 통해 빌드되는 것도 가능하다.
     - http://makble.com/java-generics-selfbounding

### HttpSecurity 설정하기 
  1. HttpSecurity를 설정하는 설정용 메소드는 2개씩 있다.
     - 파라미터 없이 AbstractHttpConfigurer의 구현체를 리턴하는 경우(주로 사용)
     - Customizer를 파라미터로 받고 HttpSecurity를 리턴하는 타입
     - ```java
           //예시1 - cors 설정
           public CorsConfigurer<HttpSecurity> cors() throws Exception
           public HttpSecurity cors(Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer) throws Exception
           //예시2 - sessionManagement 설정
           public SessionManagementConfigurer<HttpSecurity> sessionManagement() throws Exception
           public HttpSecurity sessionManagement(Customizer<SessionManagementConfigurer<HttpSecurity>> sessionManagementCustomizer) throws Exception
       ```
  2. AbstractHttpConfigurer의 구현체들은 FilterChain(HttpSecurity.performBuild()의 return 객체)에 등록될 filter 객체를 생성/설정/등록하는 역할을 한다.
  3. Customizer를 파라미터로 받는 설정용 메소드 사용법
     ```java
        http.authorizeRequests(authorizeRequests ->
        authorizeRequests.antMatchers("/**").hasRole("USER")
        )
        .passwordManagement(passwordManagement ->
        passwordManagement.changePasswordPage("/custom-change-password-page")
        );
     ```
  4. 설정용 메서드 목록 (설정용 메서드명 : Filter 클래스명(FilterOrderRegistration에 등록되어있음))
     - headers : HeaderWriterFilter
     - cors : CorsFilter
     - sessionManagement : SessionManagementFilter
     - portMapper : X
       - 다른 AbstractHttpConfigurer의 구현체들과 달리 init만 수행함. 
       - init시에 PortMapper객체를 모든 AbstractHttpConfigurer의 구현체들과 공유할 수 있도록 설정함
       - http.setSharedObject(PortMapper.class, getPortMapper());
       - ChannelSecurityConfigurer, AbstractAuthenticationFilterConfigurer에서 http.getSharedObject를 통해 PortMapper를 사용함
     - jee : J2eePreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter
     - x509 : X509AuthenticationFilter
     - rememberMe : RememberMeAuthenticationFilter
     - authorizeRequests : FilterSecurityInterceptor
       - 이 메서드와 관련된 ExpressionUrlAuthorizationConfigurer는 AbstractHttpConfigurer를 직접 구현하지 않고 그 하위 클래스인 AbstractInterceptUrlConfigurer를 구현하고 있음. 
       - AbstractInterceptUrlConfigurer의 configure에서 addFilter를 호출하고 있음.
     - authorizeHttpRequests : AuthorizationFilter
     - requestCache : RequestCacheAwareFilter
     - exceptionHandling : ExceptionTranslationFilter
     - securityContext : SecurityContextPersistenceFilter
     - servletApi : SecurityContextHolderAwareRequestFilter
     - csrf : CsrfFilter
     - logout : LogoutFilter
     - anonymous : AnonymousAuthenticationFilter
     - formLogin : UsernamePasswordAuthenticationFilter
       - 이 메서드와 관련된 FormLoginConfigurer는 AbstractHttpConfigurer를 직접 구현하지 않고 그 하위 클래스인 AbstractAuthenticationFilterConfigurer 구현하고 있음.
       - public final class FormLoginConfigurer<H extends HttpSecurityBuilder<H>> extends
         AbstractAuthenticationFilterConfigurer<H, FormLoginConfigurer<H>, ""UsernamePasswordAuthenticationFilter"">
     - saml2Login : Saml2WebSsoAuthenticationFilter
     - saml2Logout : Saml2LogoutRequestFilter, Saml2LogoutResponseFilter, LogoutFilter
     - oauth2Login : OAuth2LoginAuthenticationFilter
     - oauth2Client : OAuth2AuthorizationRequestRedirectFilter, OAuth2AuthorizationCodeGrantFilter
     - oauth2ResourceServer : BearerTokenAuthenticationFilter
       - OAuth2ResourceServerConfigurer의 jwt() 메서드가 눈에 띔
     - requiresChannel : ChannelProcessingFilter 
     - httpBasic : BasicAuthenticationFilter
     - passwordManagement : RequestMatcherRedirectFilter
       - RequestMatcherRedirectFilter는 FilterOrderRegistration소스상엔 없다. 
       - FilterOrderRegistration의 put 메서드를 통해 나중에 등록된다.
       - 

### HttpSecurity의 default known filter들
  - HttpSecurityBuilder의 addFilter() 주석 
  - HttpSecurity의 FilterOrderRegistration filterOrders 필드 : filter간 순서가 존재 함
  - FilterOrderRegistration 소스 살펴보기. 
  - HttpSecurity의 addFilter로는 커스텀 필터를 등록할 수 없다. 
    - order가 미리 정해져 있는 known 필터클래스나, 그 하위 클래스 필터만 등록 가능하다.
    - Adds a {@link Filter} that must be an instance of or extend one of the Filters provided within the Security framework
    - 단, addFilterAfter, addFilterBefore를 통해 커스텀 필터를 등록할 수 있다.
  - HttpSecurity의 addFilterAt (기존에 있던 필터를 overriding하진 않음.)
    - Registration of multiple Filters in the same location means their ordering is not
      deterministic. More concretely, registering multiple Filters in the same location
      does not override existing Filters.
  
### AuthenticationManagerBuilder authenticationManager, authenticationProvider, userDetailsService 등록 

## 3-3 AuthenticationManagerBuilder
### AuthenticationManagerBuilder의 역할
  - Allows for easily building in memory authentication, LDAP authentication, JDBC based authentication, adding UserDetailsService, and adding AuthenticationProvider's.

### AuthenticationManager의 역할
  - Attempts to authenticate the passed Authentication object, returning a fully populated Authentication object (including granted authorities) if successful.

### ProviderManager
  - AuthenticationManager의 구현체
  - 역할 : Iterates an Authentication request through a list of AuthenticationProviders.

### 설정용 메소드 
  - eraseCredentials
  - authenticationEventPublisher
  - parentAuthenticationManager

  - inMemoryAuthentication : InMemoryUserDetailsManagerConfigurer
    - 리턴으로 받는 InMemoryUserDetailsManagerConfigurer는 UserDetailsManagerConfigurer의 하위 클래스이다.
    - UserDetailsManagerConfigurer는 user정보를 설정할 수 있는 메서드들이 있다. 
  - jdbcAuthentication : JdbcUserDetailsManagerConfigurer
  - userDetailsService : DaoAuthenticationConfigurer


# ObjectPostProcessor 
security관련 Bean의 속성을 직접 변경하는 방법 
https://godekdls.github.io/Spring%20Security/javaconfiguration/

실 적용 예시. (Shared Object postProcessor )
https://blog.sunimos.me/25

# 참고
https://kimchanjung.github.io/programming/2020/07/02/spring-security-02/
샘플코드 : https://github.com/spring-projects/spring-security-samples
공식문서 : https://docs.spring.io/spring-security/reference/
한글화 ? : https://godekdls.github.io/Spring%20Security/saml2/
