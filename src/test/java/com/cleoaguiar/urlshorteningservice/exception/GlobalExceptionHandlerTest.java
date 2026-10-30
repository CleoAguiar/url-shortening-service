package com.cleoaguiar.urlshorteningservice.exception;

import com.cleoaguiar.urlshorteningservice.service.ShortUrlService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({
        GlobalExceptionHandler.class,
        GlobalExceptionHandlerTest.TestController.class
})
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void shouldReturnNotFoundWhenShortUrlDoesNotExist() throws  Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Short URL not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "url": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("url: URL must not be blank"))
                .andExpect(jsonPath("$.path").value("/test/validation"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMalformed() throws  Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "url": "https://example.com"
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.path").value("/test/validation"));
    }

    @Test
    void shouldReturnInternalServerErrorWithoutExposingInternalDetails() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/test/internal-error"))
                .andExpect(content().string(
                        not(containsString("Sensitive internal error details"))
                ));
    }

    record TestRequest(
            @NotBlank(message = "URL must not be blank")
            String url
    ) {
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        ResponseEntity<Void> notFound() {
            throw  new ShortUrlNotFoundException("Short URL not found");
        }

        @PostMapping("/test/validation")
        ResponseEntity<Void> validation(
                @Valid @RequestBody TestRequest request
        ){
            return  ResponseEntity.ok().build();
        }

        @GetMapping("/test/internal-error")
        ResponseEntity<Void> internalError() {
            throw  new RuntimeException("Sensitive internal error details");
        }
    }
}
