package com.cloud.resource;

import com.cloud.agent.api.StartupCommand;
import com.cloud.host.HostVO;
import com.cloud.utils.component.Adapter;

import java.util.List;
import java.util.Map;

public interface ResourceStateAdapter extends Adapter {
    public HostVO createHostVOForConnectedAgent(HostVO host, StartupCommand[] cmd);

    public HostVO createHostVOForDirectConnectAgent(HostVO host, final StartupCommand[] startup, ServerResource resource, Map<String, String> details,
                                                    List<String> hostTags);

    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced, boolean isForceDeleteStorage) throws UnableDeleteHostException;

    static public enum Event {
        CREATE_HOST_VO_FOR_CONNECTED, CREATE_HOST_VO_FOR_DIRECT_CONNECT, DELETE_HOST,
    }

    static public class DeleteHostAnswer {
        private final boolean isContinue;
        private final boolean isException;

        public DeleteHostAnswer(final boolean isContinue) {
            this.isContinue = isContinue;
            this.isException = false;
        }

        public DeleteHostAnswer(final boolean isContinue, final boolean isException) {
            this.isContinue = isContinue;
            this.isException = isException;
        }

        public boolean getIsContinue() {
            return this.isContinue;
        }

        public boolean getIsException() {
            return this.isException;
        }
    }
}
