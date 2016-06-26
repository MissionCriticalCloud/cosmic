//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.utils.Pair;

import java.util.List;
import java.util.Map;

public class MigrateWithStorageReceiveAnswer extends Answer {

    List<Pair<VolumeTO, Object>> volumeToSr;
    List<Pair<NicTO, Object>> nicToNetwork;
    Map<String, String> token;

    public MigrateWithStorageReceiveAnswer(final MigrateWithStorageReceiveCommand cmd, final Exception ex) {
        super(cmd, ex);
        volumeToSr = null;
        nicToNetwork = null;
        token = null;
    }

    public MigrateWithStorageReceiveAnswer(final MigrateWithStorageReceiveCommand cmd, final List<Pair<VolumeTO, Object>> volumeToSr, final List<Pair<NicTO, Object>> nicToNetwork,
                                           final Map<String, String> token) {
        super(cmd, true, null);
        this.volumeToSr = volumeToSr;
        this.nicToNetwork = nicToNetwork;
        this.token = token;
    }

    public List<Pair<VolumeTO, Object>> getVolumeToSr() {
        return volumeToSr;
    }

    public List<Pair<NicTO, Object>> getNicToNetwork() {
        return nicToNetwork;
    }

    public Map<String, String> getToken() {
        return token;
    }
}
