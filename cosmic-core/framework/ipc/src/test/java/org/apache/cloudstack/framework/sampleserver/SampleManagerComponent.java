package org.apache.cloudstack.framework.sampleserver;

import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.framework.messagebus.MessageDispatcher;
import org.apache.cloudstack.framework.messagebus.MessageHandler;
import org.apache.cloudstack.framework.rpc.RpcCallbackListener;
import org.apache.cloudstack.framework.rpc.RpcException;
import org.apache.cloudstack.framework.rpc.RpcProvider;
import org.apache.cloudstack.framework.rpc.RpcServerCall;
import org.apache.cloudstack.framework.rpc.RpcServiceDispatcher;
import org.apache.cloudstack.framework.rpc.RpcServiceHandler;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SampleManagerComponent {
    private static final Logger s_logger = LoggerFactory.getLogger(SampleManagerComponent.class);

    @Inject
    private MessageBus _eventBus;

    @Inject
    private RpcProvider _rpcProvider;

    private final Timer _timer = new Timer();

    public SampleManagerComponent() {
    }

    @PostConstruct
    public void init() {
        _rpcProvider.registerRpcServiceEndpoint(RpcServiceDispatcher.getDispatcher(this));

        // subscribe to all network events (for example)
        _eventBus.subscribe("network", MessageDispatcher.getDispatcher(this));

        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                testRpc();
            }
        }, 3000);
    }

    void testRpc() {
        final SampleStoragePrepareCommand cmd = new SampleStoragePrepareCommand();
        cmd.setStoragePool("Pool1");
        cmd.setVolumeId("vol1");

        _rpcProvider.newCall()
                    .setCommand("StoragePrepare")
                    .setCommandArg(cmd)
                    .setTimeout(10000)
                    .addCallbackListener(new RpcCallbackListener<SampleStoragePrepareAnswer>() {
                        @Override
                        public void onSuccess(final SampleStoragePrepareAnswer result) {
                            s_logger.info("StoragePrepare return result: " + result.getResult());
                        }

                        @Override
                        public void onFailure(final RpcException e) {
                            s_logger.info("StoragePrepare failed");
                        }
                    })
                    .apply();
    }

    @RpcServiceHandler(command = "NetworkPrepare")
    void onStartCommand(final RpcServerCall call) {
        call.completeCall("NetworkPrepare completed");
    }

    @MessageHandler(topic = "network.prepare")
    void onPrepareNetwork(final String sender, final String topic, final Object args) {
    }
}
