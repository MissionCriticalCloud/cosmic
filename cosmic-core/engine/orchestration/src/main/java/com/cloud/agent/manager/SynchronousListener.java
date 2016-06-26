// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.agent.manager;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.utils.Profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronousListener implements Listener {
    private static final Logger s_logger = LoggerFactory.getLogger(SynchronousListener.class);

    protected Answer[] _answers;
    protected boolean _disconnected;
    protected String _peer;

    public SynchronousListener(Listener listener) {
        _answers = null;
        _peer = null;
    }

    public String getPeer() {
        return _peer;
    }

    public void setPeer(String peer) {
        _peer = peer;
    }

    public synchronized Answer[] getAnswers() {
        return _answers;
    }

    public synchronized boolean isDisconnected() {
        return _disconnected;
    }

    @Override
    public synchronized boolean processAnswers(long agentId, long seq, Answer[] resp) {
        _answers = resp;
        notifyAll();
        return true;
    }

    @Override
    public boolean processCommands(long agentId, long seq, Command[] req) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
        return null;
    }

    @Override
    public void processConnect(Host agent, StartupCommand cmd, boolean forRebalance) {
    }

    @Override
    public synchronized boolean processDisconnect(long agentId, Status state) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Agent disconnected, agent id: " + agentId + ", state: " + state + ". Will notify waiters");
        }

        _disconnected = true;
        notifyAll();
        return true;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public int getTimeout() {
        return -1;
    }

    @Override
    public boolean processTimeout(long agentId, long seq) {
        return true;
    }

    public Answer[] waitFor() throws InterruptedException {
        return waitFor(-1);
    }

    public synchronized Answer[] waitFor(int s) throws InterruptedException {
        if (_disconnected) {
            return null;
        }

        if (_answers != null) {
            return _answers;
        }

        Profiler profiler = new Profiler();
        profiler.start();
        if (s <= 0) {
            wait();
        } else {
            int ms = s * 1000;
            wait(ms);
        }
        profiler.stop();

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Synchronized command - sending completed, time: " + profiler.getDurationInMillis() + ", answer: " +
                    (_answers != null ? _answers[0].toString() : "null"));
        }
        return _answers;
    }
}
