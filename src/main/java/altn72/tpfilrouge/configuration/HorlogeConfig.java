package altn72.tpfilrouge.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class HorlogeConfig {
    @Bean
    public Clock horloge() {
        return Clock.systemDefaultZone();
    }
}

