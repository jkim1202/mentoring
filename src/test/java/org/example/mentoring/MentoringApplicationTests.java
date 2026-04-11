package org.example.mentoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "security.jwt.refresh-secret=ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=",
        "security.jwt.access-exp-minutes=30",
        "security.jwt.refresh-exp-days=7"
})
class MentoringApplicationTests {

    @Test
    void contextLoads() {
    }

}
