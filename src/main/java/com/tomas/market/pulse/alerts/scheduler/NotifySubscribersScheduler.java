package com.tomas.market.pulse.alerts.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tomas.market.pulse.alerts.api.services.MarketHubService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotifySubscribersScheduler {
  private final MarketHubService marketHubService;

  @Scheduled(fixedRateString = "${scheduler.notifySubscribers.fixedRate}")
  void executeTask(){
    log.info("Starting scheduled task: NotifySubscribers at {}", System.currentTimeMillis());

    try {
      marketHubService.notifySubscribers();
      log.info("Scheduled task: NotifySubscribers completed successfully at {}", System.currentTimeMillis());
    } catch (Exception e) {
      log.error("Error occurred while executing scheduled task: NotifySubscribers", e);
    }
  }
} //todo actualizar el current price
