package com.example.pixelgame.server;

import static com.example.pixelgame.online.OnlineContract.*;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestController
@RequestMapping("/api")
public class RecordController {
    private final RecordService service;
    public RecordController(RecordService service) { this.service = service; }
    @GetMapping("/health") public Map<String,String> health() { return Map.of("status", "ok", "service", "deadlight-records"); }
    @PostMapping("/players") @ResponseStatus(HttpStatus.CREATED)
    public PlayerCredentials register() { return service.register(); }
    @PostMapping("/players/recover")
    public PlayerCredentials recover(@RequestBody PlayerCredentials identity) { return service.recover(identity); }
    @PostMapping("/runs") public SubmissionResult submit(@RequestHeader(value="Authorization", required=false) String auth, @RequestBody RunSubmission run) {
        return service.submit(service.authenticate(auth), run);
    }
    @GetMapping("/achievements") public List<Achievement> achievements(@RequestHeader(value="Authorization", required=false) String auth) {
        return service.achievements(service.authenticate(auth));
    }
    @GetMapping("/leaderboard") public Board leaderboard(@RequestHeader(value="Authorization", required=false) String auth,
            @RequestParam(name="rulesVersion", defaultValue="11") int rulesVersion,
            @RequestParam(name="startStage", defaultValue="1") int startStage,
            @RequestParam(name="limit", defaultValue="10") int limit) {
        return service.leaderboard(auth == null ? null : service.authenticate(auth), rulesVersion, startStage, limit);
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> error(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new ApiError(e.getReason()));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> malformed(Exception e) {
        return ResponseEntity.badRequest().body(new ApiError("요청 내용을 확인해 주세요."));
    }
}
