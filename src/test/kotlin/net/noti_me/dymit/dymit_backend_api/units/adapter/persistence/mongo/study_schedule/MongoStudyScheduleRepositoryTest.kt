package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.study_schedule

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_schedule.MongoStudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.LocalDateTime

@Import(MongoConfig::class)
@DataMongoTest
internal class MongoStudyScheduleRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private val scheduleRepository = MongoStudyScheduleRepository(mongoTemplate)

    companion object {
        fun createTestStudySchedule(
            id: ObjectId = ObjectId.get(),
            groupId: ObjectId = ObjectId.get(),
            title: String = "테스트 스터디 일정",
            description: String = "테스트 설명",
            location: ScheduleLocation = ScheduleLocation(
                type = ScheduleLocation.LocationType.OFFLINE,
                value = "테스트 장소"
            ),
            session: Long = 1,
            scheduleAt: LocalDateTime = LocalDateTime.now().plusDays(1),
            participants: MutableSet<ObjectId> = mutableSetOf(ObjectId.get()),
            roles: MutableSet<ScheduleRole> = mutableSetOf(
                ScheduleRole(
                    memberId = ObjectId.get(),
                    nickname = "테스트 멤버",
                    image = ProfileImageVo(
                        type = "preset",
                        url = "0"
                    ),
                    roles = listOf("자료조사")
                )
            )
        ): StudySchedule {
            return StudySchedule(
                id = id,
                groupId = groupId,
                title = title,
                description = description,
                location = location,
                session = session,
                scheduleAt = scheduleAt,
                roles = roles
            )
        }
    }

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection(StudySchedule::class.java)
    }

    @Test
    fun `save - 스터디 일정을 저장할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()

        // When
        val savedSchedule = scheduleRepository.save(schedule)

        // Then
        savedSchedule.shouldNotBeNull()
        savedSchedule.id shouldBe schedule.id
        savedSchedule.groupId shouldBe schedule.groupId
        savedSchedule.title shouldBe schedule.title
    }

    @Test
    fun `delete - 스터디 일정을 삭제할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()
        scheduleRepository.save(schedule)

        // When
        val result = scheduleRepository.delete(schedule)

        // Then
        result shouldBe true
        scheduleRepository.loadById(schedule.id).shouldBeNull()
    }

    @Test
    fun `delete - 존재하지 않는 스터디 일정 삭제 시 false를 반환한다`() {
        // Given
        val schedule = createTestStudySchedule()

        // When
        val result = scheduleRepository.delete(schedule)

        // Then
        result shouldBe false
    }

    @Test
    fun `deleteById - ID로 스터디 일정을 삭제할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()
        scheduleRepository.save(schedule)

        // When
        val result = scheduleRepository.deleteById(schedule.id)

        // Then
        result shouldBe true
        scheduleRepository.loadById(schedule.id).shouldBeNull()
    }

    @Test
    fun `deleteById - 존재하지 않는 ID로 삭제 시 false를 반환한다`() {
        // Given
        val nonExistentId = ObjectId.get()

        // When
        val result = scheduleRepository.deleteById(nonExistentId)

        // Then
        result shouldBe false
    }

    @Test
    fun `loadById - ID로 스터디 일정을 조회할 수 있다`() {
        // Given
        val schedule = createTestStudySchedule()
        scheduleRepository.save(schedule)

        // When
        val loadedSchedule = scheduleRepository.loadById(schedule.id)

        // Then
        loadedSchedule.shouldNotBeNull()
        loadedSchedule.id shouldBe schedule.id
        loadedSchedule.groupId shouldBe schedule.groupId
        loadedSchedule.title shouldBe schedule.title
    }

    @Test
    fun `loadById - 존재하지 않는 ID로 조회 시 null을 반환한다`() {
        // Given
        val nonExistentId = ObjectId.get()

        // When
        val loadedSchedule = scheduleRepository.loadById(nonExistentId)

        // Then
        loadedSchedule.shouldBeNull()
    }

    @Test
    fun `loadByGroupIdOrderByScheduleAtDesc - 그룹 ID로 일정을 조회하고 일정 시간 내림차순으로 정렬한다`() {
        // Given
        val groupId = ObjectId.get()
        val now = LocalDateTime.now()

        val schedule1 = createTestStudySchedule(
            groupId = groupId,
            scheduleAt = now.plusDays(1),
            title = "첫 번째 일정"
        )
        val schedule2 = createTestStudySchedule(
            groupId = groupId,
            scheduleAt = now.plusDays(3),
            title = "세 번째 일정"
        )
        val schedule3 = createTestStudySchedule(
            groupId = groupId,
            scheduleAt = now.plusDays(2),
            title = "두 번째 일정"
        )
        val otherGroupSchedule = createTestStudySchedule(
            groupId = ObjectId.get(),
            scheduleAt = now.plusDays(5),
            title = "다른 그룹 일정"
        )

        scheduleRepository.save(schedule1)
        scheduleRepository.save(schedule2)
        scheduleRepository.save(schedule3)
        scheduleRepository.save(otherGroupSchedule)

        // When
        val schedules = scheduleRepository.loadByGroupIdOrderByScheduleAtDesc(groupId)

        // Then
        schedules shouldHaveSize 3
        schedules[0].title shouldBe "세 번째 일정" // 가장 늦은 시간
        schedules[1].title shouldBe "두 번째 일정"
        schedules[2].title shouldBe "첫 번째 일정" // 가장 이른 시간
    }

    @Test
    fun `loadByGroupIdOrderByScheduleAtDesc - 존재하지 않는 그룹 ID로 조회 시 빈 리스트를 반환한다`() {
        // Given
        val nonExistentGroupId = ObjectId.get()
        val schedule = createTestStudySchedule(groupId = ObjectId.get())
        scheduleRepository.save(schedule)

        // When
        val schedules = scheduleRepository.loadByGroupIdOrderByScheduleAtDesc(nonExistentGroupId)

        // Then
        schedules shouldHaveSize 0
    }
}