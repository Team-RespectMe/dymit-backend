package net.noti_me.dymit.dymit_backend_api.common.daily_statistics

import jakarta.annotation.PostConstruct
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.stereotype.Component

/**
 * Creates the unique index required by daily-statistics upserts.
 */
@Component
class DailyStatisticsIndexInitializer(
    private val mongoTemplate: MongoTemplate
) {

    /**
     * Ensures one statistics document can exist for each business date.
     */
    @PostConstruct
    fun ensureIndexes() {
        mongoTemplate.indexOps(DAILY_STATISTICS_COLLECTION).createIndex(
            Index()
                .on("statisticDate", Sort.Direction.ASC)
                .unique()
                .named("daily_stats_statistic_date_uq")
        )
    }

    private companion object {
        const val DAILY_STATISTICS_COLLECTION = "daily_stats"
    }
}
