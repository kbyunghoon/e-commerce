package kr.hhplus.be.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("🛒 E-Commerce 주문 서비스 API")
                    .description("""
                        ## 주요 기능
                        - **잔액 관리**: 충전, 조회
                        - **상품 관리**: 목록 조회, 상세 조회
                        - **쿠폰 시스템**: 선착순 발급, 보유 쿠폰 조회
                        - **주문 및 결제**: 주문 생성, 쿠폰 적용, 잔액 결제
                        - **통계**: 인기 상품 조회 (3일간 집계)
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("김병훈")
                            .url("https://github.com/kbyunghoon")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("개발 서버"),
                )
            )
    }
}
