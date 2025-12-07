package net.noti_me.dymit.dymit_backend_api.common.sanitizer

import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import org.springframework.stereotype.Service

@Service
class SanitizerService {
    private val policy: PolicyFactory = HtmlPolicyBuilder()
        // 허용 태그
        .allowElements("b", "i", "u", "em", "strong", "strike", "s", "sup", "sub", "code", "pre", "tt", "br",
                       "a", "blockquote", "p", "div", "span", "ul", "ol", "li", "dl", "dt", "dd", "hr",
                       "h1", "h2", "h3", "h4", "h5", "h6", "font", "center", "table", "thead", "tbody", "tfoot", "tr", "td", "th", "img")
        // a 태그 속성 제한
        .allowAttributes("href", "rel", "target").onElements("a")
        .allowUrlProtocols("http", "https", "mailto")
        .requireRelNofollowOnLinks()
        // img 태그 속성 제한
        .allowAttributes("src", "alt", "title", "width", "height")
            .onElements("img")
        .allowUrlProtocols("http", "https", "data")
        // font 태그 속성 제한
        .allowAttributes("color", "face", "size").onElements("font")
        // style 태그 및 style 속성 제한 (필요시)
        // .allowStyling() // 필요에 따라 제한적으로 허용
        // 기타 태그 속성 제한
        .allowAttributes("align", "valign", "colspan", "rowspan").onElements("td", "th", "tr")
        .allowAttributes("class", "id")
            .onElements("div", "span", "p", "table", "ul", "ol", "li")
        // 이벤트 핸들러 속성 금지
        // javascript: 스킴 금지 (allowUrlProtocols로 제한)
        .toFactory()

    fun sanitize(input: String?): String {
        if (input == null) return ""
        return policy.sanitize(input)
    }
}