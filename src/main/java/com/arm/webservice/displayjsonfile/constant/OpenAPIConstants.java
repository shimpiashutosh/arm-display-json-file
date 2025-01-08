package com.arm.webservice.displayjsonfile.constant;

/**
 * Open API response examples constant
 */
public interface OpenAPIConstants {
    String DISPLAY_WHOLE_FILE_JSON_EXAMPLE = """
            {
              "boards": [
                {
                  "name": "Board-1",
                  "vendor": "Edge Devices",
                  "core": "Cortex-M0+",
                  "has_wifi": true
                },
                {
                  "name": "Board-2",
                  "vendor": "Tech Corp.",
                  "core": "Cortex-M4",
                  "has_wifi": false
                }
              ],
              "_metadata": {
                "total_boards": 2,
                "total_vendors": 2
              }
            }
            """;

    String DISPLAY_JSON_FILE_RECORD_BY_RECORD = """
            {"name":"B7-400X","vendor":"Boards R Us","core":"Cortex-M7","has_wifi":true}
            {"name":"D4-200S","vendor":"Boards R Us","core":"Cortex-M4","has_wifi":false}
            {"name":"D5-200S","vendor":"Boards R Us","core":"Cortex-V4","has_wifi":false}
            {"name":"D6-200S","vendor":"Tech Corp.","core":"Cortex-B4","has_wifi":false}
            {"name":"Low_Power","vendor":"Tech Corp.","core":"Cortex-M0+","has_wifi":false}
            {"total_vendors":2,"total_boards":5}
            """;

    String FILE_READ_ERROR_MESSAGE = """
            {
              "errorMessage": "Unable to process JSON file"
            }
            """;
}
