package com.arm.webservice.displayjsonfile.rest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.arm.webservice.displayjsonfile.constant.OpenAPIConstants.DISPLAY_JSON_FILE_RECORD_BY_RECORD;
import static com.arm.webservice.displayjsonfile.constant.OpenAPIConstants.DISPLAY_WHOLE_FILE_JSON_EXAMPLE;
import static com.arm.webservice.displayjsonfile.constant.OpenAPIConstants.FILE_READ_ERROR_MESSAGE;
import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

/**
 * REST controller to define endpoints.
 */
@RestController
public class JsonDataController {
    private final ObjectMapper objectMapper;
    private final Path displayFilePath;

    public JsonDataController(@Value("${display.file-path.json}") final Path displayFilePath) {
        this.objectMapper = new ObjectMapper();
        this.displayFilePath = validateFilePath(displayFilePath);
    }

    /**
     * Retrieves JSON file content & returns the content.
     *
     * @return whole file content as string.
     */
    @Operation(summary = "Get all boards", description = "Display all boards from JSON file")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully returns JSON file contents",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Board list example",
                                    value = DISPLAY_WHOLE_FILE_JSON_EXAMPLE
                            ))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Failed to load JSON content",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Error message",
                                    value = FILE_READ_ERROR_MESSAGE
                            )))
    })
    @GetMapping(value = "/boards", produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> getBoardsJsonFile() throws IOException {
        final File jsonFile = displayFilePath.toFile();
        // Read JSON into Object (can be Map, List, or specific DTO)
        return Mono.just(ResponseEntity.ok(objectMapper.readValue(jsonFile, new TypeReference<>() {
        })));
    }

    /**
     * Retrieves JSON file content & returns the content record by record.
     *
     * @return streams record one by one.
     */
    @Operation(summary = "Get all boards record by record (Streaming)", description = "Stream all boards from JSON file")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully streams JSON file contents, individual record",
                    content = @Content(
                            mediaType = APPLICATION_NDJSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Board records example",
                                    value = DISPLAY_JSON_FILE_RECORD_BY_RECORD))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Failed to load JSON content",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Error message",
                                    value = FILE_READ_ERROR_MESSAGE
                            )))
    })
    @GetMapping(value = "/boards/ndjson", produces = APPLICATION_NDJSON_VALUE)
    public Flux<Map<String, Object>> streamBoardsJsonFile() {
        return Flux.create(emitter -> {
            try {
                // Load the JSON file
                final InputStream inputStream = new FileInputStream(displayFilePath.toFile());
                final JsonFactory jsonFactory = new JsonFactory();
                final JsonParser parser = jsonFactory.createParser(inputStream);

                // Variables to store board count and unique vendors
                int totalBoards = 0;
                final Set<String> vendors = new LinkedHashSet<>();

                // Parse the JSON file
                while (!parser.isClosed()) {
                    final JsonToken token = parser.nextToken();

                    // Look for the "boards" array
                    if (FIELD_NAME.equals(token) && "boards".equals(parser.currentName())) {
                        parser.nextToken(); // Move to START_ARRAY

                        while (parser.nextToken() != END_ARRAY) {
                            // Parse each board as a map
                            final Map<String, Object> board = new LinkedHashMap<>();
                            while (parser.nextToken() != END_OBJECT) {
                                final String fieldName = parser.currentName();
                                parser.nextToken(); // Move to field value
                                final Object value = switch (parser.currentToken()) {
                                    case VALUE_STRING -> parser.getValueAsString();
                                    case VALUE_NUMBER_INT -> parser.getValueAsInt();
                                    case VALUE_TRUE -> true;
                                    case VALUE_FALSE -> false;
                                    default -> null;
                                };
                                board.put(fieldName, value);
                            }
                            emitter.next(board); // Emit each board

                            // Update total boards and vendor set
                            totalBoards++;
                            final String vendor = (String) board.get("vendor");
                            if (vendor != null) {
                                vendors.add(vendor);
                            }
                        }

                        // After processing all boards, emit metadata
                        final Map<String, Object> metadata = new LinkedHashMap<>();
                        metadata.put("total_vendors", vendors.size());
                        metadata.put("total_boards", totalBoards);
                        emitter.next(metadata); // Emit metadata
                        break; // Done processing boards array and metadata
                    }
                }
                parser.close();
                emitter.complete();
            } catch (Exception e) {
                emitter.error(new ResponseStatusException(INTERNAL_SERVER_ERROR, FILE_READ_ERROR_MESSAGE, e));
            }
        });
    }

    private Path validateFilePath(final Path displayFilePath) {
        if (!Files.exists(displayFilePath)) {
            throw new RuntimeException("Display file: %s doesn't exists!".formatted(displayFilePath));
        }
        return displayFilePath;
    }
}

