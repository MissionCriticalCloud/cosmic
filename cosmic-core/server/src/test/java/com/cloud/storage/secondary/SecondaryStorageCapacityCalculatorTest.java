package com.cloud.storage.secondary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SecondaryStorageCapacityCalculatorTest {

    @Parameters(name = "{index}: calculateRequiredCapacity({0},{1})={2}")
    public static Collection<Integer[]> data() {
        return Arrays.asList(new Integer[][]{
                {1, 1, 1}, {2, 2, 1}, {3, 3, 1}, {10, 10, 1},  // required capacity is exactly matched by 1 VM
                {2, 1, 2}, {3, 1, 3}, {4, 1, 4}, {5, 1, 5},    // required capacity is met by having as many VMs as the required stand by
                {4, 2, 2}, {6, 2, 3}, {8, 2, 4}, {10, 2, 5},   // required capacity is met by having as many VMs as the required stand by (x2)
                {6, 3, 2}, {9, 3, 3}, {12, 3, 4}, {15, 3, 5},  // required capacity is met by having as many VMs as the required stand by (x3)
                {1, 2, 1}, {1, 3, 1}, {2, 4, 1}, {3, 6, 1},    // required capacity is surpassed (single VM is enough)
        });
    }

    private final int commandsStandBy;
    private final int commandsPerVm;
    private final int expected;

    public SecondaryStorageCapacityCalculatorTest(final int commandsStandBy, final int commandsPerVm, final int expected) {
        this.commandsStandBy = commandsStandBy;
        this.commandsPerVm = commandsPerVm;
        this.expected = expected;
    }

    @Test
    public void test_calculateRequiredCapacity() throws Exception {
        final SecondaryStorageCapacityCalculator calculator = new SecondaryStorageCapacityCalculator();

        final int requiredVms = calculator.calculateRequiredCapacity(commandsStandBy, commandsPerVm);

        assertThat(requiredVms, is(expected));
    }
}
