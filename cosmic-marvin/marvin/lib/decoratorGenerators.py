"""Custom decorator generators used across test cases
"""

from functools import wraps


def skipTestIf(attribute):
    def decorator(test):
        @wraps(test)
        def test_wrapper(self, *args, **kwargs):
            if hasattr(self, attribute):
                if getattr(self, attribute):
                    self.skipTest("Skipping test: Reason -  %s" % attribute)
                else:
                    return test(self, *args, **kwargs)
            else:
                return test(self, *args, **kwargs)

        return test_wrapper

    return decorator
