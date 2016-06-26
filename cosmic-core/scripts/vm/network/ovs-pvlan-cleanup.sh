#! /bin/bash

#!/bin/bash

ovs-ofctl del-flows xenbr0
ovs-ofctl add-flow xenbr0 priority=0,actions=NORMAL

