package org.apache.cloudstack.spring.lifecycle;

public class CloudStackExtendedLifeCycleStart extends AbstractSmartLifeCycle implements Runnable {

    CloudStackExtendedLifeCycle lifeCycle;

    @Override
    public void stop() {
        lifeCycle.stopBeans();
        super.stop();
    }

    @Override
    public int getPhase() {
        return 3000;
    }

    public CloudStackExtendedLifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public void setLifeCycle(final CloudStackExtendedLifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    @Override
    public void run() {
        lifeCycle.startBeans();
    }
}
