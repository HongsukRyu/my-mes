package com.mes.poc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PocApplicationTests {

	@Test
	void contextLoads() {
		// Spring Boot 컨텍스트가 정상적으로 로드되는지 테스트
	}

}
