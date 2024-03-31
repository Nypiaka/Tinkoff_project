package edu.java.controllers;

import edu.java.service.LinksService;
import edu.java.utils.Utils;
import edu.java.utils.dto.AddLinkRequest;
import edu.java.utils.dto.ApiErrorResponse;
import edu.java.utils.dto.RemoveLinkRequest;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinksController {
    private final LinksService linksService;
    private final Bucket bucket;

    @ApiResponse(responseCode = "200", description = "Ссылки успешно получены")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @GetMapping()
    public Mono<ResponseEntity<?>> getAllLinks(@RequestHeader("Tg-Chat-Id") Long id) {
        if (bucket.tryConsume(1)) {
            var result = linksService.getAllByChatId(id);
            return Mono.just(ResponseEntity.ok().body(result));
        }
        return Mono.just(Utils.errorRequest(HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    @ApiResponse(responseCode = "200", description = "Ссылка успешно добавлена")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @PostMapping()
    public Mono<ResponseEntity<ApiErrorResponse>> addLink(
        @RequestHeader("Tg-Chat-Id") Long id,
        @RequestBody AddLinkRequest request
    ) {
        if (bucket.tryConsume(1)) {
            if (linksService.saveLinkInChat(id, request.getLink().toString().toLowerCase())) {
                return Mono.just(ResponseEntity.ok().build());
            }
            return Mono.just(Utils.errorRequest(HttpStatus.BAD_REQUEST.value()));
        }
        return Mono.just(Utils.errorRequest(HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    @ApiResponse(responseCode = "200", description = "Ссылка успешно удалена")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @ApiResponse(responseCode = "404", description = "Ссылка не найдена")
    @DeleteMapping()
    public Mono<ResponseEntity<ApiErrorResponse>> removeLink(
        @RequestHeader("Tg-Chat-Id") Long id,
        @RequestBody RemoveLinkRequest request
    ) {
        if (bucket.tryConsume(1)) {
            if (!linksService.containsChatAndLink(id, request.getLink().toString().toLowerCase())) {
                return Mono.just(Utils.errorRequest(HttpStatus.NOT_FOUND.value()));
            }
            if (linksService.removeLinkFromChat(id, request.getLink().toString().toLowerCase())) {
                return Mono.just(ResponseEntity.ok().build());
            }
            return Mono.just(Utils.errorRequest(HttpStatus.BAD_REQUEST.value()));
        }
        return Mono.just(Utils.errorRequest(HttpStatus.TOO_MANY_REQUESTS.value()));
    }
}
