package com.test_task.n_minimal.controller;

import com.test_task.n_minimal.service.NMinimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер для работы с сервисом.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Find N Minimal", description = "Finds N-th minimal value in local file")
public class NMinimalController {

    private final NMinimalService service;

    public NMinimalController(NMinimalService service) {
        this.service = service;
    }

    /**
     * Отдаёт N число из локального файла, переданного в первом параметре.
     * Значения берутся из 1 столбца 1 листа, и сортируются по возрастанию.
     * пример запроса: http://localhost:8080/api/find-nth-min?fileLink=path/to/file&N=5
     *
     * @param fileLink ссылка на локальный файл
     * @param N требуемое минимальное число
     * @return значение N-го минимального числа
     */
    @Operation(summary = "Gets N minimal value from local file.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode =
            "400", description = "Bad request - link is incorrect, or file no found"), @ApiResponse(responseCode =
            "404", description = "File link or N are not found")})
    @GetMapping("/find-nth-min")
    public ResponseEntity<Long> getNthMinimal(@RequestParam final String fileLink, @RequestParam final String N) {

        Long nthMinimal = service.getNthMinimal(fileLink.trim(), N.trim());

        return ResponseEntity.ok(nthMinimal);
    }
}
