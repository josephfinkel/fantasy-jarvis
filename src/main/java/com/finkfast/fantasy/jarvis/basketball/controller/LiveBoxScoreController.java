package com.finkfast.fantasy.jarvis.basketball.controller;

import com.finkfast.fantasy.jarvis.basketball.scheduler.LiveStatsScheduler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("schedule")
public class LiveBoxScoreController {

    final LiveStatsScheduler liveStatsScheduler;

    public LiveBoxScoreController(LiveStatsScheduler liveStatsScheduler) {
        this.liveStatsScheduler = liveStatsScheduler;
    }

    /*@GetMapping(produces = "application/json")
    public JsonObject fetchBoxScores(@RequestParam(defaultValue = "60") int duration,
                                     @RequestParam(defaultValue = "300") int period) {
        JsonObject response = new JsonObject();
        LocalDateTime expirationTime = liveStatsScheduler.scheduleBoxScoreFetch(duration, period);
        //if successful 202
        //if already in flight 409
        //if error 500
        return null;
    }*/
}
