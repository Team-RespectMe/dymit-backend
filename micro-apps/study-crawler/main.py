"""애플리케이션 실행 진입점.

기능:
- parsing.yaml 설정을 로드합니다.
- Crawler를 실행해 스터디 정보를 수집합니다.
- 수집 결과를 MongoDB에 upsert 저장합니다.

매개변수:
- run(): 없음
- main(): 없음

반환형:
- run: None
- main: None
"""

import logging

from app.crawler import Crawler
from app.db.mongodb_connection import MongoStudyInfoConnection
from app.dto.study_info import StudyInfo
from app.site_config import SiteConfigLoader


logger = logging.getLogger(__name__)


def run() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s - %(message)s",
    )

    site_config = SiteConfigLoader().get_site_config()
    site_list = site_config.get("sites") or site_config.get("services") or []
    logger.info("Loaded site config entries: %s", len(site_list) if isinstance(site_list, list) else 0)

    crawler = Crawler(site_config)
    crawled_results = crawler.run()
    logger.info("Crawler returned result groups: %s", len(crawled_results))

    studies: list[StudyInfo] = []
    for site_result in crawled_results:
        if isinstance(site_result, list):
            for item in site_result:
                if isinstance(item, StudyInfo):
                    studies.append(item)

    logger.info("Parsed study count: %s", len(studies))
    if studies:
        preview = [
            {
                "identifier": study.identifier,
                "title": study.title,
                "writer": study.writer,
            }
            for study in studies[:5]
        ]
        logger.info("Parsed study preview (max 5): %s", preview)

    if not studies:
        logger.warning("No parsed studies. Skip DB write.")
        return

    db = MongoStudyInfoConnection()
    upserted_ids = db.insert_many(studies)
    logger.info("DB upsert done. Upserted IDs count: %s", len(upserted_ids))


def main() -> None:
    run()


if __name__ == "__main__":
    main()
