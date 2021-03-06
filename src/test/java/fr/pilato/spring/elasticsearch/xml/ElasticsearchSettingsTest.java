/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.spring.elasticsearch.xml;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ElasticsearchSettingsTest {
	static protected ConfigurableApplicationContext ctx;
	
	@BeforeClass
	static public void setup() {
		ctx = new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-settings-test-context.xml");
	}
	
	@AfterClass
	static public void tearDown() {
		if (ctx != null) {
			ctx.close();
		}
	}
	
	@Test
	public void test_transport_client() {
		Client client = ctx.getBean("esClient", Client.class);
		assertNotNull("Client must not be null...", client);

		Client client2 = ctx.getBean("esClient2", Client.class);
		assertNotNull("Client2 must not be null...", client2);
		// We wait a while for connection to the cluster (1s should be enough)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

        // We test how many shards and replica we have
        ClusterStateResponse response = client.admin().cluster().prepareState().execute().actionGet();
        assertEquals(1, response.getState().getMetaData().getIndices().get("twitter").getNumberOfShards());

        // We don't expect the number of replicas to be 4 as we won't merge _update_settings.json
        // See #31: https://github.com/dadoonet/spring-elasticsearch/issues/31
        assertEquals(0, response.getState().getMetaData().getIndices().get("twitter").getNumberOfReplicas());

    }
}
