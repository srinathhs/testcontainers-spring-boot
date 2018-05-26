/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Playtika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.playtika.test.elasticsearch;

import org.slf4j.Logger;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.HostPortWaitStrategy;
import org.testcontainers.containers.wait.WaitAllStrategy;
import org.testcontainers.containers.wait.WaitStrategy;

import com.playtika.test.elasticsearch.rest.CreateIndex;
import com.playtika.test.elasticsearch.rest.WaitForGreenStatus;

import static com.playtika.test.common.utils.ContainerUtils.DEFAULT_CONTAINER_WAIT_DURATION;
import static com.playtika.test.common.utils.ContainerUtils.containerLogsConsumer;

class ElasticSearchContainerFactory {

    static GenericContainer create(ElasticSearchProperties properties, Logger containerLogger) {
        return new FixedHostPortGenericContainer(properties.dockerImage)
            .withExposedPorts(properties.httpPort, properties.transportPort)
            .withEnv("cluster.name", properties.getClusterName())
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms" + properties.getClusterRamMb() + "m -Xmx" + properties.getClusterRamMb() + "m")
            .withLogConsumer(containerLogsConsumer(containerLogger))
            .waitingFor(getCompositeWaitStrategy(properties));
    }

    private static WaitStrategy getCompositeWaitStrategy(ElasticSearchProperties properties) {
        WaitAllStrategy strategy = new WaitAllStrategy()
            .withStrategy(new HostPortWaitStrategy());
        properties.indices.forEach(index -> strategy.withStrategy(new CreateIndex(properties, index)));
        return strategy
            .withStrategy(new WaitForGreenStatus(properties))
            .withStartupTimeout(DEFAULT_CONTAINER_WAIT_DURATION);
    }
}