package com.tomas.market.pulse.alerts.repositories;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import com.tomas.market.pulse.alerts.model.MarketType;
import com.tomas.market.pulse.alerts.model.entities.FinancialInstrumentEntity;
import com.tomas.market.pulse.alerts.model.entities.SubscriptionEntity;

@TestPropertySource(locations = "classpath:application-test.properties")
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FinancialInstrumentEntityRepositoryTest {
  @Autowired
  private FinancialInstrumentEntityRepository financialInstrumentRepo;

  @Autowired
  private SubscriptionEntityRepository subscriptionRepo;

  private FinancialInstrumentEntity f1;
  private FinancialInstrumentEntity f2;

  @BeforeAll
  void init() {
    f1 = FinancialInstrumentEntity.builder()
        .symbol("s")
        .name("n")
        .marketType(MarketType.CRYPTO)
        .build();
    financialInstrumentRepo.save(f1);

    f2 = FinancialInstrumentEntity.builder()
        .symbol("s2")
        .name("n2")
        .marketType(MarketType.STOCK)
        .build();
    financialInstrumentRepo.save(f2);
  }

  @BeforeEach
  void setUp() {
    subscriptionRepo.deleteAll();
  }

  @AfterEach
  void tearDown() {
    subscriptionRepo.deleteAll();
  }

  @AfterAll
  void cleanup() {
    financialInstrumentRepo.deleteAll();
  }

  @Test
  void shouldRetrieveEmptyListWhenFinancialInstrumentsDoNotHaveSubscriptions() {
    List<FinancialInstrumentEntity> result = financialInstrumentRepo.findAllWithRelatedSubscriptions();

    assertEquals(0, result.size());
  }

  @Test
  void shouldRetrieveOneElementListWhenOnlyOneFinancialInstrumentHasSubscriptions(){
    SubscriptionEntity s = SubscriptionEntity.builder()
        .financialInstrument(f1)
        .email("email@gmail.com")
        .lastReferencePrice(10)
        .originalPrice(10)
        .lowerThreshold(10)
        .upperThreshold(10)
        .build();

    subscriptionRepo.save(s);

    List<FinancialInstrumentEntity> result = financialInstrumentRepo.findAllWithRelatedSubscriptions();

    assertEquals(1, result.size());
    assertEquals(f1, result.get(0));
  }

  @Test
  void shouldRetrieveTwoElementListWhenOnlyTwoFinancialInstrumentHaveSubscriptions(){
    SubscriptionEntity s1 = SubscriptionEntity.builder()
        .financialInstrument(f1)
        .email("email@gmail.com")
        .lastReferencePrice(10)
        .originalPrice(10)
        .lowerThreshold(10)
        .upperThreshold(10)
        .build();

    subscriptionRepo.save(s1);

    SubscriptionEntity s2 = SubscriptionEntity.builder()
        .financialInstrument(f2)
        .email("email@gmail.com")
        .lastReferencePrice(10)
        .originalPrice(10)
        .lowerThreshold(10)
        .upperThreshold(10)
        .build();

    subscriptionRepo.save(s2);

    List<FinancialInstrumentEntity> result = financialInstrumentRepo.findAllWithRelatedSubscriptions();

    assertEquals(2, result.size());
    assertEquals(f1, result.get(0));
    assertEquals(f2, result.get(1));
  }

  @Test
  void shouldNotRepeatEntitiesWhenTheyHaveMoreThanOneSubscription(){
    SubscriptionEntity s1 = SubscriptionEntity.builder()
        .financialInstrument(f1)
        .email("email@gmail.com")
        .lastReferencePrice(10)
        .originalPrice(10)
        .lowerThreshold(10)
        .upperThreshold(10)
        .build();

    subscriptionRepo.save(s1);

    SubscriptionEntity s2 = SubscriptionEntity.builder()
        .financialInstrument(f1)
        .email("email@gmail.com")
        .lastReferencePrice(10)
        .originalPrice(10)
        .lowerThreshold(10)
        .upperThreshold(10)
        .build();

    subscriptionRepo.save(s2);

    List<FinancialInstrumentEntity> result = financialInstrumentRepo.findAllWithRelatedSubscriptions();

    assertEquals(1, result.size());
    assertEquals(f1, result.get(0));
  }
}
