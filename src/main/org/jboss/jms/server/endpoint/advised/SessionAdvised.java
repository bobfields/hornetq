/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.jms.server.endpoint.advised;

import java.util.List;

import javax.jms.JMSException;

import org.jboss.jms.delegate.BrowserDelegate;
import org.jboss.jms.delegate.ConsumerDelegate;
import org.jboss.jms.destination.JBossDestination;
import org.jboss.jms.destination.JBossQueue;
import org.jboss.jms.destination.JBossTopic;
import org.jboss.jms.message.JBossMessage;
import org.jboss.jms.server.endpoint.Ack;
import org.jboss.jms.server.endpoint.Cancel;
import org.jboss.jms.server.endpoint.SessionEndpoint;

/**
 * The server-side advised instance corresponding to a Session. It is bound to the AOP
 * Dispatcher's map.
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class SessionAdvised extends AdvisedSupport implements SessionEndpoint
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   protected SessionEndpoint endpoint;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionAdvised(SessionEndpoint endpoint)
   {
      this.endpoint = endpoint;
   }

   // SessionEndpoint implementation --------------------------------

   public void close() throws JMSException
   {
      endpoint.close();
   }

   public void closing() throws JMSException
   {
      endpoint.closing();
   }

   public void send(JBossMessage msg) throws JMSException
   {
      endpoint.send(msg);
   }
   
   public ConsumerDelegate createConsumerDelegate(JBossDestination destination, String selector,
                                                  boolean noLocal, String subscriptionName,
                                                  boolean connectionConsumer,
                                                  long failoverChannelID) throws JMSException
   {
      return endpoint.createConsumerDelegate(destination, selector, noLocal, subscriptionName,
                                             connectionConsumer, failoverChannelID);
   }
   
   public BrowserDelegate createBrowserDelegate(JBossDestination queue, String messageSelector,
                                                long failoverChannelID) throws JMSException                                                 
   {
      return endpoint.createBrowserDelegate(queue, messageSelector, failoverChannelID);
   }

   public JBossQueue createQueue(String queueName) throws JMSException
   {
      return endpoint.createQueue(queueName);
   }

   public JBossTopic createTopic(String topicName) throws JMSException
   {
      return endpoint.createTopic(topicName);
   }

   public void acknowledgeDeliveries(List acks) throws JMSException
   {
      endpoint.acknowledgeDeliveries(acks);
   }
   
   public void acknowledgeDelivery(Ack ack) throws JMSException
   {
      endpoint.acknowledgeDelivery(ack);
   }

   public void addTemporaryDestination(JBossDestination destination) throws JMSException
   {
      endpoint.addTemporaryDestination(destination);
   }

   public void deleteTemporaryDestination(JBossDestination destination) throws JMSException
   {
      endpoint.deleteTemporaryDestination(destination);
   }

   public void unsubscribe(String subscriptionName) throws JMSException
   {
      endpoint.unsubscribe(subscriptionName);
   }
   
   public void cancelDeliveries(List ackInfos) throws JMSException
   {
      endpoint.cancelDeliveries(ackInfos);
   }
   
   public void cancelDelivery(Cancel cancel) throws JMSException
   {
      endpoint.cancelDelivery(cancel);
   }
   
   public void recoverDeliveries(List ackInfos) throws JMSException
   {
      endpoint.recoverDeliveries(ackInfos);
   }

   // AdvisedSupport overrides --------------------------------------

   public Object getEndpoint()
   {
      return endpoint;
   }

   public String toString()
   {
      return "SessionAdvised->" + endpoint;
   }

   // Public --------------------------------------------------------

   // Protected -----------------------------------------------------

   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------

}
