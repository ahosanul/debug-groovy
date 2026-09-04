package cool.request.util

/**
 * Utility class for Cool Request plugin
 * Provides common helper methods used throughout the plugin
 */
class CoolRequestUtils {

    /**
     * Convert a Grails controller class name to controller name
     * e.g., UserController -> user
     */
    static String normalizeControllerName(String className) {
        if (!className) return null
        def name = className
        if (name.endsWith('Controller')) {
            name = name[0..-11]
        }
        return name[0].toLowerCase() + (name.length() > 1 ? name[1..-1] : '')
    }

    /**
     * Convert controller name to class name
     * e.g., user -> UserController
     */
    static String denormalizeControllerName(String controllerName) {
        if (!controllerName) return null
        def capitalized = controllerName[0].toUpperCase() + (controllerName.length() > 1 ? controllerName[1..-1] : '')
        return "${capitalized}Controller"
    }

    /**
     * Check if a type is a primitive or common type
     */
    static boolean isSimpleType(Class type) {
        if (type == null) return false
        return type.isPrimitive() ||
               type == String ||
               type == Boolean ||
               type == Character ||
               Number.isAssignableFrom(type) ||
               type == Date ||
               type == Calendar ||
               type == BigDecimal ||
               type == BigInteger
    }

    /**
     * Generate an example value for a given type
     */
    static Object generateExampleValue(String typeName) {
        switch (typeName) {
            case 'String':
                return ''
            case 'Integer':
            case 'int':
                return 1
            case 'Long':
            case 'long':
                return 1L
            case 'Boolean':
            case 'boolean':
                return true
            case 'Double':
            case 'double':
                return 1.0
            case 'Float':
            case 'float':
                return 1.0F
            case 'Short':
            case 'short':
                return (short) 1
            case 'Byte':
            case 'byte':
                return (byte) 1
            case 'BigDecimal':
                return new BigDecimal(1)
            case 'Date':
                return new Date()
            default:
                return null
        }
    }

    /**
     * Extract path variables from a URL pattern
     * e.g., /api/users/$id -> ['id']
     */
    static List<String> extractPathVariables(String urlPattern) {
        if (!urlPattern) return []
        def matcher = urlPattern =~ /\$([a-zA-Z_][a-zA-Z0-9_]*)/
        def variables = []
        while (matcher.find()) {
            variables << matcher.group(1)
        }
        return variables
    }

    /**
     * Replace path variables in URL with values
     * e.g., /api/users/{id} + [id: 123] -> /api/users/123
     */
    static String replacePathVariables(String urlPattern, Map variables) {
        if (!urlPattern || !variables) return urlPattern
        def result = urlPattern
        variables.each { key, value ->
            result = result.replaceAll("\\{${key}\\}", value?.toString() ?: '')
            result = result.replaceAll("\\\$${key}", value?.toString() ?: '')
        }
        return result
    }

    /**
     * Check if running in production environment
     */
    static boolean isProduction() {
        def env = System.getProperty('grails.env') ?: System.getenv('GRAILS_ENV')
        return env == 'production' || env == 'prod'
    }

    /**
     * Safely truncate a string to max length
     */
    static String truncate(String str, int maxLength) {
        if (!str || str.length() <= maxLength) return str
        return str[0..maxLength - 3] + '...'
    }

    /**
     * Format bytes to human readable size
     */
    static String formatBytes(long bytes) {
        if (bytes < 1024) return "${bytes} B"
        if (bytes < 1024 * 1024) return "${(bytes / 1024).toFixed(2)} KB"
        if (bytes < 1024 * 1024 * 1024) return "${(bytes / (1024 * 1024)).toFixed(2)} MB"
        return "${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB"
    }

    /**
     * Format milliseconds to human readable duration
     */
    static String formatDuration(long ms) {
        if (ms < 1000) return "${ms} ms"
        if (ms < 60000) return "${(ms / 1000.0).toFixed(2)} s"
        return "${(ms / 60000.0).toFixed(2)} min"
    }

    /**
     * Escape special characters for cURL command
     */
    static String escapeForShell(String str) {
        if (!str) return ''
        return str.replace("'", "'\"'\"'")
    }

    /**
     * Parse content type to get media type
     */
    static String parseMediaType(String contentType) {
        if (!contentType) return 'application/octet-stream'
        return contentType.split(';')[0].trim()
    }

    /**
     * Check if content type is JSON
     */
    static boolean isJson(String contentType) {
        if (!contentType) return false
        def mediaType = parseMediaType(contentType)
        return mediaType.contains('json')
    }

    /**
     * Check if content type is XML
     */
    static boolean isXml(String contentType) {
        if (!contentType) return false
        def mediaType = parseMediaType(contentType)
        return mediaType.contains('xml')
    }

    /**
     * Check if content type is HTML
     */
    static boolean isHtml(String contentType) {
        if (!contentType) return false
        def mediaType = parseMediaType(contentType)
        return mediaType.contains('html')
    }

    /**
     * Get file extension from content type
     */
    static String getExtensionFromContentType(String contentType) {
        if (!contentType) return '.bin'
        switch (parseMediaType(contentType)) {
            case 'application/json':
                return '.json'
            case 'application/xml':
            case 'text/xml':
                return '.xml'
            case 'text/html':
                return '.html'
            case 'text/plain':
                return '.txt'
            case 'image/jpeg':
                return '.jpg'
            case 'image/png':
                return '.png'
            case 'image/gif':
                return '.gif'
            case 'application/pdf':
                return '.pdf'
            default:
                return '.bin'
        }
    }
}
