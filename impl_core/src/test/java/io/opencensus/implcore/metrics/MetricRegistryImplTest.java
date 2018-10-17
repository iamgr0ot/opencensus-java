/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package io.opencensus.implcore.metrics;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opencensus.testing.common.TestClock;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricRegistryImpl}. */
@RunWith(JUnit4.class)
public class MetricRegistryImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("key", "key description"));
  private static final List<LabelValue> LABEL_VALUES =
      Collections.singletonList(LabelValue.create("value"));

  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private final TestClock testClock = TestClock.create(TEST_TIME);
  private final MetricRegistryImpl metricRegistry = new MetricRegistryImpl(testClock);

  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(NAME, DESCRIPTION, UNIT, Type.GAUGE_INT64, LABEL_KEY);

  @Test
  public void addLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addLongGauge(null, DESCRIPTION, UNIT, LABEL_KEY);
  }

  @Test
  public void addLongGauge_NullDescription() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    metricRegistry.addLongGauge(NAME, null, UNIT, LABEL_KEY);
  }

  @Test
  public void addLongGauge_NullUnit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    metricRegistry.addLongGauge(NAME, DESCRIPTION, null, LABEL_KEY);
  }

  @Test
  public void addLongGauge_NullLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKeys");
    metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, null);
  }

  @Test
  public void addLongGauge_WithNullElement() {
    List<LabelKey> labelKeys = Collections.singletonList(null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKey element should not be null.");
    metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, labelKeys);
  }

  @Test
  public void addLongGauge_GetMetrics() {
    LongGauge longGauge = metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, LABEL_KEY);
    longGauge.getOrCreateTimeSeries(LABEL_VALUES);

    Collection<Metric> metricCollections = metricRegistry.getMetricProducer().getMetrics();
    assertThat(metricCollections.size()).isEqualTo(1);
    assertThat(metricCollections)
        .containsExactly(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(0), TEST_TIME), null)));
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(metricRegistry.getMetricProducer().getMetrics()).isEmpty();
  }

  @Test
  public void checkInstanceOf() {
    LongGauge longGauge = metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, LABEL_KEY);
    assertThat(longGauge).isInstanceOf(LongGaugeImpl.class);
  }
}
