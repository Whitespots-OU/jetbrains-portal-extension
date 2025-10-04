package io.whitespots.appsecplugin.utils

import com.intellij.ide.BrowserUtil
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import io.whitespots.appsecplugin.models.Finding
import io.whitespots.appsecplugin.models.TriageStatus
import io.whitespots.appsecplugin.services.AppSecPluginSettings
import io.whitespots.appsecplugin.services.AutoValidatorService
import io.whitespots.appsecplugin.services.FindingRejectionService
import io.whitespots.appsecplugin.services.FindingsRefreshTopics
import kotlinx.coroutines.*
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.network.CefRequest
import java.net.URLEncoder

object ThemeUtils {
    private val LOG = logger<ThemeUtils>()

    fun isDarkTheme(): Boolean {
        return LafManager.getInstance().currentUIThemeLookAndFeel.isDark
    }

    fun configureBrowserForExternalLinks(browser: JBCefBrowser) {
        val cefBrowser = browser.cefBrowser
        val client = cefBrowser.client

        client.addRequestHandler(object : CefRequestHandlerAdapter() {
            override fun onBeforeBrowse(
                browser: CefBrowser,
                frame: CefFrame,
                request: CefRequest,
                userGesture: Boolean,
                isRedirect: Boolean
            ): Boolean {
                val url = request.url

                if (url.startsWith("data:") || url.startsWith("about:")) {
                    return false
                }

                if (url.startsWith("http://") || url.startsWith("https://")) {
                    try {
                        BrowserUtil.browse(url)
                        LOG.info("Opened external URL in system browser: $url")
                    } catch (e: Exception) {
                        LOG.warn("Failed to open URL in system browser: $url", e)
                    }
                    return true
                }

                return false
            }
        })
    }

    fun getIntellijThemeCSS(): String {
        return if (isDarkTheme()) {
            getDarkThemeCSS()
        } else {
            getLightThemeCSS()
        }
    }

    private fun getDarkThemeCSS(): String {
        return """
            body {
                background-color: #1E1F22;
                color: #BCBEC4;
                font-family: 'SF Pro Text', 'Segoe UI', Ubuntu, Arial, sans-serif;
                font-size: 13px;
                text-wrap: pretty;
                overflow-wrap: break-word;
                margin: 0px 16px 16px 16px;
                padding: 0;
            }
            h1, h2, h3, h4, h5, h6 {
                margin-top: 16px;
                margin-bottom: 16px;
                font-weight: 600;
            }
            h1 { font-size: 3em; }
            h2 { font-size: 2em; }
            h3 { font-size: 1.5em; }
            h4 { font-size: 1em; }
            h5 { font-size: 0.83em; }
            h6 { font-size: 0.75em; }
            p {
                margin: 8px 0;
            }
            code {
                background-color: #3C3F41;
                color: #A9B7C6;
                padding: 2px 6px;
                border-radius: 4px;
                font-family: 'JetBrains Mono', 'Courier New', monospace;
                font-size: 12px;
            }
            pre {
                background-color: #3C3F41;
                border: 1px solid #555555;
                border-radius: 6px;
                padding: 16px;
                overflow-x: auto;
                margin: 16px 0;
            }
            pre code {
                background-color: transparent;
                padding: 0;
                border-radius: 0;
            }
            a {
                color: #589DF6;
                text-decoration: underline;
                cursor: pointer;
            }
            a img {
                display: inline-block;
                vertical-align: middle;
                border: none;
                text-decoration: none;
                margin: 2px;
                border-radius: 4px;
                transition: opacity 0.2s ease;
            }
            a img:hover {
                opacity: 0.8;
            }
            .tooltip {
                position: relative;
                display: inline-block;
            }
            .tooltip .tooltiptext {
                visibility: hidden;
                width: 100%;
                background-color: #3C3F41;
                color: #BCBEC4;
                text-align: center;
                border-radius: 6px;
                padding: 8px;
                position: absolute;
                z-index: 1000;
                bottom: 125%;
                left: 0%;
                margin-left: -8px;
                opacity: 0;
                transition: opacity 0.3s;
                font-size: 12px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.3);
            }
            .tooltip:hover .tooltiptext {
                visibility: visible;
                opacity: 1;
            }
            .tooltip .tooltiptext::after {
                content: "";
                position: absolute;
                top: 100%;
                left: 50%;
                margin-left: -5px;
                border-width: 5px;
                border-style: solid;
                border-color: #3C3F41 transparent transparent transparent;
            }
            blockquote {
                border-left: 4px solid #CC7832;
                margin: 16px 0;
                padding: 4px 0 4px 16px;
                color: #9876AA;
                background-color: rgba(204, 120, 50, 0.1);
                border-radius: 0 4px 4px 0;
            }
            ul, ol {
                margin: 16px 0;
                padding-left: 32px;
            }
            li {
                margin: 4px 0;
            }
            table {
                border-collapse: collapse;
                width: 100%;
                margin: 16px 0;
            }
            th, td {
                border: 1px solid #555555;
                padding: 8px 12px;
                text-align: left;
            }
            th {
                background-color: #3C3F41;
                font-weight: 600;
            }
            tr:nth-child(even) {
                background-color: rgba(255, 255, 255, 0.05);
            }
            strong, b {
                color: #FFC66D;
                font-weight: 600;
            }
            em, i {
                color: #6A8759;
                font-style: italic;
            }
            hr {
                border: none;
                border-top: 1px solid #4C5052;
                margin: 24px 0;
                background: transparent;
            }
        """.trimIndent()
    }

    private fun getLightThemeCSS(): String {
        return """
            body {
                background-color: #FFFFFF;
                color: #000000;
                font-family: 'SF Pro Text', 'Segoe UI', Ubuntu, Arial, sans-serif;
                font-size: 13px;
                text-wrap: pretty;
                overflow-wrap: break-word;
                margin: 0px 16px 16px 16px;
                padding: 0;
            }
            h1, h2, h3, h4, h5, h6 {
                margin-top: 16px;
                margin-bottom: 16px;
                font-weight: 600;
            }
            h1 { font-size: 3em; }
            h2 { font-size: 2em; }
            h3 { font-size: 1.5em; }
            h4 { font-size: 1em; }
            h5 { font-size: 0.83em; }
            h6 { font-size: 0.75em; }
            p {
                margin: 8px 0;
            }
            code {
                background-color: #F5F5F5;
                color: #D73A49;
                padding: 2px 6px;
                border-radius: 4px;
                font-family: 'JetBrains Mono', 'Courier New', monospace;
                font-size: 12px;
            }
            pre {
                background-color: #F6F8FA;
                border: 1px solid #E1E4E8;
                border-radius: 6px;
                padding: 16px;
                overflow-x: auto;
                margin: 16px 0;
            }
            pre code {
                background-color: transparent;
                color: #24292E;
                padding: 0;
                border-radius: 0;
            }
            a {
                color: #0366D6;
                text-decoration: underline;
                cursor: pointer;
            }
            a img {
                display: inline-block;
                vertical-align: middle;
                border: none;
                text-decoration: none;
                margin: 2px;
                border-radius: 4px;
                transition: opacity 0.2s ease;
            }
            a img:hover {
                opacity: 0.8;
            }
            .tooltip {
                position: relative;
                display: inline-block;
            }
            .tooltip .tooltiptext {
                visibility: hidden;
                width: 100%;
                background-color: #FFFFFF;
                color: #000000;
                text-align: center;
                border-radius: 6px;
                padding: 8px;
                position: absolute;
                z-index: 1000;
                bottom: 125%;
                left: 0%;
                margin-left: -8px;
                opacity: 0;
                transition: opacity 0.3s;
                border: 1px solid #E1E4E8;
                font-size: 12px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            }
            .tooltip:hover .tooltiptext {
                visibility: visible;
                opacity: 1;
            }
            .tooltip .tooltiptext::after {
                content: "";
                position: absolute;
                top: 100%;
                left: 50%;
                margin-left: -5px;
                border-width: 5px;
                border-style: solid;
                border-color: #FFFFFF transparent transparent transparent;
            }
            blockquote {
                border-left: 4px solid #DFE2E5;
                margin: 16px 0;
                padding: 4px 0 4px 16px;
                color: #6A737D;
                background-color: rgba(223, 226, 229, 0.2);
                border-radius: 0 4px 4px 0;
            }
            ul, ol {
                margin: 16px 0;
                padding-left: 32px;
            }
            li {
                margin: 4px 0;
            }
            table {
                border-collapse: collapse;
                width: 100%;
                margin: 16px 0;
            }
            th, td {
                border: 1px solid #E1E4E8;
                padding: 8px 12px;
                text-align: left;
            }
            th {
                background-color: #F6F8FA;
                font-weight: 600;
            }
            tr:nth-child(even) {
                background-color: #F6F8FA;
            }
            strong, b {
                color: #24292E;
                font-weight: 600;
            }
            em, i {
                color: #6A737D;
                font-style: italic;
            }
            hr {
                border: none;
                border-top: 1px solid #D0D7DE;
                margin: 24px 0;
                background: transparent;
            }
        """.trimIndent()
    }

    fun createStyledHtmlTemplate(bodyContent: String, title: String = "Content"): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$title</title>
                <style>
                    ${getIntellijThemeCSS()}
                </style>
            </head>
            <body>
                $bodyContent
            </body>
            </html>
        """.trimIndent()
    }

    fun buildFindingMarkdown(finding: Finding): String {
        val markdown = StringBuilder()

        val settings = service<AppSecPluginSettings>().state
        val severityColor = when (finding.severity.name.lowercase()) {
            "critical" -> "#ff0000"
            "high" -> "#ff8800"
            "medium" -> "#ffaa00"
            "low" -> "#00aa00"
            "info" -> "#0088ff"
            else -> "#888888"
        }
        val url = "<a href='${settings.apiUrl.removeSuffix("/")}/products/${finding.product}/findings/${finding.id}' target='_blank'>${finding.id}</a>"

        markdown.append("### ${url}: <span style='color: $severityColor; font-weight: bold;'>${finding.severity.name}</span> - ${finding.name}\n\n")

        markdown.append("### Status: ${finding.triageStatus.name.lowercase().replaceFirstChar { it.uppercase() }}\n\n")

        val buttons = mutableListOf<String>()

        if (finding.triageStatus != TriageStatus.REJECTED) {
            buttons.add(SvgButtonUtils.createRejectButton(finding.id))
        }

        if (finding.filePath != null) {
            buttons.add(SvgButtonUtils.createRejectForeverButton(finding.id))
        }

        if (buttons.isNotEmpty()) {
            markdown.append("<div style=\"margin: 10px 0;\">${buttons.joinToString("&nbsp;&nbsp;")}</div>\n\n")
        }

        if (!finding.lineText.isNullOrBlank()) {
            markdown.append("### Code snippet:\n\n")
            markdown.append("```${finding.language}\n${finding.lineText}\n```")
            markdown.append("\n\n")
        }

        if (!finding.description.isNullOrBlank()) {
            markdown.append("### Description\n\n")
            markdown.append(finding.description)
            markdown.append("\n\n")
        }

        if (finding.tags.isNotEmpty()) {
            markdown.append("### Tags\n\n")
            finding.tags.forEach { tag ->
                markdown.append("- `$tag`\n")
            }
            markdown.append("\n")
        }

        return markdown.toString()
    }

    fun prepareMarkdownPage(
        browser: JBCefBrowser,
        finding: Finding,
        project: Project,
        jsQuery: JBCefJSQuery
    ) {
        configureBrowserForExternalLinks(browser)

        jsQuery.addHandler { query ->
            when {
                query.startsWith("reject-finding:") -> {
                    val findingId = query.substringAfter("reject-finding:").toLongOrNull()
                    LOG.info("Processing reject finding: $findingId for finding: ${finding.id}")
                    if (findingId != null && findingId == finding.id) {
                        handleRejectFinding(project, finding)
                    }
                }
                query.startsWith("reject-finding-forever:") -> {
                    val findingId = query.substringAfter("reject-finding-forever:").toLongOrNull()
                    LOG.info("Processing reject finding forever: $findingId for finding: ${finding.id}")
                    if (findingId != null && findingId == finding.id) {
                        handleRejectFindingForever(project, finding)
                    }
                }
                query.startsWith("open-external:") -> {
                    val url = query.substringAfter("open-external:")
                    try {
                        BrowserUtil.browse(url)
                        LOG.info("Opened external URL in system browser via JS: $url")
                    } catch (e: Exception) {
                        LOG.warn("Failed to open URL in system browser via JS: $url", e)
                    }
                }
            }
            null
        }

        val htmlContent = MarkdownConverter.toStyledHtml(
            buildFindingMarkdown(finding)
        )

        val htmlWithJS = htmlContent.replace(
            "</body>",
            """
                <script>
                    document.addEventListener('click', function(e) {
                        var anchor = e.target.closest('a');

                        if (anchor) {
                            if (anchor.href.startsWith('reject-finding:') || anchor.href.startsWith('reject-finding-forever:')) {
                                e.preventDefault();
                                e.stopPropagation();
                                ${jsQuery.inject("anchor.href")};
                                return false;
                            } else if (anchor.hasAttribute('target') && anchor.getAttribute('target') === '_blank') {
                                e.preventDefault();
                                e.stopPropagation();
                                ${jsQuery.inject("'open-external:' + anchor.href")};
                                return false;
                            }
                        }
                    });
                </script>
                </body>
                """.trimIndent()
        )

        browser.loadHTML(htmlWithJS)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleRejectFinding(project: Project, finding: Finding) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val rejectionService = FindingRejectionService.getInstance(project)
                val result = rejectionService.rejectFinding(finding)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Messages.showInfoMessage(
                            project,
                            "Finding ${finding.id} has been rejected successfully.",
                            "Finding Rejected"
                        )

                        project.messageBus.syncPublisher(FindingsRefreshTopics.REFRESH_TOPIC)
                            .onRefreshRequested()
                    } else {
                        val error = result.exceptionOrNull()
                        Messages.showErrorDialog(
                            project,
                            "Failed to reject finding ${finding.id}: ${error?.message ?: "Unknown error"}",
                            "Rejection Failed"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Messages.showErrorDialog(
                        project,
                        "Failed to reject finding ${finding.id}: ${e.message}",
                        "Rejection Failed"
                    )
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleRejectFindingForever(project: Project, finding: Finding) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                LOG.info("Starting reject finding forever process for finding ${finding.id}")
                val autoValidatorService = AutoValidatorService.getInstance(project)
                val ruleResult = autoValidatorService.rejectFindingForever(finding)

                withContext(Dispatchers.Main) {
                    if (ruleResult.isSuccess) {
                        when (val result = ruleResult.getOrThrow()) {
                            is AutoValidatorService.RuleCreationResult.RuleCreated -> {
                                val settings = service<AppSecPluginSettings>().state
                                val ruleUrl = "${settings.apiUrl.removeSuffix("/")}/autovalidator/rule/${result.ruleId}"
                                val message = "Auto-validator rule has been created to reject similar findings forever.\n\nView created rule: $ruleUrl"

                                val choice = Messages.showDialog(
                                    project,
                                    message,
                                    "Finding Rejected Forever",
                                    arrayOf("View Rule", "OK"),
                                    0,
                                    Messages.getInformationIcon()
                                )

                                if (choice == 0) {
                                    try {
                                        BrowserUtil.browse(ruleUrl)
                                        LOG.info("Opened rule URL in system browser: $ruleUrl")
                                    } catch (e: Exception) {
                                        LOG.warn("Failed to open rule URL in system browser: $ruleUrl", e)
                                    }
                                }
                            }
                            is AutoValidatorService.RuleCreationResult.ExistingRulesFound -> {
                                val settings = service<AppSecPluginSettings>().state
                                val actionChoicesParam = URLEncoder.encode("${result.queryParams.actionChoices}", "UTF-8")
                                val searchParam = URLEncoder.encode(result.queryParams.search, "UTF-8")
                                val rulesUrl = "${settings.apiUrl.removeSuffix("/")}/autovalidator?action_choices=$actionChoicesParam&search=$searchParam"

                                val message = "Found ${result.count} rule${if (result.count == 1) "" else "s"} for this finding.\n\nExisting rules already handle similar findings."

                                val choice = Messages.showDialog(
                                    project,
                                    message,
                                    "Existing Rules Found",
                                    arrayOf("View Rules", "OK"),
                                    0,
                                    Messages.getWarningIcon()
                                )

                                if (choice == 0) {
                                    try {
                                        BrowserUtil.browse(rulesUrl)
                                        LOG.info("Opened rules URL in system browser: $rulesUrl")
                                    } catch (e: Exception) {
                                        LOG.warn("Failed to open rules URL in system browser: $rulesUrl", e)
                                    }
                                }
                                return@withContext
                            }
                        }
                    } else {
                        val error = ruleResult.exceptionOrNull()
                        Messages.showWarningDialog(
                            project,
                            "Failed to create auto-validator rule: ${error?.message ?: "Unknown error"}",
                            "Error"
                        )
                    }

                    project.messageBus.syncPublisher(FindingsRefreshTopics.REFRESH_TOPIC)
                        .onRefreshRequested()
                }

            } catch (e: Exception) {
                LOG.error("Failed to reject finding forever for finding ${finding.id}", e)
                withContext(Dispatchers.Main) {
                    Messages.showErrorDialog(
                        project,
                        "Failed to reject finding ${finding.id} forever: ${e.message}",
                        "Rejection Failed"
                    )
                }
            }
        }
    }
}
