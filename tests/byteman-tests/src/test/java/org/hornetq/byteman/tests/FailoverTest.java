/*
* JBoss, Home of Professional Open Source.
* Copyright 2010, Red Hat, Inc., and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.hornetq.byteman.tests;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.HornetQTransactionOutcomeUnknownException;
import org.hornetq.api.core.HornetQTransactionRolledBackException;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.client.impl.ClientSessionFactoryInternal;
import org.hornetq.core.server.Bindable;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.Queue;
import org.hornetq.tests.integration.cluster.failover.FailoverTestBase;
import org.hornetq.tests.integration.cluster.util.TestableServer;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMRules;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         4/18/13
 */
@RunWith(BMUnitRunner.class)
public class FailoverTest extends FailoverTestBase
{
   private ServerLocator locator;
   private ClientSessionFactoryInternal sf;
   public static TestableServer serverToStop;
   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      locator = getServerLocator();
   }

   @Test
   @BMRules
   (
         rules =
               {
                     @BMRule
                           (
                                 name = "trace clientsessionimpl commit",
                                 targetClass = "org.hornetq.core.client.impl.ClientSessionImpl",
                                 targetMethod = "commit",
                                 targetLocation = "ENTRY",
                                 action = "org.hornetq.byteman.tests.FailoverTest.serverToStop.getServer().stop(true)"
                           )
               }
   )
   public void testFailoverOnCommit() throws Exception
   {
      setUp();
      try
      {
         serverToStop = liveServer;
         locator = getServerLocator();
         locator.setFailoverOnInitialConnection(true);
         createSessionFactory();
         ClientSession session = createSessionAndQueue();

         ClientProducer producer = addClientProducer(session.createProducer(FailoverTestBase.ADDRESS));

         sendMessages(session, producer, 10);
         try
         {
            session.commit();
            fail("should have thrown an exception");
         }
         catch (HornetQTransactionOutcomeUnknownException e)
         {
            //pass
         }
         sendMessages(session, producer, 10);
         session.commit();
         Queue bindable = (Queue) backupServer.getServer().getPostOffice().getBinding(FailoverTestBase.ADDRESS).getBindable();
         assertTrue(bindable.getMessageCount() == 10);
      }
      finally
      {
         tearDown();
      }
   }

   @Test
      @BMRules
      (
            rules =
                  {
                        @BMRule
                              (
                                    name = "trace clientsessionimpl commit",
                                    targetClass = "org.hornetq.core.client.impl.ClientSessionImpl",
                                    targetMethod = "commit",
                                    targetLocation = "ENTRY",
                                    action = "org.hornetq.byteman.tests.FailoverTest.serverToStop.getServer().stop(true)"
                              )
                  }
      )
   public void testFailoverOnReceiveCommit() throws Exception
   {
      setUp();
      try
      {
         serverToStop = liveServer;
         locator = getServerLocator();
         locator.setFailoverOnInitialConnection(true);
         createSessionFactory();
         ClientSession session = createSessionAndQueue();

         ClientSession sendSession = createSession(sf, true, true);

         ClientProducer producer = addClientProducer(sendSession.createProducer(FailoverTestBase.ADDRESS));

         sendMessages(sendSession, producer, 10);

         ClientConsumer consumer = session.createConsumer(FailoverTestBase.ADDRESS);
         session.start();
         for(int i = 0; i < 10; i++)
         {
            ClientMessage m = consumer.receive(500);
            assertNotNull(m);
            m.acknowledge();
         }
         try
         {
            session.commit();
            fail("should have thrown an exception");
         }
         catch (HornetQTransactionOutcomeUnknownException e)
         {
            //pass
         }
         catch(HornetQTransactionRolledBackException e1)
         {
            //pass
         }
         Queue bindable = (Queue) backupServer.getServer().getPostOffice().getBinding(FailoverTestBase.ADDRESS).getBindable();
         assertTrue("messager count = " + bindable.getMessageCount(), bindable.getMessageCount() == 10);
      }
      finally
      {
         tearDown();
      }
   }

   @Override
   protected TransportConfiguration getAcceptorTransportConfiguration(final boolean live)
   {
      return getNettyAcceptorTransportConfiguration(live);
   }

   @Override
   protected TransportConfiguration getConnectorTransportConfiguration(final boolean live)
   {
      return getNettyConnectorTransportConfiguration(live);
   }

   private ClientSession createSessionAndQueue() throws Exception, HornetQException
   {
      ClientSession session = createSession(sf, false, false);

      session.createQueue(FailoverTestBase.ADDRESS, FailoverTestBase.ADDRESS, null, true);
      return session;
   }
   protected ClientSession
               createSession(ClientSessionFactory sf1, boolean autoCommitSends, boolean autoCommitAcks) throws Exception
      {
         return addClientSession(sf1.createSession(autoCommitSends, autoCommitAcks));
      }
   private void createSessionFactory() throws Exception
   {
      locator.setBlockOnNonDurableSend(true);
      locator.setBlockOnDurableSend(true);
      locator.setReconnectAttempts(-1);

      sf = createSessionFactoryAndWaitForTopology(locator, 2);
   }
}
