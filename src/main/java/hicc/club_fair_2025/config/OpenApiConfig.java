package hicc.club_fair_2025.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * API 문서 자동 생성 설정
 */
@OpenAPIDefinition(
	info = @Info(
		title = "Hongik Club Fair 2025 API",
		description = "홍익대학교 동아리 박람회 프로젝트 API 문서"
	)
)
@Configuration
public class OpenApiConfig {
}