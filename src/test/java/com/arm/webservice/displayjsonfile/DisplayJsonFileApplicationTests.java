package com.arm.webservice.displayjsonfile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.copy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToApplicationContext;

@TestPropertySource("classpath:/application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DisplayJsonFileApplicationTests {
    private static Path tempDir;
    private static final String testFile = "combined-board-file.json";

    @Autowired
    private ApplicationContext applicationContext;

    // @BeforeAll or any other setup method where you can set the temp directory
    @BeforeAll
    static void setupTempDir(final @TempDir Path tempDirectory) {
        // Initialize the tempDir field
        tempDir = tempDirectory;
    }

    //Set properties dynamically
    @DynamicPropertySource
    public static void registerPgProperties(final DynamicPropertyRegistry registry) throws IOException {
        final File testFolder = ResourceUtils.getFile("classpath:test-data");
        final Path destinationFile = tempDir.resolve(testFile);

        copy(testFolder.toPath().resolve(testFile), destinationFile);
        registry.add("display.file-path.json", destinationFile::toString);
    }

    @DisplayName("When API is triggered returns JSON file contents as whole")
    @Test
    public void testBoardsAPI_WhenAccessTheAPI_ReturnsJSONFileContents() {
        // Initialize WebTestClient with the random port from Spring Boot
        final WebTestClient webTestClient = bindToApplicationContext(this.applicationContext).build();

        // Make a GET request to the /api/boards/by-line endpoint
        webTestClient.get().uri("/boards")
                .accept(APPLICATION_JSON) // Set accept header to APPLICATION_JSON
                .exchange()
                .expectStatus().isOk() // Verify that the status code is 200
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON) // Verify the content type
                .expectBody() // Check the body of the response
                .consumeWith(response -> {
                    // Assert that the body contains the expected data
                    final String body = new String(response.getResponseBody());

                    // Read the files using ObjectMapper
                    final ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        final JsonNode mergedJson = objectMapper.readTree(body);
                        final JsonNode expectedJson = objectMapper.readTree(tempDir.resolve(testFile).toFile());

                        // Compare the JSON contents
                        assertEquals(expectedJson, mergedJson, "The merged JSON does not match the expected output");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}

