/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.itau.journey.simple;

import org.junit.Rule;
import org.junit.rules.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

/*@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationContextConfiguration.class }, properties = {
    "org.camunda.bpm.spring.boot.starter.example.simple.SimpleApplication.exitWhenFinished=false" })*/
public class SimpleApplicationTest {

  @Rule
  public Timeout timeout = new Timeout(10000);

  @Autowired
  private ApplicationContextConfiguration application;

  /*@Test*/
  public void would_fail_if_process_not_completed_after_10_seconds() throws InterruptedException {
    while (!application.processApplicationStopped && !application.isProcessInstanceFinished()) {
      Thread.sleep(500L);
    }
  }
}
