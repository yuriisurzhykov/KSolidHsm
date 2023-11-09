package com.yuriisurzhykov.ksolidhsm.exceptions

class RecursiveHierarchyException(
    child: Any,
    parent: Any
) : IllegalStateException("Child $child infer to parent $parent, but parent infer to child! Check your hierarchy of states!")