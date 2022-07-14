/*
 Licensed to Diennea S.r.l. under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. Diennea S.r.l. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
package blazingcache;

import java.nio.charset.StandardCharsets;
import blazingcache.client.CacheClient;
import blazingcache.network.ServerHostData;
import blazingcache.network.netty.NettyCacheServerLocator;
import blazingcache.server.CacheServer;
import blazingcache.utils.RawString;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author enrico.olivelli
 */
public class DisconnectedClientTest {

    @Test
    public void basicTest() throws Exception {
        byte[] data = "testdata".getBytes(StandardCharsets.UTF_8);

        ServerHostData serverHostData = new ServerHostData("localhost", 1234, "test", false, null);
        try (CacheServer cacheServer = new CacheServer("ciao", serverHostData)) {
            cacheServer.start();
            try (CacheClient client1 = new CacheClient("theClient1", "ciao", new NettyCacheServerLocator(serverHostData));) {
                client1.start();
                assertTrue(client1.waitForConnection(10000));

                client1.put("pippo", data, 0);
                assertEquals(1,cacheServer.getCacheStatus().getClientsForKey(RawString.of("pippo")).size());
                Assert.assertArrayEquals(data, client1.get("pippo").getSerializedData());
                client1.disconnect();
                assertTrue(client1.waitForDisconnection(60000));
                
                assertNull(client1.get("pippo"));
                for (int i = 0; i < 100; i++) {
                    if (cacheServer.getCacheStatus().getClientsForKey(RawString.of("pippo")).isEmpty()) {
                        break;
                    }
                    Thread.sleep(100);
                }
                assertEquals(0,cacheServer.getCacheStatus().getClientsForKey(RawString.of("pippo")).size());

            }
        }
    }
}
