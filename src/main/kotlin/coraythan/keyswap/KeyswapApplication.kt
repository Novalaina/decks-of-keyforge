package coraythan.keyswap

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.sql.DataSource


@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT24H")
class KeyswapApplication() {

    private val log = LoggerFactory.getLogger(KeyswapApplication::class.java)

    companion object {

        val objectMapper = configureJackson(Jackson2ObjectMapperBuilder.json())

        private fun configureJackson(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
            val zonedDateTimeSer = object : StdSerializer<ZonedDateTime>(ZonedDateTime::class.java) {
                override fun serialize(value: ZonedDateTime, gen: JsonGenerator, provider: SerializerProvider) {
                    gen.writeString(value.format(TimeUtils.zonedDateTimeFormatter))
                }
            }
            val dateTimeSer = object : StdSerializer<LocalDateTime>(LocalDateTime::class.java) {
                override fun serialize(value: LocalDateTime, gen: JsonGenerator, provider: SerializerProvider) {
                    gen.writeString(value.format(TimeUtils.dateTimeFormatter))
                }
            }
            val dateSer = object : StdSerializer<LocalDate>(LocalDate::class.java) {
                override fun serialize(value: LocalDate, gen: JsonGenerator, provider: SerializerProvider) {
                    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            }
            val dateDeser = object : StdDeserializer<LocalDate>(LocalDate::class.java) {
                override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LocalDate =
                    LocalDate.parse(jp.readValueAs(String::class.java))
            }

            return builder
                .modulesToInstall(
                    JavaTimeModule()
                        .addSerializer(zonedDateTimeSer)
                        .addSerializer(dateTimeSer)
                        .addSerializer(dateSer)
                        .addDeserializer(LocalDate::class.java, dateDeser)
                )
                .build()
        }
    }

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.setConnectTimeout(30000)
        requestFactory.setConnectionRequestTimeout(30000)
        restTemplate.requestFactory = requestFactory
        return restTemplate
    }

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun objectMapper(builder: Jackson2ObjectMapperBuilder) = configureJackson(builder)

    @Bean
    fun lockProvider(dataSource: DataSource): LockProvider {
        return JdbcTemplateLockProvider(dataSource)
    }

    @Bean
    fun threadPoolTaskScheduler(): TaskScheduler {
        val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
        threadPoolTaskScheduler.poolSize = 3
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler")
        return threadPoolTaskScheduler
    }

}

fun main(args: Array<String>) {
    runApplication<KeyswapApplication>(*args)
}

object Api {
    const val base = "/api"
}

val scheduledStart = "Starting scheduled task:"
val scheduledStop = "Scheduled task complete:"
val scheduledException = "Exception in scheduled task:"
