//

//

package com.cloud.utils.cisco.n1kv.vsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VsmPolicyMapResponse extends VsmResponse {
    private static final Logger s_logger = LoggerFactory.getLogger(VsmPolicyMapResponse.class);
    private static final String s_policyMapDetails = "__XML__OPT_Cmd_show_policy-map___readonly__";

    private final PolicyMap _policyMap = new PolicyMap();

    VsmPolicyMapResponse(final String response) {
        super(response);
        initialize();
    }

    public PolicyMap getPolicyMap() {
        return _policyMap;
    }

    @Override
    protected void parse(final Element root) {
        final NodeList list = root.getElementsByTagName("nf:rpc-error");
        if (list.getLength() == 0) {
            // No rpc-error tag; means response was ok.
            final NodeList dataList = root.getElementsByTagName("nf:data");
            if (dataList.getLength() > 0) {
                parseData(dataList.item(0));
                _responseOk = true;
            }
        } else {
            super.parseError(list.item(0));
            _responseOk = false;
        }
    }

    protected void parseData(final Node data) {
        try {
            final NodeList list = ((Element) data).getElementsByTagName(s_policyMapDetails);
            if (list.getLength() > 0) {
                final NodeList readOnlyList = ((Element) list.item(0)).getElementsByTagName("__readonly__");
                final Element readOnly = (Element) readOnlyList.item(0);

                for (Node node = readOnly.getFirstChild(); node != null; node = node.getNextSibling()) {
                    final String currentNode = node.getNodeName();
                    final String value = node.getTextContent();
                    if ("pmap-name-out".equalsIgnoreCase(currentNode)) {
                        _policyMap.policyMapName = value;
                    } else if ("cir".equalsIgnoreCase(currentNode)) {
                        _policyMap.committedRate = Integer.parseInt(value.trim());
                    } else if ("bc".equalsIgnoreCase(currentNode)) {
                        _policyMap.burstRate = Integer.parseInt(value.trim());
                    } else if ("pir".equalsIgnoreCase(currentNode)) {
                        _policyMap.peakRate = Integer.parseInt(value.trim());
                    }
                }
            }
        } catch (final DOMException e) {
            s_logger.error("Error parsing the response : " + e.toString());
        }
    }
}
