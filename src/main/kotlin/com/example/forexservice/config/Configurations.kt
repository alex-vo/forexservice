package com.example.forexservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal


@Configuration
@EnableConfigurationProperties(ECBIntegrationProperties::class)
class ECBClientConfig {

    @Bean
    fun ecbClient(
        ecbIntegrationProperties: ECBIntegrationProperties,
        restTemplateBuilder: RestTemplateBuilder
    ): RestTemplate =
        restTemplateBuilder
            .rootUri(ecbIntegrationProperties.baseUri)
            .additionalMessageConverters(MarshallingHttpMessageConverter())
            .build()

}

@ConfigurationProperties(prefix = "integration.ecb")
data class ECBIntegrationProperties
@ConstructorBinding constructor(
    val baseUri: String
)

@Configuration
@EnableConfigurationProperties(value = [FeeProperties::class, CurrencyProperties::class])
class PropertiesConfig

@ConfigurationProperties(prefix = "fee")
data class FeeProperties
@ConstructorBinding constructor(
    val default: BigDecimal
)

@ConfigurationProperties(prefix = "currency")
data class CurrencyProperties
@ConstructorBinding constructor(
    val main: String,
    val rateScale: Int,
)

@Configuration
@EnableWebSecurity
class SecurityConfiguration {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
            .authorizeHttpRequests()
            .requestMatchers("/v1/rates/**", "/v1/fees/**").hasRole("ADMIN_ROLE")
            .requestMatchers("/v1/conversion").hasAnyRole("ADMIN_ROLE", "USER_ROLE")
            .and()
            .httpBasic()

        return http.build()
    }

    @Bean
    fun userDetailsService(): InMemoryUserDetailsManager {
        val user = User
            .withUsername("user")
            .password("{noop}123")
            .roles("USER_ROLE")
            .build()
        val admin = User
            .withUsername("admin")
            .password("{noop}123")
            .roles("ADMIN_ROLE")
            .build()
        return InMemoryUserDetailsManager(user, admin)
    }
}