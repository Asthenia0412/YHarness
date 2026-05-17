
package com.example.yharness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.yharness.config.AgentProperties;

@SpringBootApplication
@EnableConfigurationProperties(AgentProperties.class)
public class YHarnessApplication {

    public static void main(String[] args) {
        SpringApplication.run(YHarnessApplication.class, args);
    }
}
