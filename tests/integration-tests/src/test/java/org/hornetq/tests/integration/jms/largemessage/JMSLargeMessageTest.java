/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.tests.integration.jms.largemessage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageNotWriteableException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import junit.framework.Assert;

import org.hornetq.tests.util.JMSTestBase;
import org.hornetq.tests.util.UnitTestCase;
import org.hornetq.utils.UUIDGenerator;

/**
 *
 * @author <a href="mailto:clebert.suconic@feodorov.com">Clebert Suconic</a>
 * @version <tt>$Revision: 6220 $</tt>
 *
 * $Id: MessageHeaderTest.java 6220 2009-03-30 19:38:11Z timfox $
 */
public class JMSLargeMessageTest extends JMSTestBase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   Queue queue1;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   @Override
   protected boolean usePersistence()
   {
      return true;
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      queue1 = createQueue("queue1");
   }

   @Override
   protected void tearDown() throws Exception
   {
      queue1 = null;
      super.tearDown();
   }

   public void testSimpleLargeMessage() throws Exception
   {

      conn = cf.createConnection();

      Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageProducer prod = session.createProducer(queue1);

      BytesMessage m = session.createBytesMessage();

      m.setObjectProperty("JMS_HQ_InputStream", UnitTestCase.createFakeLargeStream(1024 * 1024));

      prod.send(m);

      conn.close();

      conn = cf.createConnection();

      session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer cons = session.createConsumer(queue1);

      conn.start();

      BytesMessage rm = (BytesMessage)cons.receive(10000);

      byte data[] = new byte[1024];

      System.out.println("Message = " + rm);

      for (int i = 0; i < 1024 * 1024; i += 1024)
      {
         int numberOfBytes = rm.readBytes(data);
         Assert.assertEquals(1024, numberOfBytes);
         for (int j = 0; j < 1024; j++)
         {
            Assert.assertEquals(UnitTestCase.getSamplebyte(i + j), data[j]);
         }
      }

      Assert.assertNotNull(rm);
   }

   public void testSimpleLargeMessage2() throws Exception
   {
      conn = cf.createConnection();

      Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageProducer prod = session.createProducer(queue1);

      BytesMessage m = session.createBytesMessage();

      m.setObjectProperty("JMS_HQ_InputStream", UnitTestCase.createFakeLargeStream(10));

      prod.send(m);

      conn.close();

      conn = cf.createConnection();

      session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer cons = session.createConsumer(queue1);

      conn.start();

      BytesMessage rm = (BytesMessage)cons.receive(10000);

      byte data[] = new byte[1024];

      System.out.println("Message = " + rm);

      int numberOfBytes = rm.readBytes(data);
      Assert.assertEquals(10, numberOfBytes);
      for (int j = 0; j < numberOfBytes; j++)
      {
         Assert.assertEquals(UnitTestCase.getSamplebyte(j), data[j]);
      }

      Assert.assertNotNull(rm);
   }

   public void testExceptionsOnSettingNonStreaming() throws Exception
   {
      conn = cf.createConnection();

      Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage();

      try
      {
         msg.setObjectProperty("JMS_HQ_InputStream", UnitTestCase.createFakeLargeStream(10));
         Assert.fail("Exception was expected");
      }
      catch (JMSException e)
      {
      }

      msg.setText("hello");

      MessageProducer prod = session.createProducer(queue1);

      prod.send(msg);

      conn.close();

      conn = cf.createConnection();

      session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer cons = session.createConsumer(queue1);

      conn.start();

      TextMessage rm = (TextMessage)cons.receive(10000);

      try
      {
         rm.setObjectProperty("JMS_HQ_OutputStream", new OutputStream()
         {
            @Override
            public void write(final int b) throws IOException
            {
               System.out.println("b = " + b);
            }

         });
         Assert.fail("Exception was expected");
      }
      catch (JMSException e)
      {
      }

      Assert.assertEquals("hello", rm.getText());

      Assert.assertNotNull(rm);

   }

   public void testWaitOnOutputStream() throws Exception
   {
      int msgSize = 1024 * 1024;

      conn = cf.createConnection();

      Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageProducer prod = session.createProducer(queue1);

      BytesMessage m = session.createBytesMessage();

      m.setObjectProperty("JMS_HQ_InputStream", UnitTestCase.createFakeLargeStream(msgSize));

      prod.send(m);

      conn.close();

      conn = cf.createConnection();

      session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer cons = session.createConsumer(queue1);

      conn.start();

      BytesMessage rm = (BytesMessage)cons.receive(10000);
      Assert.assertNotNull(rm);

      final AtomicLong numberOfBytes = new AtomicLong(0);

      final AtomicInteger numberOfErrors = new AtomicInteger(0);

      OutputStream out = new OutputStream()
      {

         int position = 0;

         @Override
         public void write(final int b) throws IOException
         {
            numberOfBytes.incrementAndGet();
            if (UnitTestCase.getSamplebyte(position++) != b)
            {
               System.out.println("Wrong byte at position " + position);
               numberOfErrors.incrementAndGet();
            }
         }

      };

      try
      {
         rm.setObjectProperty("JMS_HQ_InputStream", UnitTestCase.createFakeLargeStream(100));
         Assert.fail("Exception expected!");
      }
      catch (MessageNotWriteableException expected)
      {
      }

      rm.setObjectProperty("JMS_HQ_SaveStream", out);

      Assert.assertEquals(msgSize, numberOfBytes.get());

      Assert.assertEquals(0, numberOfErrors.get());

   }


   public void testHugeString() throws Exception
   {
      int msgSize = 1024 * 1024;

      conn = cf.createConnection();

      Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageProducer prod = session.createProducer(queue1);

      TextMessage m = session.createTextMessage();

      StringBuffer buffer = new StringBuffer();
      while(buffer.length() < msgSize)
      {
         buffer.append(UUIDGenerator.getInstance().generateStringUUID());
      }

      final String originalString = buffer.toString();

      m.setText(originalString);

      buffer = null;

      prod.send(m);

      conn.close();

      validateNoFilesOnLargeDir(1);

      conn = cf.createConnection();

      session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer cons = session.createConsumer(queue1);

      conn.start();

      TextMessage rm = (TextMessage)cons.receive(10000);
      Assert.assertNotNull(rm);

      String str = rm.getText();
      assertEquals(originalString, str);
      conn.close();
      validateNoFilesOnLargeDir(0);

   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   class ThreadReader extends Thread
   {
      CountDownLatch latch;

      ThreadReader(final CountDownLatch latch)
      {
         this.latch = latch;
      }

      @Override
      public void run()
      {
      }
   }

}
