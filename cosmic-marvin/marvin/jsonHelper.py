import inspect
from marvin.cloudstackAPI import *

import cloudstackException


class jsonLoader(object):
    '''The recursive class for building and representing objects with.'''

    def __init__(self, obj):
        for k in obj:
            v = obj[k]
            if isinstance(v, dict):
                setattr(self, k, jsonLoader(v))
            elif isinstance(v, (list, tuple)):
                if len(v) > 0 and isinstance(v[0], dict):
                    setattr(self, k, [jsonLoader(elem) for elem in v])
                else:
                    setattr(self, k, v)
            else:
                setattr(self, k, v)

    def __getattr__(self, val):
        if val in self.__dict__:
            return self.__dict__[val]
        else:
            return None

    def __repr__(self):
        return '{%s}' % str(', '.join('%s : %s' % (k, repr(v)) for (k, v)
                                      in self.__dict__.iteritems()))

    def __str__(self):
        return '{%s}' % str(', '.join('%s : %s' % (k, repr(v)) for (k, v)
                                      in self.__dict__.iteritems()))


class jsonDump(object):
    @staticmethod
    def __serialize(obj):
        """Recursively walk object's hierarchy."""
        if isinstance(obj, (bool, int, long, float, basestring)):
            return obj
        elif isinstance(obj, dict):
            obj = obj.copy()
            newobj = { }
            for key in obj:
                if obj[key] is not None:
                    if (isinstance(obj[key], list) and len(obj[key]) == 0):
                        continue
                    newobj[key] = jsonDump.__serialize(obj[key])

            return newobj
        elif isinstance(obj, list):
            return [jsonDump.__serialize(item) for item in obj]
        elif isinstance(obj, tuple):
            return tuple(jsonDump.__serialize([item for item in obj]))
        elif hasattr(obj, '__dict__'):
            return jsonDump.__serialize(obj.__dict__)
        else:
            return repr(obj)  # Don't know how to handle, convert to string

    @staticmethod
    def dump(obj):
        return jsonDump.__serialize(obj)


def getclassFromName(cmd, name):
    module = inspect.getmodule(cmd)
    return getattr(module, name)()


def finalizeResultObj(result, responseName, responsecls):
    responsclsLoadable = (responsecls is None
                          and responseName.endswith("response")
                          and responseName != "queryasyncjobresultresponse")
    if responsclsLoadable:
        '''infer the response class from the name'''
        moduleName = responseName.replace("response", "")
        try:
            responsecls = getclassFromName(moduleName, responseName)
        except:
            pass

    responsNameValid = (responseName is not None
                        and responseName == "queryasyncjobresultresponse"
                        and responsecls is not None
                        and result.jobresult is not None)
    if responsNameValid:
        result.jobresult = finalizeResultObj(result.jobresult, None,
                                             responsecls)
        return result
    elif responsecls is not None:
        for k, v in result.__dict__.iteritems():
            if k in responsecls.__dict__:
                return result

        attr = result.__dict__.keys()[0]

        value = getattr(result, attr)
        if not isinstance(value, jsonLoader):
            return result

        findObj = False
        for k, v in value.__dict__.iteritems():
            if k in responsecls.__dict__:
                findObj = True
                break
        if findObj:
            return value
        else:
            return result
    else:
        return result


def getResultObj(returnObj, responsecls=None):
    if len(returnObj) == 0:
        return None
    responseName = filter(lambda a: a != u'cloudstack-version',
                          returnObj.keys())[0]

    response = returnObj[responseName]
    if len(response) == 0:
        return None

    result = jsonLoader(response)
    if result.errorcode is not None:
        errMsg = "errorCode: %s, errorText:%s" % (result.errorcode,
                                                  result.errortext)
        respname = responseName.replace("response", "")
        raise cloudstackException.CloudstackAPIException(respname, errMsg)

    if result.count is not None:
        for key in result.__dict__.iterkeys():
            if key == "count":
                continue
            else:
                return getattr(result, key)
    else:
        return finalizeResultObj(result, responseName, responsecls)
