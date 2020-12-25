package com.finkfast.fantasy.basketball.FantasyBasketballAssistant.scheduler;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LiveStatsScheduler {

    ScheduledExecutorService executorService;
    LocalDateTime expirationTime;

    public LocalDateTime scheduleBoxScoreFetch(int duration, int period) {
        if(!executorService.isShutdown()) {
            throw new IllegalStateException("Scraping is already in flight");
        }
        expirationTime = LocalDateTime.now().plusSeconds(duration);
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::fetchBoxScores,0, period, TimeUnit.SECONDS);
        return expirationTime;
    }

    private void fetchBoxScores() {
        if(expirationTime.isBefore(LocalDateTime.now())) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        //Fetch Box Scores
    }
}
