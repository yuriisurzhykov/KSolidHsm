package com.yuriisurzhykov.ksolidhsm.exceptions

import java.lang.IllegalStateException

class IllegalInheritanceException(child: Any, parent: Any?) : IllegalStateException(
    "Child state $child is inherited from $parent, but inheritance "
)