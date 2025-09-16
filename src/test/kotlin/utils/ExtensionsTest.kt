package utils

import com.sriniketh.utils.cleanHtmlContent
import kotlin.test.Test

class ExtensionsTest {

    @Test
    fun `clean HTML content`() {
        val html = """
            <html>
                <body>
                    <h1>Title</h1>
                    <p>This is a <b>bold</b> paragraph.</p>
                    <p>This is another paragraph with &amp; special characters &lt; &gt; &quot; &#39;.</p>
                </body>
            </html>
        """.trimIndent()

        val expected = "Title This is a bold paragraph. This is another paragraph with & special characters < > \" '."
        val cleaned = html.cleanHtmlContent()
        assert(cleaned == expected)
    }

    @Test
    fun `clean HTML content with empty string`() {
        val html = ""
        val expected = ""
        val cleaned = html.cleanHtmlContent()
        assert(cleaned == expected)
    }

    @Test
    fun `clean HTML content with no HTML`() {
        val html = "This is a plain text without any HTML."
        val expected = "This is a plain text without any HTML."
        val cleaned = html.cleanHtmlContent()
        assert(cleaned == expected)
    }

    @Test
    fun `clean HTML content with malformed HTML`() {
        val html = "<html><body><h1>Title<p>This is a paragraph without closing tags."
        val expected = "Title This is a paragraph without closing tags."
        val cleaned = html.cleanHtmlContent()
        assert(cleaned == expected)
    }

    @Test
    fun `clean HTML content with only special characters`() {
        val html = "&nbsp;&amp;&lt;&gt;&quot;&#39;"
        val cleaned = html.cleanHtmlContent()
        val expected = "&<>\"'"
        assert(cleaned == expected)
    }

    @Test
    fun `clean HTML content with nested tags`() {
        val html = "<div><p>This is a <span>nested <b>bold</b> text</span> inside a paragraph.</p></div>"
        val expected = "This is a nested bold text inside a paragraph."
        val cleaned = html.cleanHtmlContent()
        assert(cleaned == expected)
    }

    @Test
    fun `clean HTML content with multiple spaces`() {
        val html = "<p>This    is    a    paragraph    with    irregular    spacing.</p>"
        val expected = "This is a paragraph with irregular spacing."
        val cleaned = html.cleanHtmlContent()
        assert(cleaned == expected)
    }

    @Test
    fun `clean HTML content with new lines and tabs`() {
        val html = "<p>This is a paragraph.\n\tThis is a new line with a tab.</p>"
        val expected = "This is a paragraph. This is a new line with a tab."
        val cleaned = html.cleanHtmlContent()
        assert(cleaned == expected)
    }

    @Test
    fun `clean HTML content with script and style tags`() {
        val html = "<html><head><style>body{}</style><script>alert('hi')</script></head><body>Content</body></html>"
        val expected = "Content"
        assert(html.cleanHtmlContent() == expected)
    }
}
