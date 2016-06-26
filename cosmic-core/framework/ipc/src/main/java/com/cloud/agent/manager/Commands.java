package com.cloud.agent.manager;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.Command.OnError;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Commands implements Iterable<Command> {
    private final ArrayList<String> _ids = new ArrayList<>();
    private final ArrayList<Command> _cmds = new ArrayList<>();
    OnError _handler;
    private Answer[] _answers;

    public Commands(final Command cmd) {
        this(OnError.Stop);
        addCommand(cmd);
    }

    public Commands(final OnError handler) {
        _handler = handler;
    }

    public void addCommand(final Command cmd) {
        addCommand(null, cmd);
    }

    public void addCommand(final String id, final Command cmd) {
        _ids.add(id);
        _cmds.add(cmd);
    }

    public void addCommands(final List<Command> cmds) {
        int i = 0;
        for (final Command cmd : cmds) {
            addCommand(Integer.toString(i++), cmd);
        }
    }

    public int size() {
        return _cmds.size();
    }

    public void addCommand(final int index, final Command cmd) {
        _cmds.add(index, cmd);
    }

    public Answer getAnswer(final String id) {
        final int i = _ids.indexOf(id);
        return i == -1 ? null : _answers[i];
    }

    public <T extends Answer> T getAnswer(final Class<T> clazz) {
        assert (clazz != Answer.class) : "How do you expect to get a unique answer in this case?  huh?  How? How? How?....one more time....How?";
        for (final Answer answer : _answers) {
            if (answer.getClass() == clazz) {
                return (T) answer;
            }
        }
        throw new CloudRuntimeException("Unable to get answer that is of " + clazz);
    }

    public <T extends Command> Answer getAnswerFor(final Class<T> clazz) {
        assert (clazz != Command.class) : "You passed in a generic Command.  Seriously, you think you did that?";
        int i = 0;
        for (final Command cmd : _cmds) {
            if (cmd.getClass() == clazz) {
                break;
            }
            i++;
        }

        assert i < _cmds.size() : "You sure you actually sent this command " + clazz;

        return _answers[i];
    }

    public Command[] toCommands() {
        return _cmds.toArray(new Command[_cmds.size()]);
    }

    public OnError getErrorHandling() {
        return _handler;
    }

    public boolean stopOnError() {
        return _handler == OnError.Stop;
    }

    public Answer[] getAnswers() {
        return _answers;
    }

    public void setAnswers(final Answer[] answers) {
        _answers = answers;
    }

    public <T extends Command> T getCommand(final Class<T> clazz) {
        for (final Command cmd : _cmds) {
            if (cmd.getClass() == clazz) {
                return (T) cmd;
            }
        }
        return null;
    }

    /**
     * @return For Commands with handler OnError.Continue, one command succeeding is successful.  If not, all commands must succeed to be successful.
     */
    public boolean isSuccessful() {
        if (_answers == null) {
            return false;
        }
        if (_handler == OnError.Continue) {
            return true;
        }
        for (final Answer answer : _answers) {
            if (_handler == OnError.Continue && answer.getResult()) {
                return true;
            } else if (_handler != OnError.Continue && !answer.getResult()) {
                return false;
            }
        }

        return _handler != OnError.Continue;
    }

    @Override
    public Iterator<Command> iterator() {
        return _cmds.iterator();
    }
}
