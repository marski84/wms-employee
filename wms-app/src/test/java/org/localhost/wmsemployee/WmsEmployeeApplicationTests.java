package org.localhost.wmsemployee;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Application context loading test.
 * <p>
 * This test is disabled by default as it requires full environment setup with Auth0 credentials.
 * To enable this test:
 * 1. Create auth0.env file in project root with Auth0 credentials
 * 2. Ensure PostgreSQL, RabbitMQ, and NATS are running (docker compose up -d)
 * 3. Add @SpringBootTest annotation to this class
 * 4. Remove @Disabled annotation from contextLoads() method
 */
class WmsEmployeeApplicationTests {

    @Test
    @Disabled("Requires auth0.env configuration and external dependencies. "
            + "Run this test when full environment is set up with Auth0 credentials.")
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // If this test passes, all beans are properly configured and wired
    }

}
