package com.cloud.network.router.deployment;

import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.dao.UserIpv6AddressDao;
import com.cloud.network.dao.VirtualRouterProviderDao;
import com.cloud.network.router.NetworkHelper;
import com.cloud.network.router.VpcNetworkHelperImpl;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcManager;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.network.vpc.dao.VpcOfferingDao;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.VirtualMachineProfile.Param;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;

public class RouterDeploymentDefinitionBuilder {

    @Inject
    protected NetworkDao networkDao;
    @Inject
    @Qualifier("networkHelper")
    protected NetworkHelper nwHelper;
    @Inject
    @Qualifier("vpcNetworkHelper")
    protected VpcNetworkHelperImpl vpcNwHelper;
    protected Long offeringId;
    @Inject
    private DomainRouterDao routerDao;
    @Inject
    private PhysicalNetworkServiceProviderDao physicalProviderDao;
    @Inject
    private NetworkModel networkModel;
    @Inject
    private VirtualRouterProviderDao vrProviderDao;
    @Inject
    private NetworkOfferingDao networkOfferingDao;
    @Inject
    private ServiceOfferingDao serviceOfferingDao;
    @Inject
    private IpAddressManager ipAddrMgr;
    @Inject
    private VMInstanceDao vmDao;
    @Inject
    private HostPodDao podDao;
    @Inject
    private AccountManager accountMgr;
    @Inject
    private NetworkOrchestrationService networkMgr;
    @Inject
    private NicDao nicDao;
    @Inject
    private UserIpv6AddressDao ipv6Dao;
    @Inject
    private IPAddressDao ipAddressDao;
    @Inject
    private VpcDao vpcDao;
    @Inject
    private VpcOfferingDao vpcOffDao;
    @Inject
    private PhysicalNetworkDao pNtwkDao;
    @Inject
    private VpcManager vpcMgr;
    @Inject
    private VlanDao vlanDao;

    public void setOfferingId(final Long offeringId) {
        this.offeringId = offeringId;
    }

    public IntermediateStateBuilder create() {
        return new IntermediateStateBuilder(this);
    }

    protected RouterDeploymentDefinition injectDependencies(
            final RouterDeploymentDefinition routerDeploymentDefinition) {

        routerDeploymentDefinition.networkDao = networkDao;
        routerDeploymentDefinition.routerDao = routerDao;
        routerDeploymentDefinition.physicalProviderDao = physicalProviderDao;
        routerDeploymentDefinition.networkModel = networkModel;
        routerDeploymentDefinition.vrProviderDao = vrProviderDao;
        routerDeploymentDefinition.networkOfferingDao = networkOfferingDao;
        routerDeploymentDefinition.serviceOfferingDao = serviceOfferingDao;
        routerDeploymentDefinition.ipAddrMgr = ipAddrMgr;
        routerDeploymentDefinition.vmDao = vmDao;
        routerDeploymentDefinition.podDao = podDao;
        routerDeploymentDefinition.accountMgr = accountMgr;
        routerDeploymentDefinition.networkMgr = networkMgr;
        routerDeploymentDefinition.nicDao = nicDao;
        routerDeploymentDefinition.ipv6Dao = ipv6Dao;
        routerDeploymentDefinition.ipAddressDao = ipAddressDao;
        routerDeploymentDefinition.serviceOfferingId = offeringId;

        routerDeploymentDefinition.nwHelper = nwHelper;

        if (routerDeploymentDefinition instanceof VpcRouterDeploymentDefinition) {
            injectVpcDependencies((VpcRouterDeploymentDefinition) routerDeploymentDefinition);
        }

        return routerDeploymentDefinition;
    }

    protected void injectVpcDependencies(
            final VpcRouterDeploymentDefinition routerDeploymentDefinition) {

        routerDeploymentDefinition.vpcDao = vpcDao;
        routerDeploymentDefinition.vpcOffDao = vpcOffDao;
        routerDeploymentDefinition.pNtwkDao = pNtwkDao;
        routerDeploymentDefinition.vpcMgr = vpcMgr;
        routerDeploymentDefinition.vlanDao = vlanDao;
        routerDeploymentDefinition.nwHelper = vpcNwHelper;
        routerDeploymentDefinition.routerDao = routerDao;
    }

    public class IntermediateStateBuilder {

        protected Vpc vpc;
        protected Network guestNetwork;
        protected DeployDestination dest;
        protected Account owner;
        protected Map<Param, Object> params;
        protected List<DomainRouterVO> routers = new ArrayList<>();
        RouterDeploymentDefinitionBuilder builder;

        protected IntermediateStateBuilder(final RouterDeploymentDefinitionBuilder builder) {
            this.builder = builder;
        }

        public IntermediateStateBuilder setVpc(final Vpc vpc) {
            this.vpc = vpc;
            return this;
        }

        public IntermediateStateBuilder setGuestNetwork(final Network nw) {
            guestNetwork = nw;
            return this;
        }

        public IntermediateStateBuilder setAccountOwner(final Account owner) {
            this.owner = owner;
            return this;
        }

        public IntermediateStateBuilder setDeployDestination(final DeployDestination dest) {
            this.dest = dest;
            return this;
        }

        public IntermediateStateBuilder setParams(final Map<Param, Object> params) {
            this.params = params;
            return this;
        }

        public RouterDeploymentDefinition build() {
            final RouterDeploymentDefinition routerDeploymentDefinition;
            if (vpc != null) {
                routerDeploymentDefinition = new VpcRouterDeploymentDefinition(guestNetwork, vpc, dest, owner, params);
            } else {
                routerDeploymentDefinition = new RouterDeploymentDefinition(guestNetwork, dest, owner, params);
            }

            return builder.injectDependencies(routerDeploymentDefinition);
        }
    }
}
