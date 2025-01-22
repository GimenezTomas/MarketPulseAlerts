package com.tomas.market.pulse.alerts.api.services;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService{

  @Override
  public void notifyUsersByEmail(Map<String, String> emailsAndMessages) {
    emailsAndMessages.forEach((email, message) ->
      log.info("Sending notification via email to {} with content: {}", email, message)
    );
  }
}
