//

//

package com.cloud.agent.transport;

import com.cloud.agent.api.Answer;
import com.cloud.exception.UnsupportedVersionException;

/**
 *
 */
public class Response extends Request {
    protected Response() {
    }

    public Response(final Request request, final Answer answer) {
        this(request, new Answer[]{answer});
    }

    public Response(final Request request, final Answer[] answers) {
        super(request, answers);
    }

    public Response(final Request request, final Answer answer, final long mgmtId, final long agentId) {
        this(request, new Answer[]{answer}, mgmtId, agentId);
    }

    public Response(final Request request, final Answer[] answers, final long mgmtId, final long agentId) {
        super(request, answers);
        _mgmtId = mgmtId;
        _via = agentId;
    }

    protected Response(final Version ver, final long seq, final long agentId, final long mgmtId, final long via, final short flags, final String ans) {
        super(ver, seq, agentId, mgmtId, via, flags, ans);
    }

    public static Response parse(final byte[] bytes) throws ClassNotFoundException, UnsupportedVersionException {
        return (Response) Request.parse(bytes);
    }

    public Answer getAnswer() {
        final Answer[] answers = getAnswers();
        return answers[0];
    }

    public Answer[] getAnswers() {
        if (_cmds == null) {
            _cmds = s_gson.fromJson(_content, Answer[].class);
        }
        return (Answer[]) _cmds;
    }

    @Override
    protected String getType() {
        return "Ans: ";
    }
}
