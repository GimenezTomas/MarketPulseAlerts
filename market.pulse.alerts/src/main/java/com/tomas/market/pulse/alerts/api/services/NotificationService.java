package com.tomas.market.pulse.alerts.api.services;

import java.util.Map;

public interface NotificationService {
  void notifyUsersByEmail(Map<String, String> emailsAndMessages);
}
