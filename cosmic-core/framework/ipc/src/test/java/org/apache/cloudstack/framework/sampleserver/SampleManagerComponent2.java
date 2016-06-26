package org.apache.cloudstack.framework.sampleserver;

import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.framework.messagebus.MessageDispatcher;
import org.apache.cloudstack.framework.messagebus.MessageHandler;
import org.apache.cloudstack.framework.rpc.RpcProvider;
import org.apache.cloudstack.framework.rpc.RpcServerCall;
import org.apache.cloudstack.framework.rpc.RpcServiceDispatcher;
import org.apache.cloudstack.framework.rpc.RpcServiceHandler;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SampleManagerComponent2 {
    private static final Logger s_logger = LoggerFactory.getLogger(SampleManagerComponent2.class);

    @Inject
    private MessageBus _eventBus;

    @Inject
    private RpcProvider _rpcProvider;

    public SampleManagerComponent2() {
    }

    @PostConstruct
    public void init() {
        _rpcProvider.registerRpcServiceEndpoint(RpcServiceDispatcher.getDispatcher(this));

        // subscribe to all network events (for example)
        _eventBus.subscribe("storage", MessageDispatcher.getDispatcher(this));
    }

    @RpcServiceHandler(command = "StoragePrepare")
    void onStartCommand(final RpcServerCall call) {
        s_logger.info("Reevieved StoragePrpare call");
        final SampleStoragePrepareCommand cmd = call.getCommandArgument();

        s_logger.info("StoragePrepare command arg. pool: " + cmd.getStoragePool() + ", vol: " + cmd.getVolumeId());
        final SampleStoragePrepareAnswer answer = new SampleStoragePrepareAnswer();
        answer.setResult("Successfully executed StoragePrepare command");

        call.completeCall(answer);
    }

    @MessageHandler(topic = "storage.prepare")
    void onPrepareNetwork(final String sender, final String topic, final Object args) {
    }

    void test() {

    }
}
