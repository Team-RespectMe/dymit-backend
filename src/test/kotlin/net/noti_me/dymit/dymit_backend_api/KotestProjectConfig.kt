package net.noti_me.dymit.dymit_backend_api

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.junitxml.JunitXmlReporter
// import io.kotest.extensions.htmlreporter.HtmlReporter

// class KotestProjectConfig : AbstractProjectConfig() {

    /**
     * 테스트 이름을 생성할 때 부모 컨텍스트(Given, When 등)의 이름을 포함하도록 설정합니다.
     * 이 값을 true로 설정하면 테스트 리포트에서 각 테스트를 명확하게 구분할 수 있습니다.
     */
    // override fun extensions(): List<Extension> = listOf(
        // JunitXmlReporter (
            // includeContainers = true, // don't write out status for all tests
            // useTestPathAsName = true, // use the full test path (ie, includes parent test names)
            // outputDir = "../build/test-results/test/" // include to set output dir for maven
        // ),
    // )

    // override fun extensions(): List<Extension> = listOf(
    //     JunitXmlReporter(
    //         includeContainers = true, // don't write out status for all tests
    //         useTestPathAsName = true, // use the full test path (ie, includes parent test names)
    //         outputDir = "../build/test-results/test/" // include to set output dir for maven
    //     )
    //     // HtmlReporter() // Uncomment if you want to use HTML reporter
    // )

    // override val displayFullTestPath = true

    // override val testNameAppendTags = true
// }