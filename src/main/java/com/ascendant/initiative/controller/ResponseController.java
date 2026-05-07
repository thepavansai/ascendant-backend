package com.ascendant.initiative.controller;

import com.ascendant.initiative.dto.evaluation.EvaluationResultDto;
import com.ascendant.initiative.dto.response.ResponseSubmitDto;
import com.ascendant.initiative.dto.response.ResponseSubmitRequest;
import com.ascendant.initiative.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/responses")
@RequiredArgsConstructor
public class ResponseController {

    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<ResponseSubmitDto> submit(
            @Valid @RequestBody ResponseSubmitRequest req) {
        ResponseSubmitDto result = evaluationService.submit(req);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @GetMapping("/{id}/evaluation")
    public ResponseEntity<EvaluationResultDto> getEvaluation(@PathVariable UUID id) {
        return ResponseEntity.ok(evaluationService.getResult(id));
    }
}
