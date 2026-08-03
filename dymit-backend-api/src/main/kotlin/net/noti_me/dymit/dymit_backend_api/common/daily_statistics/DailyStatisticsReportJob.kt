package net.noti_me.dymit.dymit_backend_api.common.daily_statistics

import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DailyStatisticsReportFormatter
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordWebhookTransport
import net.noti_me.dymit.dymit_backend_api.configs.DiscordConfig
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

/**
 * Reads the completed prior-day statistics document and sends one daily report to Discord.
 */
@Component
@DisallowConcurrentExecution
class DailyStatisticsReportJob(
    private val mongoTemplate: MongoTemplate,
    private val config: DiscordConfig,
    private val formatter: DailyStatisticsReportFormatter,
    private val transport: DiscordWebhookTransport
) : Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Sends one best-effort report for the previous Korean business date when its document exists.
     */
    override fun execute(context: JobExecutionContext?) {
        val window = DailyStatisticsWindowCalculator.calculate()
        val document = mongoTemplate.findOne(
            Query(Criteria.where("statisticDate").`is`(window.statisticDate)),
            DailyStatisticsReportDocument::class.java,
            DAILY_STATISTICS_COLLECTION
        )
        if ( document == null ) {
            logger.info("Daily statistics report skipped: no document for ${window.statisticDate}")
            return
        }

        try {
            transport.send(
                webhookUrl = config.getDailyStatisticsUrl(),
                message = formatter.format(document)
            )
                .doOnError { error ->
                    logger.error("Failed to send daily statistics report: ${error.message}", error)
                }
                .onErrorComplete()
                .subscribe()
        } catch (error: Exception) {
            logger.error("Failed to send daily statistics report: ${error.message}", error)
        }
    }

    private companion object {
        const val DAILY_STATISTICS_COLLECTION = "daily_stats"
    }
}
