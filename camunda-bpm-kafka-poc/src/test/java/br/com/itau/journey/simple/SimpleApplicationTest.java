package br.com.itau.journey.simple;

import br.com.itau.journey.configuration.ApplicationContextConfiguration;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleApplicationTest {

  @Rule
  public Timeout timeout = new Timeout(10000);

  @Autowired
  private ApplicationContextConfiguration application;
}
