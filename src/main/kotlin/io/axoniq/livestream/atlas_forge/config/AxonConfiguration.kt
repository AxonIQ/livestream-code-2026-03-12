package io.axoniq.livestream.atlas_forge.config

import org.axonframework.messaging.eventhandling.processing.streaming.token.store.TokenStore
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.inmemory.InMemoryTokenStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfiguration() {
    @Bean
    fun tokenStore() : TokenStore {
        return InMemoryTokenStore()
    }
}
