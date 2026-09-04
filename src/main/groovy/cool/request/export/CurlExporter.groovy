package cool.request.export

import cool.request.model.RequestData

/**
 * Generates cURL commands from request data
 */
class CurlExporter {

    /**
     * Generate a cURL command from request data
     */
    String generateCurl(RequestData requestData) {
        if (!requestData?.url) {
            return "curl: error - no URL provided"
        }

        def curl = new StringBuilder()
        curl << "curl"

        // HTTP method
        def method = (requestData.method ?: 'GET').toUpperCase()
        if (method != 'GET') {
            curl << " -X ${method}"
        }

        // URL - quote if contains spaces or special chars
        def url = requestData.url
        if (url.contains(' ') || url.contains('?') || url.contains('&')) {
            curl << " '${escapeSingleQuotes(url)}'"
        } else {
            curl << " ${url}"
        }

        // Headers
        if (requestData.headers) {
            requestData.headers.each { name, value ->
                curl << " -H '${escapeSingleQuotes("${name}: ${value}")}'"
            }
        }

        // Cookies
        if (requestData.cookies) {
            def cookieString = requestData.cookies.collect { "${it.key}=${it.value}" }.join('; ')
            curl << " -b '${escapeSingleQuotes(cookieString)}'"
        }

        // Body for POST/PUT/PATCH
        if (method in ['POST', 'PUT', 'PATCH'] && requestData.body) {
            def body = requestData.body
            // Escape single quotes in body
            def escapedBody = escapeSingleQuotes(body)
            curl << " -d '${escapedBody}'"
        }

        return curl.toString()
    }

    /**
     * Escape single quotes for shell safety
     */
    private String escapeSingleQuotes(String text) {
        if (!text) return ''
        // Replace ' with '\'' (end quote, escaped quote, start quote)
        return text.replace("'", "'\"'\"'")
    }
}
