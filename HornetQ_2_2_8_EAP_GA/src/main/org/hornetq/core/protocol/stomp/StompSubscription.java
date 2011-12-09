/*
 * Copyright 2010 Red Hat, Inc.
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

package org.hornetq.core.protocol.stomp;

/**
 * A StompSubscription
 *
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 *
 *
 */
public class StompSubscription
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private final String subID;
   
   private final boolean autoACK;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public StompSubscription(String subID, boolean ack)
   {
      this.subID = subID;
      this.autoACK = ack;
   }

   // Public --------------------------------------------------------

   public boolean isAutoACK()
   {
      return autoACK;
   }

   public String getID()
   {
      return subID;
   }

   @Override
   public String toString()
   {
      return "StompSubscription[id=" + subID + ", autoACK=" + autoACK + "]";
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
